package com.user_425.ewallet_relay.data.repository

import android.content.Context
import android.util.Log
import com.user_425.ewallet_relay.data.database.dao.NotificationLogDao
import com.user_425.ewallet_relay.data.database.dao.PendingNotificationDao
import com.user_425.ewallet_relay.data.database.entity.NotificationLogEntity
import com.user_425.ewallet_relay.data.database.entity.PendingNotificationEntity
import com.user_425.ewallet_relay.data.model.NotificationPayload
import com.user_425.ewallet_relay.data.model.TestPayload
import com.user_425.ewallet_relay.data.network.NotificationApiService
import com.user_425.ewallet_relay.data.preferences.UserPreferencesRepository
import com.user_425.ewallet_relay.data.security.EncryptedPreferencesManager
import com.user_425.ewallet_relay.utils.NetworkUtils
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: NotificationApiService,
    private val notificationLogDao: NotificationLogDao,
    private val pendingNotificationDao: PendingNotificationDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val encryptedPreferencesManager: EncryptedPreferencesManager,
    private val gson: Gson
) {
    
    companion object {
        private const val TAG = "NotificationRepository"
    }

    internal fun formatPostedAt(postedAt: String): String {
        return try {
            val parsedDateTime = OffsetDateTime.parse(postedAt)
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(parsedDateTime)
        } catch (e: Exception) {
            try {
                val instant = Instant.parse(postedAt)
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(instant)
            } catch (e2: Exception) {
                postedAt
            }
        }
    }
    
    fun getAllLogs(): Flow<List<NotificationLogEntity>> = notificationLogDao.getAllLogs()
    
    suspend fun insertLog(message: String, type: String, details: String? = null) {
        val log = NotificationLogEntity(
            timestamp = System.currentTimeMillis(),
            message = message,
            type = type,
            details = details
        )
        notificationLogDao.insertLog(log)
        
        // Clean up old logs to keep only 100 recent ones
        notificationLogDao.deleteOldLogs()
    }
    
    suspend fun clearAllLogs() {
        notificationLogDao.clearAllLogs()
        insertLog("Logs berhasil dibersihkan", "INFO")
    }
    
    suspend fun sendNotification(payload: NotificationPayload): Result<String> {
        val endpointUrl = userPreferencesRepository.getEndpointUrlSync()
        val apiKey = encryptedPreferencesManager.getApiKey()
        
        val ntfyEnabled = userPreferencesRepository.getNtfyEnabledSync()
        val ntfyUrl = userPreferencesRepository.getNtfyUrlSync()
        val ntfyTopic = userPreferencesRepository.getNtfyTopicSync()
        val ntfyUseAuth = userPreferencesRepository.getNtfyUseAuthSync()
        val ntfyToken = encryptedPreferencesManager.getNtfyToken()
        
        if (endpointUrl.isBlank() && !ntfyEnabled) {
            return Result.failure(Exception("Endpoint URL dan ntfy tidak aktif atau kosong"))
        }
        
        var endpointSuccess = true
        var ntfySuccess = true
        var errorMessage = ""
        
        // 1. Send to Webhook Endpoint
        if (endpointUrl.isNotBlank()) {
            try {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    queueNotification(payload, endpointUrl, apiKey)
                    insertLog("Tidak ada koneksi: notifikasi disimpan dalam antrian", "QUEUED", payload.packageName)
                    endpointSuccess = false
                    errorMessage = "Tidak ada koneksi internet untuk Webhook"
                } else {
                    val response = apiService.sendNotification(
                        url = endpointUrl,
                        apiKey = apiKey,
                        payload = payload
                    )
                    
                    if (response.isSuccessful) {
                        val statusCode = response.code()
                        insertLog("POST $statusCode ${payload.packageName}: ${payload.text?.take(50) ?: ""}", "SUCCESS")
                    } else {
                        val error = "HTTP ${response.code()}: ${response.message()}"
                        queueNotification(payload, endpointUrl, apiKey)
                        insertLog("Gagal kirim Webhook: disimpan dalam antrian: $error", "ERROR", payload.packageName)
                        endpointSuccess = false
                        errorMessage = error
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending to Webhook", e)
                queueNotification(payload, endpointUrl, apiKey)
                insertLog("Error Webhook: disimpan dalam antrian: ${e.message}", "ERROR", payload.packageName)
                endpointSuccess = false
                errorMessage = e.message ?: "Unknown webhook error"
            }
        }
        
        // 2. Send to Ntfy
        if (ntfyEnabled && ntfyTopic.isNotBlank()) {
            try {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    insertLog("Tidak ada koneksi: Gagal kirim ke ntfy", "ERROR", payload.packageName)
                    ntfySuccess = false
                    errorMessage = if (errorMessage.isEmpty()) "Tidak ada koneksi untuk ntfy" else "$errorMessage; Tidak ada koneksi untuk ntfy"
                } else {
                    val fullUrl = "${ntfyUrl.trimEnd('/')}/${ntfyTopic.trimStart('/')}"
                    val authHeader = if (ntfyUseAuth && !ntfyToken.isNullOrBlank()) "Bearer ${ntfyToken.trim()}" else null
                    
                    val formattedTime = formatPostedAt(payload.postedAt)
                    val ntfyMessage = buildString {
                        append("Aplikasi: ${payload.appName}\n")
                        if (!payload.title.isNullOrEmpty()) append("Judul: ${payload.title}\n")
                        if (!payload.text.isNullOrEmpty()) append("Pesan: ${payload.text}\n")
                        if (!payload.amountDetected.isNullOrEmpty()) append("Nominal: ${payload.amountDetected}\n")
                        append("Waktu: $formattedTime")
                    }
                    
                    val requestBody = ntfyMessage.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
                    
                    val response = apiService.sendNtfyNotification(
                        url = fullUrl,
                        title = payload.title ?: payload.appName,
                        priority = "3",
                        tags = "ewallet-relay",
                        authorization = authHeader,
                        message = requestBody
                    )
                    
                    if (response.isSuccessful) {
                        insertLog("Ntfy Berhasil: Terkirim ke topic $ntfyTopic", "SUCCESS")
                    } else {
                        val error = "HTTP ${response.code()}: ${response.message()}"
                        insertLog("Ntfy Gagal: $error", "ERROR", payload.packageName)
                        ntfySuccess = false
                        errorMessage = if (errorMessage.isEmpty()) error else "$errorMessage; $error"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending to ntfy", e)
                insertLog("Ntfy Error: ${e.message}", "ERROR", payload.packageName)
                ntfySuccess = false
                errorMessage = if (errorMessage.isEmpty()) e.message ?: "Ntfy error" else "$errorMessage; ${e.message}"
            }
        }
        
        return if (endpointSuccess && ntfySuccess) {
            Result.success("Success")
        } else {
            Result.failure(Exception(errorMessage.ifEmpty { "Gagal mengirim notifikasi" }))
        }
    }
    
    suspend fun sendTestNotification(): Result<String> {
        val endpointUrl = userPreferencesRepository.getEndpointUrlSync()
        val apiKey = encryptedPreferencesManager.getApiKey()
        
        val ntfyEnabled = userPreferencesRepository.getNtfyEnabledSync()
        val ntfyUrl = userPreferencesRepository.getNtfyUrlSync()
        val ntfyTopic = userPreferencesRepository.getNtfyTopicSync()
        val ntfyUseAuth = userPreferencesRepository.getNtfyUseAuthSync()
        val ntfyToken = encryptedPreferencesManager.getNtfyToken()
        
        if (endpointUrl.isBlank() && !ntfyEnabled) {
            return Result.failure(Exception("Endpoint URL dan ntfy tidak aktif atau kosong"))
        }
        
        var endpointSuccess = true
        var ntfySuccess = true
        var errorMessage = ""
        
        if (endpointUrl.isNotBlank()) {
            try {
                val testPayload = TestPayload(message = "This is a test notification from the Android app.")
                
                val response = apiService.sendTestNotification(
                    url = endpointUrl,
                    apiKey = apiKey,
                    payload = testPayload
                )
                
                if (response.isSuccessful) {
                    insertLog("Test Webhook berhasil: HTTP ${response.code()}", "SUCCESS")
                } else {
                    if (response.code() == 400) { // Check if the error code is 400
                        insertLog("Test Webhook berhasil: HTTP 400 (dianggap berhasil)", "SUCCESS")
                    } else {
                        val error = "Test Webhook gagal: HTTP ${response.code()}"
                        insertLog(error, "ERROR")
                        endpointSuccess = false
                        errorMessage = error
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending test notification to Webhook", e)
                insertLog("Test Webhook error: ${e.message}", "ERROR")
                endpointSuccess = false
                errorMessage = e.message ?: "Webhook test error"
            }
        }
        
        if (ntfyEnabled && ntfyTopic.isNotBlank()) {
            try {
                val fullUrl = "${ntfyUrl.trimEnd('/')}/${ntfyTopic.trimStart('/')}"
                val authHeader = if (ntfyUseAuth && !ntfyToken.isNullOrBlank()) "Bearer ${ntfyToken.trim()}" else null
                val testMessage = "This is a test notification from the EWallet Relay App."
                val requestBody = testMessage.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
                
                val response = apiService.sendNtfyNotification(
                    url = fullUrl,
                    title = "Test EWallet Relay",
                    priority = "3",
                    tags = "ewallet-relay,test",
                    authorization = authHeader,
                    message = requestBody
                )
                
                if (response.isSuccessful) {
                    insertLog("Test Ntfy Berhasil: Terkirim ke topic $ntfyTopic", "SUCCESS")
                } else {
                    val error = "Test Ntfy gagal: HTTP ${response.code()}"
                    insertLog(error, "ERROR")
                    ntfySuccess = false
                    errorMessage = if (errorMessage.isEmpty()) error else "$errorMessage; $error"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending test notification to ntfy", e)
                insertLog("Test Ntfy error: ${e.message}", "ERROR")
                ntfySuccess = false
                errorMessage = if (errorMessage.isEmpty()) e.message ?: "Ntfy test error" else "$errorMessage; ${e.message}"
            }
        }
        
        return if (endpointSuccess && ntfySuccess) {
            Result.success("Test berhasil")
        } else {
            Result.failure(Exception(errorMessage.ifEmpty { "Test gagal" }))
        }
    }
    
    private suspend fun queueNotification(payload: NotificationPayload, endpointUrl: String, apiKey: String?) {
        val jsonPayload = gson.toJson(payload)
        val pendingNotification = PendingNotificationEntity(
            jsonPayload = jsonPayload,
            endpointUrl = endpointUrl,
            apiKey = apiKey,
            createdAt = System.currentTimeMillis()
        )
        pendingNotificationDao.insertPending(pendingNotification)
    }
    
    suspend fun getPendingNotifications(): List<PendingNotificationEntity> {
        return pendingNotificationDao.getAllPendingList()
    }
    
    suspend fun retryPendingNotification(pendingNotification: PendingNotificationEntity): Result<String> {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return Result.failure(Exception("No network connection"))
            }
            
            val payload = gson.fromJson(pendingNotification.jsonPayload, NotificationPayload::class.java)
            
            val response = apiService.sendNotification(
                url = pendingNotification.endpointUrl,
                apiKey = pendingNotification.apiKey,
                payload = payload
            )
            
            if (response.isSuccessful) {
                // Remove from queue on success
                pendingNotificationDao.deletePending(pendingNotification)
                insertLog("Retry berhasil: ${payload.packageName}", "SUCCESS")
                Result.success("Retry successful")
            } else {
                // Update retry count
                pendingNotificationDao.incrementRetryCount(
                    pendingNotification.id, 
                    "HTTP ${response.code()}: ${response.message()}"
                )
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrying notification", e)
            pendingNotificationDao.incrementRetryCount(pendingNotification.id, e.message ?: "Unknown error")
            Result.failure(e)
        }
    }
    
    suspend fun deletePendingNotification(id: Long) {
        pendingNotificationDao.deletePendingById(id)
    }
    
    suspend fun clearAllPendingNotifications() {
        pendingNotificationDao.clearAllPending()
    }
    
    suspend fun getLogsSummaryText(): String {
        val logs = notificationLogDao.getRecentLogs(100)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        
        return logs.joinToString("\n") { log ->
            val timestamp = formatter.format(Instant.ofEpochMilli(log.timestamp))
            "[$timestamp] [${log.type}] ${log.message}"
        }
    }
}