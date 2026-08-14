package com.user_425.ewallet_relay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.user_425.ewallet_relay.ui.state.NotificationListenerUiState
import com.user_425.ewallet_relay.ui.state.UiEvent
import com.user_425.ewallet_relay.ui.state.ValidationErrors
import com.user_425.ewallet_relay.ui.theme.NotificationListenerTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun SettingsForm(
    uiState: NotificationListenerUiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Pengaturan Koneksi",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Endpoint URL
            OutlinedTextField(
                value = uiState.endpointUrl,
                onValueChange = { onEvent(UiEvent.UpdateEndpointUrl(it)) },
                label = { Text("Endpoint URL") },
                placeholder = { Text("https://api.example.com/webhook") },
                isError = uiState.validationErrors.endpointUrl != null,
                supportingText = uiState.validationErrors.endpointUrl?.let { { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
            
            // API Key
            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = { onEvent(UiEvent.UpdateApiKey(it)) },
                label = { Text("API Key (Opsional)") },
                placeholder = { Text("Masukkan API key") },
                isError = uiState.validationErrors.apiKey != null,
                supportingText = uiState.validationErrors.apiKey?.let { { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (uiState.isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = { onEvent(UiEvent.ToggleApiKeyVisibility) }
                    ) {
                        Icon(
                            imageVector = if (uiState.isApiKeyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (uiState.isApiKeyVisible) "Sembunyikan API Key" else "Tampilkan API Key",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            
            // Filter Packages
            OutlinedTextField(
                value = uiState.filterPackages,
                onValueChange = { onEvent(UiEvent.UpdateFilterPackages(it)) },
                label = { Text("Filter Package (Pemisah Koma)") },
                placeholder = { Text("id.dana, com.whatsapp") },
                isError = uiState.validationErrors.filterPackages != null,
                supportingText = uiState.validationErrors.filterPackages?.let { { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )
            
            // Forward all apps toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Forward Semua Aplikasi",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Kirim notifikasi semua app, abaikan filter",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.forwardAllApps,
                    onCheckedChange = { onEvent(UiEvent.UpdateForwardAllApps(it)) }
                )
            }
            
            // Save button
            Button(
                onClick = { onEvent(UiEvent.SaveSettings) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Menyimpan...", style = MaterialTheme.typography.labelLarge)
                } else {
                    Text("Simpan Pengaturan", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Settings Form - Empty")
@Composable
fun SettingsFormPreviewEmpty() {
    NotificationListenerTheme {
        SettingsForm(
            uiState = NotificationListenerUiState(
                endpointUrl = "",
                apiKey = "",
                filterPackages = "",
                forwardAllApps = false,
                isLoading = false,
                validationErrors = ValidationErrors(),
                isApiKeyVisible = false
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Form - Filled")
@Composable
fun SettingsFormPreviewFilled() {
    NotificationListenerTheme {
        SettingsForm(
            uiState = NotificationListenerUiState(
                endpointUrl = "https://api.example.com/webhook",
                apiKey = "sample-api-key-123456",
                filterPackages = "id.dana, com.whatsapp, com.tokopedia",
                forwardAllApps = true,
                isLoading = false,
                validationErrors = ValidationErrors(),
                isApiKeyVisible = false
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Form - Loading")
@Composable
fun SettingsFormPreviewLoading() {
    NotificationListenerTheme {
        SettingsForm(
            uiState = NotificationListenerUiState(
                endpointUrl = "https://api.example.com/webhook",
                apiKey = "sample-api-key",
                filterPackages = "id.dana",
                forwardAllApps = false,
                isLoading = true,
                validationErrors = ValidationErrors(),
                isApiKeyVisible = false
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Form - With Errors")
@Composable
fun SettingsFormPreviewErrors() {
    NotificationListenerTheme {
        SettingsForm(
            uiState = NotificationListenerUiState(
                endpointUrl = "invalid-url",
                apiKey = "",
                filterPackages = "invalid package name",
                forwardAllApps = false,
                isLoading = false,
                validationErrors = ValidationErrors(
                    endpointUrl = "URL tidak valid",
                    filterPackages = "Format package tidak valid"
                ),
                isApiKeyVisible = false
            ),
            onEvent = {}
        )
    }
}