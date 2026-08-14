package com.user_425.ewallet_relay.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.user_425.ewallet_relay.data.model.PackageFilter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_listener_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private object PreferencesKeys {
        val ENDPOINT_URL = stringPreferencesKey("endpoint_url")
        val FILTER_PACKAGES = stringPreferencesKey("filter_packages")
        val FORWARD_ALL_APPS = booleanPreferencesKey("forward_all_apps")
        val SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val BATTERY_OPTIMIZATION_SHOWN = booleanPreferencesKey("battery_optimization_shown")
        val NTFY_ENABLED = booleanPreferencesKey("ntfy_enabled")
        val NTFY_URL = stringPreferencesKey("ntfy_url")
        val NTFY_TOPIC = stringPreferencesKey("ntfy_topic")
        val NTFY_USE_AUTH = booleanPreferencesKey("ntfy_use_auth")
    }
    
    val endpointUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ENDPOINT_URL] ?: ""
    }
    
    private val gson = Gson()
    
    private val defaultFilters = listOf(
        PackageFilter("com.gojek.gopay", "GoPay"),
        PackageFilter("com.gojek.gopaymerchant", "GoPay Merchant"),
        PackageFilter("id.dana", "Dana"),
        PackageFilter("com.shopeepay.id", "ShopeePay"),
        PackageFilter("ovo.id", "OVO"),
        PackageFilter("com.telkom.mwallet", "LinkAja")
    )
    
    private fun getDefaultFilterPackagesJson(): String {
        return gson.toJson(defaultFilters)
    }

    fun parseFilterPackages(raw: String): List<PackageFilter> {
        if (raw.isBlank()) {
            return defaultFilters
        }
        if (raw.trim().startsWith("[")) {
            return try {
                val type = object : TypeToken<List<PackageFilter>>() {}.type
                gson.fromJson<List<PackageFilter>>(raw, type) ?: defaultFilters
            } catch (e: Exception) {
                defaultFilters
            }
        } else {
            // Migration legacy comma-separated packages
            return raw.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { pkg ->
                    val defaultMatch = defaultFilters.find { it.packageName.equals(pkg, ignoreCase = true) }
                    if (defaultMatch != null) {
                        defaultMatch
                    } else {
                        PackageFilter(packageName = pkg, appName = pkg, containWords = "")
                    }
                }
        }
    }

    val filterPackages: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FILTER_PACKAGES] ?: getDefaultFilterPackagesJson()
    }
    
    val filterPackagesList: Flow<List<PackageFilter>> = filterPackages.map { raw ->
        if (raw.isBlank()) defaultFilters else parseFilterPackages(raw)
    }
    
    val forwardAllApps: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FORWARD_ALL_APPS] ?: false
    }
    
    val serviceEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SERVICE_ENABLED] ?: false
    }
    
    val deviceId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEVICE_ID] ?: ""
    }
    
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_LAUNCH] ?: true
    }
    
    val batteryOptimizationShown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BATTERY_OPTIMIZATION_SHOWN] ?: false
    }
    
    val ntfyEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NTFY_ENABLED] ?: false
    }
    
    val ntfyUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NTFY_URL] ?: "https://ntfy.sh"
    }
    
    val ntfyTopic: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NTFY_TOPIC] ?: ""
    }
    
    val ntfyUseAuth: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NTFY_USE_AUTH] ?: false
    }
    
    suspend fun setEndpointUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENDPOINT_URL] = url
        }
    }
    
    suspend fun setFilterPackages(packages: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FILTER_PACKAGES] = packages
        }
    }
    
    suspend fun setFilterPackagesList(filters: List<PackageFilter>) {
        setFilterPackages(gson.toJson(filters))
    }
    
    suspend fun setForwardAllApps(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FORWARD_ALL_APPS] = enabled
        }
    }
    
    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVICE_ENABLED] = enabled
        }
    }
    
    suspend fun setDeviceId(deviceId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICE_ID] = deviceId
        }
    }
    
    suspend fun setFirstLaunch(isFirst: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = isFirst
        }
    }
    
    suspend fun setBatteryOptimizationShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BATTERY_OPTIMIZATION_SHOWN] = shown
        }
    }
    
    suspend fun setNtfyEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NTFY_ENABLED] = enabled
        }
    }
    
    suspend fun setNtfyUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NTFY_URL] = url
        }
    }
    
    suspend fun setNtfyTopic(topic: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NTFY_TOPIC] = topic
        }
    }
    
    suspend fun setNtfyUseAuth(useAuth: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NTFY_USE_AUTH] = useAuth
        }
    }
    
    suspend fun getEndpointUrlSync(): String {
        return endpointUrl.first()
    }
    
    suspend fun getFilterPackagesSync(): String {
        return filterPackages.first()
    }
    
    suspend fun getFilterPackagesListSync(): List<PackageFilter> {
        return filterPackagesList.first()
    }
    
    suspend fun getForwardAllAppsSync(): Boolean {
        return forwardAllApps.first()
    }
    
    suspend fun getDeviceIdSync(): String {
        return deviceId.first()
    }
    
    suspend fun getNtfyEnabledSync(): Boolean {
        return ntfyEnabled.first()
    }
    
    suspend fun getNtfyUrlSync(): String {
        return ntfyUrl.first()
    }
    
    suspend fun getNtfyTopicSync(): String {
        return ntfyTopic.first()
    }
    
    suspend fun getNtfyUseAuthSync(): Boolean {
        return ntfyUseAuth.first()
    }
}