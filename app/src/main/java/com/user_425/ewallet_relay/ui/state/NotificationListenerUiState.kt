package com.user_425.ewallet_relay.ui.state

import com.user_425.ewallet_relay.data.database.entity.NotificationLogEntity
import com.user_425.ewallet_relay.data.model.PackageFilter

data class NotificationListenerUiState(
    val isLoading: Boolean = false,
    val isNotificationAccessGranted: Boolean = false,
    val endpointUrl: String = "",
    val apiKey: String = "",
    val filterPackages: List<PackageFilter> = emptyList(),
    val forwardAllApps: Boolean = false,
    val serviceEnabled: Boolean = false,
    val logs: List<NotificationLogEntity> = emptyList(),
    val isApiKeyVisible: Boolean = false,
    val validationErrors: ValidationErrors = ValidationErrors(),
    val showBatteryOptimizationDialog: Boolean = false,
    val batteryOptimizationShown: Boolean = false,
    val ntfyEnabled: Boolean = false,
    val ntfyUrl: String = "https://ntfy.sh",
    val ntfyTopic: String = "",
    val ntfyUseAuth: Boolean = false,
    val ntfyToken: String = "",
    val isNtfyTokenVisible: Boolean = false,
    val showSaveSuccessDialog: Boolean = false
)

data class ValidationErrors(
    val endpointUrl: String? = null,
    val apiKey: String? = null,
    val filterPackages: String? = null,
    val ntfyUrl: String? = null,
    val ntfyTopic: String? = null,
    val ntfyToken: String? = null
)

sealed class UiEvent {
    object CheckPermissionStatus : UiEvent()
    object OpenNotificationSettings : UiEvent()
    data class UpdateEndpointUrl(val url: String) : UiEvent()
    data class UpdateApiKey(val key: String) : UiEvent()
    data class AddFilterPackage(val filter: PackageFilter) : UiEvent()
    data class RemoveFilterPackage(val packageName: String) : UiEvent()
    data class UpdateFilterPackage(val filter: PackageFilter) : UiEvent()
    data class UpdateForwardAllApps(val enabled: Boolean) : UiEvent()
    object ToggleApiKeyVisibility : UiEvent()
    object SaveSettings : UiEvent()
    object DismissSaveSuccessDialog : UiEvent()
    object ClearLogs : UiEvent()
    object ShareLogs : UiEvent()
    object TestSend : UiEvent()
    object CopySettings : UiEvent()
    object DismissBatteryOptimizationDialog : UiEvent()
    object OpenBatteryOptimizationSettings : UiEvent()
    data class UpdateNtfyEnabled(val enabled: Boolean) : UiEvent()
    data class UpdateNtfyUrl(val url: String) : UiEvent()
    data class UpdateNtfyTopic(val topic: String) : UiEvent()
    data class UpdateNtfyUseAuth(val useAuth: Boolean) : UiEvent()
    data class UpdateNtfyToken(val token: String) : UiEvent()
    object ToggleNtfyTokenVisibility : UiEvent()
}