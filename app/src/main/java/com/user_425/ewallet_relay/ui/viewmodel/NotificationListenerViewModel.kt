package com.user_425.ewallet_relay.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.user_425.ewallet_relay.data.preferences.UserPreferencesRepository
import com.user_425.ewallet_relay.data.repository.NotificationRepository
import com.user_425.ewallet_relay.data.model.PackageFilter
import com.user_425.ewallet_relay.data.security.EncryptedPreferencesManager
import com.user_425.ewallet_relay.service.ForegroundService
import com.user_425.ewallet_relay.service.NotificationCaptureService
import com.user_425.ewallet_relay.ui.state.NotificationListenerUiState
import com.user_425.ewallet_relay.ui.state.UiEvent
import com.user_425.ewallet_relay.ui.state.ValidationErrors
import com.user_425.ewallet_relay.utils.NotificationUtils
import com.user_425.ewallet_relay.worker.NotificationRetryWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NotificationListenerViewModel @Inject constructor(
    application: Application,
    private val notificationRepository: NotificationRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val encryptedPreferencesManager: EncryptedPreferencesManager
) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "NotificationListenerViewModel"
    }
    
    private val context: Context = application.applicationContext
    
    private val _uiState = MutableStateFlow(NotificationListenerUiState())
    val uiState: StateFlow<NotificationListenerUiState> = _uiState.asStateFlow()
    
    init {
        observePreferences()
        observeLogs()
        checkInitialState()
    }
    
    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.endpointUrl,
                userPreferencesRepository.filterPackagesList,
                userPreferencesRepository.forwardAllApps,
                userPreferencesRepository.serviceEnabled,
                userPreferencesRepository.batteryOptimizationShown
            ) { endpointUrl, filterPackages, forwardAllApps, serviceEnabled, batteryOptimizationShown ->
                _uiState.value = _uiState.value.copy(
                    endpointUrl = endpointUrl,
                    filterPackages = filterPackages,
                    forwardAllApps = forwardAllApps,
                    serviceEnabled = serviceEnabled,
                    batteryOptimizationShown = batteryOptimizationShown,
                    apiKey = encryptedPreferencesManager.getApiKey() ?: ""
                )
            }.collect()
        }
        viewModelScope.launch {
            combine(
                userPreferencesRepository.ntfyEnabled,
                userPreferencesRepository.ntfyUrl,
                userPreferencesRepository.ntfyTopic,
                userPreferencesRepository.ntfyUseAuth
            ) { ntfyEnabled, ntfyUrl, ntfyTopic, ntfyUseAuth ->
                _uiState.value = _uiState.value.copy(
                    ntfyEnabled = ntfyEnabled,
                    ntfyUrl = ntfyUrl,
                    ntfyTopic = ntfyTopic,
                    ntfyUseAuth = ntfyUseAuth,
                    ntfyToken = encryptedPreferencesManager.getNtfyToken() ?: ""
                )
            }.collect()
        }
    }
    
    private fun observeLogs() {
        viewModelScope.launch {
            notificationRepository.getAllLogs().collect { logs ->
                _uiState.value = _uiState.value.copy(logs = logs)
            }
        }
    }
    
    private fun checkInitialState() {
        viewModelScope.launch {
            checkPermissionStatus()
            
            // Generate device ID if needed
            val deviceId = userPreferencesRepository.getDeviceIdSync()
            if (deviceId.isEmpty()) {
                val newDeviceId = UUID.randomUUID().toString()
                userPreferencesRepository.setDeviceId(newDeviceId)
                notificationRepository.insertLog("Device ID dibuat: ${newDeviceId.take(8)}...", "INFO")
            }
        }
    }
    
    fun handleEvent(event: UiEvent) {
        when (event) {
            is UiEvent.CheckPermissionStatus -> checkPermissionStatus()
            is UiEvent.OpenNotificationSettings -> openNotificationSettings()
            is UiEvent.UpdateEndpointUrl -> updateEndpointUrl(event.url)
            is UiEvent.UpdateApiKey -> updateApiKey(event.key)
            is UiEvent.AddFilterPackage -> addFilterPackage(event.filter)
            is UiEvent.RemoveFilterPackage -> removeFilterPackage(event.packageName)
            is UiEvent.UpdateFilterPackage -> updateFilterPackage(event.filter)
            is UiEvent.UpdateForwardAllApps -> updateForwardAllApps(event.enabled)
            is UiEvent.ToggleApiKeyVisibility -> toggleApiKeyVisibility()
            is UiEvent.SaveSettings -> saveSettings()
            is UiEvent.ClearLogs -> clearLogs()
            is UiEvent.ShareLogs -> shareLogs()
            is UiEvent.TestSend -> testSend()
            is UiEvent.CopySettings -> copySettings()
            is UiEvent.DismissBatteryOptimizationDialog -> dismissBatteryOptimizationDialog()
            is UiEvent.OpenBatteryOptimizationSettings -> openBatteryOptimizationSettings()
            is UiEvent.UpdateNtfyEnabled -> updateNtfyEnabled(event.enabled)
            is UiEvent.UpdateNtfyUrl -> updateNtfyUrl(event.url)
            is UiEvent.UpdateNtfyTopic -> updateNtfyTopic(event.topic)
            is UiEvent.UpdateNtfyUseAuth -> updateNtfyUseAuth(event.useAuth)
            is UiEvent.UpdateNtfyToken -> updateNtfyToken(event.token)
            is UiEvent.ToggleNtfyTokenVisibility -> toggleNtfyTokenVisibility()
        }
    }
    
    private fun checkPermissionStatus() {
        val isGranted = NotificationCaptureService.isNotificationAccessGranted(
            context, 
            context.packageName
        )
        _uiState.value = _uiState.value.copy(isNotificationAccessGranted = isGranted)
        
        val status = if (isGranted) "Granted" else "Not Granted"
        viewModelScope.launch {
            notificationRepository.insertLog("Status izin akses notifikasi: $status", "INFO")
        }
    }
    
    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    private fun updateEndpointUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            endpointUrl = url,
            validationErrors = _uiState.value.validationErrors.copy(endpointUrl = null)
        )
    }
    
    private fun updateApiKey(key: String) {
        _uiState.value = _uiState.value.copy(
            apiKey = key,
            validationErrors = _uiState.value.validationErrors.copy(apiKey = null)
        )
    }
    
    private fun addFilterPackage(filter: PackageFilter) {
        val currentList = _uiState.value.filterPackages
        if (currentList.none { it.packageName.equals(filter.packageName, ignoreCase = true) }) {
            _uiState.value = _uiState.value.copy(
                filterPackages = currentList + filter,
                validationErrors = _uiState.value.validationErrors.copy(filterPackages = null)
            )
        }
    }
    
    private fun removeFilterPackage(packageName: String) {
        val currentList = _uiState.value.filterPackages
        _uiState.value = _uiState.value.copy(
            filterPackages = currentList.filter { !it.packageName.equals(packageName, ignoreCase = true) },
            validationErrors = _uiState.value.validationErrors.copy(filterPackages = null)
        )
    }
    
    private fun updateFilterPackage(filter: PackageFilter) {
        val currentList = _uiState.value.filterPackages
        _uiState.value = _uiState.value.copy(
            filterPackages = currentList.map {
                if (it.packageName.equals(filter.packageName, ignoreCase = true)) filter else it
            },
            validationErrors = _uiState.value.validationErrors.copy(filterPackages = null)
        )
    }
    
    private fun updateForwardAllApps(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(forwardAllApps = enabled)
    }
    
    private fun toggleApiKeyVisibility() {
        _uiState.value = _uiState.value.copy(isApiKeyVisible = !_uiState.value.isApiKeyVisible)
    }
    
    private fun updateNtfyEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            ntfyEnabled = enabled,
            validationErrors = _uiState.value.validationErrors.copy(ntfyTopic = null, ntfyUrl = null)
        )
    }
    
    private fun updateNtfyUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            ntfyUrl = url,
            validationErrors = _uiState.value.validationErrors.copy(ntfyUrl = null)
        )
    }
    
    private fun updateNtfyTopic(topic: String) {
        _uiState.value = _uiState.value.copy(
            ntfyTopic = topic,
            validationErrors = _uiState.value.validationErrors.copy(ntfyTopic = null)
        )
    }
    
    private fun updateNtfyUseAuth(useAuth: Boolean) {
        _uiState.value = _uiState.value.copy(
            ntfyUseAuth = useAuth,
            validationErrors = _uiState.value.validationErrors.copy(ntfyToken = null)
        )
    }
    
    private fun updateNtfyToken(token: String) {
        _uiState.value = _uiState.value.copy(
            ntfyToken = token,
            validationErrors = _uiState.value.validationErrors.copy(ntfyToken = null)
        )
    }
    
    private fun toggleNtfyTokenVisibility() {
        _uiState.value = _uiState.value.copy(isNtfyTokenVisible = !_uiState.value.isNtfyTokenVisible)
    }
    
    private fun saveSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val validationErrors = validateSettings()
                if (validationErrors.hasErrors()) {
                    _uiState.value = _uiState.value.copy(
                        validationErrors = validationErrors,
                        isLoading = false
                    )
                    return@launch
                }
                
                // Save settings
                val currentState = _uiState.value
                userPreferencesRepository.setEndpointUrl(currentState.endpointUrl)
                userPreferencesRepository.setFilterPackagesList(currentState.filterPackages)
                userPreferencesRepository.setForwardAllApps(currentState.forwardAllApps)
                userPreferencesRepository.setServiceEnabled(true)
                
                encryptedPreferencesManager.saveApiKey(
                    if (currentState.apiKey.isBlank()) null else currentState.apiKey
                )
                
                userPreferencesRepository.setNtfyEnabled(currentState.ntfyEnabled)
                userPreferencesRepository.setNtfyUrl(currentState.ntfyUrl)
                userPreferencesRepository.setNtfyTopic(currentState.ntfyTopic)
                userPreferencesRepository.setNtfyUseAuth(currentState.ntfyUseAuth)
                
                encryptedPreferencesManager.saveNtfyToken(
                    if (currentState.ntfyToken.isBlank()) null else currentState.ntfyToken
                )
                
                // Start services if permission is granted
                if (currentState.isNotificationAccessGranted) {
                    ForegroundService.startService(context)
                    NotificationRetryWorker.schedulePeriodicRetryWork(context)
                    
                    // Show battery optimization dialog if first successful save
                    if (!currentState.batteryOptimizationShown && !isIgnoringBatteryOptimizations()) {
                        _uiState.value = _uiState.value.copy(showBatteryOptimizationDialog = true)
                    }
                }
                
                notificationRepository.insertLog("Pengaturan berhasil disimpan", "SUCCESS")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error saving settings", e)
                notificationRepository.insertLog("Error menyimpan pengaturan: ${e.message}", "ERROR")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    private fun validateSettings(): ValidationErrors {
        val currentState = _uiState.value
        var errors = ValidationErrors()
        
        if (currentState.endpointUrl.isNotBlank()) {
            if (!NotificationUtils.isValidUrl(currentState.endpointUrl)) {
                errors = errors.copy(endpointUrl = "URL tidak valid")
            }
        }
        
        if (currentState.ntfyEnabled) {
            if (currentState.ntfyTopic.isBlank()) {
                errors = errors.copy(ntfyTopic = "Topic ntfy wajib diisi jika ntfy aktif")
            }
            if (currentState.ntfyUrl.isBlank()) {
                errors = errors.copy(ntfyUrl = "URL ntfy wajib diisi jika ntfy aktif")
            } else if (!NotificationUtils.isValidUrl(currentState.ntfyUrl)) {
                errors = errors.copy(ntfyUrl = "URL tidak valid")
            }
            if (currentState.ntfyUseAuth && currentState.ntfyToken.isBlank()) {
                errors = errors.copy(ntfyToken = "Token ntfy wajib diisi jika autentikasi diaktifkan")
            }
        }
        
        if (currentState.endpointUrl.isBlank() && !currentState.ntfyEnabled) {
            errors = errors.copy(endpointUrl = "Salah satu Endpoint URL atau ntfy harus diisi atau aktif")
        }
        
        if (!currentState.forwardAllApps && currentState.filterPackages.isEmpty()) {
            errors = errors.copy(filterPackages = "Filter package wajib diisi")
        }
        
        return errors
    }
    
    private fun clearLogs() {
        viewModelScope.launch {
            notificationRepository.clearAllLogs()
        }
    }
    
    private fun shareLogs() {
        viewModelScope.launch {
            try {
                val logText = notificationRepository.getLogsSummaryText()
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, logText)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Logs"))
            } catch (e: Exception) {
                notificationRepository.insertLog("Error berbagi logs: ${e.message}", "ERROR")
            }
        }
    }
    
    private fun testSend() {
        viewModelScope.launch {
            try {
                val result = notificationRepository.sendTestNotification()
                if (result.isSuccess) {
                    notificationRepository.insertLog("Test berhasil", "SUCCESS")
                } else {
                    notificationRepository.insertLog("Test gagal", "ERROR")
                }
            } catch (e: Exception) {
                notificationRepository.insertLog("Error test: ${e.message}", "ERROR")
            }
        }
    }
    
    private fun copySettings() {
        try {
            val currentState = _uiState.value
            val packagesStr = currentState.filterPackages.joinToString(", ") { "${it.appName} (${it.packageName})" }
            val settingsText = "Endpoint: ${currentState.endpointUrl}\nPackages: $packagesStr"
            
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Settings", settingsText)
            clipboard.setPrimaryClip(clip)
            
            viewModelScope.launch {
                notificationRepository.insertLog("Pengaturan disalin", "INFO")
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                notificationRepository.insertLog("Error menyalin: ${e.message}", "ERROR")
            }
        }
    }
    
    private fun dismissBatteryOptimizationDialog() {
        _uiState.value = _uiState.value.copy(showBatteryOptimizationDialog = false)
        viewModelScope.launch {
            userPreferencesRepository.setBatteryOptimizationShown(true)
        }
    }
    
    private fun openBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            dismissBatteryOptimizationDialog()
        } catch (e: Exception) {
            dismissBatteryOptimizationDialog()
        }
    }
    
    private fun isIgnoringBatteryOptimizations(): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } catch (e: Exception) {
            false
        }
    }
}

private fun ValidationErrors.hasErrors(): Boolean {
    return endpointUrl != null || apiKey != null || filterPackages != null ||
            ntfyUrl != null || ntfyTopic != null || ntfyToken != null
}
