package com.user_425.ewallet_relay.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.user_425.ewallet_relay.data.database.entity.NotificationLogEntity
import com.user_425.ewallet_relay.ui.components.DebugUtilities
import com.user_425.ewallet_relay.ui.components.LogsSection
import com.user_425.ewallet_relay.ui.components.PermissionStatusCard
import com.user_425.ewallet_relay.ui.components.SettingsForm
import com.user_425.ewallet_relay.ui.state.NotificationListenerUiState
import com.user_425.ewallet_relay.ui.state.UiEvent
import com.user_425.ewallet_relay.ui.state.ValidationErrors
import com.user_425.ewallet_relay.ui.theme.NotificationListenerTheme
import com.user_425.ewallet_relay.ui.viewmodel.NotificationListenerViewModel

import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings

enum class AppScreen {
    Dashboard,
    Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListenerScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationListenerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf(AppScreen.Dashboard) }
    
    NotificationListenerScreenContent(
        uiState = uiState,
        onEvent = viewModel::handleEvent,
        currentScreen = currentScreen,
        onNavigate = { currentScreen = it },
        modifier = modifier
    )
}

// Preview with permission granted
@Preview(showBackground = true, name = "Permission Granted")
@Composable
fun NotificationListenerScreenPreviewGranted() {
    NotificationListenerTheme {
        NotificationListenerScreenContent(
            uiState = NotificationListenerUiState(
                isNotificationAccessGranted = true,
                endpointUrl = "https://api.example.com/webhook",
                apiKey = "sample-api-key",
                filterPackages = "id.dana, com.whatsapp",
                forwardAllApps = false,
                logs = listOf(
                    NotificationLogEntity(
                        id = 1,
                        timestamp = System.currentTimeMillis(),
                        message = "SUCCESS: Notification sent successfully",
                        type = "SUCCESS"
                    ),
                    NotificationLogEntity(
                        id = 2,
                        timestamp = System.currentTimeMillis() - 60000,
                        message = "INFO: Service started",
                        type = "INFO"
                    )
                ),
                isLoading = false,
                validationErrors = ValidationErrors(),
                showBatteryOptimizationDialog = false,
                isApiKeyVisible = false
            ),
            currentScreen = AppScreen.Dashboard,
            onNavigate = {},
            onEvent = {}
        )
    }
}

// Preview with permission denied
@Preview(showBackground = true, name = "Permission Denied")
@Composable
fun NotificationListenerScreenPreviewDenied() {
    NotificationListenerTheme {
        NotificationListenerScreenContent(
            uiState = NotificationListenerUiState(
                isNotificationAccessGranted = false,
                endpointUrl = "",
                apiKey = "",
                filterPackages = "",
                forwardAllApps = false,
                logs = emptyList(),
                isLoading = false,
                validationErrors = ValidationErrors(),
                showBatteryOptimizationDialog = false,
                isApiKeyVisible = false
            ),
            currentScreen = AppScreen.Dashboard,
            onNavigate = {},
            onEvent = {}
        )
    }
}

// Dark theme preview
@Preview(showBackground = true, name = "Dark Theme")
@Composable
fun NotificationListenerScreenPreviewDark() {
    NotificationListenerTheme(darkTheme = true) {
        NotificationListenerScreenContent(
            uiState = NotificationListenerUiState(
                isNotificationAccessGranted = true,
                endpointUrl = "https://api.example.com/webhook",
                apiKey = "sample-api-key",
                filterPackages = "id.dana, com.whatsapp",
                forwardAllApps = true,
                logs = listOf(
                    NotificationLogEntity(
                        id = 1,
                        timestamp = System.currentTimeMillis(),
                        message = "ERROR: Failed to send notification",
                        type = "ERROR"
                    )
                ),
                isLoading = true,
                validationErrors = ValidationErrors(),
                showBatteryOptimizationDialog = false,
                isApiKeyVisible = true
            ),
            currentScreen = AppScreen.Dashboard,
            onNavigate = {},
            onEvent = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListenerScreenContent(
    uiState: NotificationListenerUiState,
    onEvent: (UiEvent) -> Unit,
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    // Battery optimization dialog
    if (uiState.showBatteryOptimizationDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(UiEvent.DismissBatteryOptimizationDialog) },
            title = { Text("Optimasi Baterai") },
            text = { 
                Text("Untuk menjaga layanan tetap aktif, disarankan untuk menonaktifkan optimasi baterai untuk aplikasi ini.")
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(UiEvent.OpenBatteryOptimizationSettings) }
                ) {
                    Text("Buka Pengaturan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onEvent(UiEvent.DismissBatteryOptimizationDialog) }
                ) {
                    Text("Nanti")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (currentScreen == AppScreen.Dashboard) "EWallet Relay" else "Pengaturan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
                    if (currentScreen == AppScreen.Settings) {
                        IconButton(onClick = { onNavigate(AppScreen.Dashboard) }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Kembali ke Dashboard",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    if (currentScreen == AppScreen.Dashboard) {
                        IconButton(onClick = { onNavigate(AppScreen.Settings) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Buka Pengaturan",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.Dashboard -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Permission Status
                        PermissionStatusCard(
                            isGranted = uiState.isNotificationAccessGranted,
                            onCheckStatus = { onEvent(UiEvent.CheckPermissionStatus) },
                            onOpenSettings = { onEvent(UiEvent.OpenNotificationSettings) }
                        )

                        // Logs Section
                        LogsSection(
                            logs = uiState.logs,
                            onClearLogs = { onEvent(UiEvent.ClearLogs) },
                            onShareLogs = { onEvent(UiEvent.ShareLogs) }
                        )

                        // Debug Utilities
                        DebugUtilities(
                            onTestSend = { onEvent(UiEvent.TestSend) },
                            onCopySettings = { onEvent(UiEvent.CopySettings) }
                        )
                    }
                }
                AppScreen.Settings -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SettingsForm(
                            uiState = uiState,
                            onEvent = onEvent
                        )
                    }
                }
            }
        }
    }
}