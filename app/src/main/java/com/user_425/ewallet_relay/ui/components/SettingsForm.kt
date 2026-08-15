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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.user_425.ewallet_relay.data.model.PackageFilter

@Composable
fun SettingsForm(
    uiState: NotificationListenerUiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingFilter by remember { mutableStateOf<PackageFilter?>(null) }
    var tempAppName by remember { mutableStateOf("") }
    var tempPackageName by remember { mutableStateOf("") }
    var tempContainWords by remember { mutableStateOf("") }
    var tempEnabled by remember { mutableStateOf(true) }
    var dialogError by remember { mutableStateOf<String?>(null) }
    // Collapsed by default so Save stays reachable; auto-opens when list is empty
    // (drawer is only worth collapsing when there's something to hide)
    var filterSectionExpanded by remember { mutableStateOf(uiState.filterPackages.isEmpty()) }

    val onAddClicked = {
        editingFilter = null
        tempAppName = ""
        tempPackageName = ""
        tempContainWords = ""
        tempEnabled = true
        dialogError = null
        showDialog = true
    }

    val onEditClicked = { filter: PackageFilter ->
        editingFilter = filter
        tempAppName = filter.appName
        tempPackageName = filter.packageName
        tempContainWords = filter.containWords
        tempEnabled = filter.enabled
        dialogError = null
        showDialog = true
    }

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
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // NTFY Configuration Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kirim via ntfy",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Relay notifikasi ke topic ntfy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.ntfyEnabled,
                    onCheckedChange = { onEvent(UiEvent.UpdateNtfyEnabled(it)) }
                )
            }
            
            if (uiState.ntfyEnabled) {
                // ntfy Server URL
                OutlinedTextField(
                    value = uiState.ntfyUrl,
                    onValueChange = { onEvent(UiEvent.UpdateNtfyUrl(it)) },
                    label = { Text("ntfy Server URL") },
                    placeholder = { Text("https://ntfy.sh") },
                    isError = uiState.validationErrors.ntfyUrl != null,
                    supportingText = uiState.validationErrors.ntfyUrl?.let { { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                // ntfy Topic
                OutlinedTextField(
                    value = uiState.ntfyTopic,
                    onValueChange = { onEvent(UiEvent.UpdateNtfyTopic(it)) },
                    label = { Text("ntfy Topic") },
                    placeholder = { Text("nama_topic") },
                    isError = uiState.validationErrors.ntfyTopic != null,
                    supportingText = uiState.validationErrors.ntfyTopic?.let { { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // ntfy Use Auth Toggle Row
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
                            text = "Gunakan Autentikasi ntfy",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Aktifkan jika topic/server memerlukan token akses",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.ntfyUseAuth,
                        onCheckedChange = { onEvent(UiEvent.UpdateNtfyUseAuth(it)) }
                    )
                }

                // ntfy Access Token (if auth is enabled)
                if (uiState.ntfyUseAuth) {
                    OutlinedTextField(
                        value = uiState.ntfyToken,
                        onValueChange = { onEvent(UiEvent.UpdateNtfyToken(it)) },
                        label = { Text("ntfy Access Token") },
                        placeholder = { Text("Masukkan token ntfy") },
                        isError = uiState.validationErrors.ntfyToken != null,
                        supportingText = uiState.validationErrors.ntfyToken?.let { { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (uiState.isNtfyTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { onEvent(UiEvent.ToggleNtfyTokenVisibility) }
                            ) {
                                Icon(
                                    imageVector = if (uiState.isNtfyTokenVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (uiState.isNtfyTokenVisible) "Sembunyikan Token" else "Tampilkan Token",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Filter Packages — collapsible accordion so Save stays reachable with many filters
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                // Tappable header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { filterSectionExpanded = !filterSectionExpanded }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aplikasi yang Difilter",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!filterSectionExpanded) {
                            val count = uiState.filterPackages.size
                            val activeCount = uiState.filterPackages.count { it.enabled }
                            Text(
                                text = if (count == 0) "Belum ada aplikasi"
                                       else "$activeCount aktif dari $count aplikasi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (filterSectionExpanded) Icons.Default.KeyboardArrowUp
                                      else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (filterSectionExpanded) "Tutup" else "Buka",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (filterSectionExpanded) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState.validationErrors.filterPackages != null) {
                            Text(
                                text = uiState.validationErrors.filterPackages,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (uiState.filterPackages.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada aplikasi yang difilter.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            uiState.filterPackages.forEach { filter ->
                                val borderAccentColor = if (filter.enabled)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .fillMaxHeight()
                                                .background(borderAccentColor)
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 12.dp, vertical = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = filter.appName,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Switch(
                                                    checked = filter.enabled,
                                                    onCheckedChange = { isChecked ->
                                                        onEvent(UiEvent.UpdateFilterPackage(filter.copy(enabled = isChecked)))
                                                    }
                                                )
                                            }
                                            Text(
                                                text = filter.packageName,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                if (filter.containWords.isNotBlank()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                                shape = RoundedCornerShape(6.dp)
                                                            )
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = "kata kunci: ${filter.containWords}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(
                                                        onClick = { onEditClicked(filter) },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Edit",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { onEvent(UiEvent.RemoveFilterPackage(filter.packageName)) },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Hapus",
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        FilledTonalButton(
                            onClick = onAddClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tambah Aplikasi", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            
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

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (editingFilter == null) "Tambah Filter Aplikasi" else "Edit Filter Aplikasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (dialogError != null) {
                        Text(
                            text = dialogError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    OutlinedTextField(
                        value = tempAppName,
                        onValueChange = { tempAppName = it },
                        label = { Text("Nama Aplikasi (e.g. GoPay)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = tempPackageName,
                        onValueChange = { tempPackageName = it },
                        label = { Text("Package Name (e.g. com.gojek.gopay)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = editingFilter == null
                    )

                    OutlinedTextField(
                        value = tempContainWords,
                        onValueChange = { tempContainWords = it },
                        label = { Text("Filter Detail Kata (Opsional, pemisah koma)") },
                        placeholder = { Text("e.g. transfer, dana masuk") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        supportingText = { Text("Kosongkan untuk meneruskan semua notifikasi dari aplikasi ini") }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Aktifkan Filter Aplikasi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = tempEnabled,
                            onCheckedChange = { tempEnabled = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempAppName.isBlank() || tempPackageName.isBlank()) {
                            dialogError = "Nama dan Package Name wajib diisi"
                            return@TextButton
                        }
                        if (!tempPackageName.contains(".")) {
                            dialogError = "Format Package Name tidak valid"
                            return@TextButton
                        }
                        
                        val newFilter = PackageFilter(
                            packageName = tempPackageName.trim(),
                            appName = tempAppName.trim(),
                            containWords = tempContainWords.trim(),
                            enabled = tempEnabled
                        )
                        
                        if (editingFilter == null) {
                            onEvent(UiEvent.AddFilterPackage(newFilter))
                        } else {
                            onEvent(UiEvent.UpdateFilterPackage(newFilter))
                        }
                        showDialog = false
                    }
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
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
                filterPackages = emptyList(),
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
                filterPackages = listOf(
                    PackageFilter("id.dana", "Dana", ""),
                    PackageFilter("com.gojek.gopay", "GoPay", "transfer, sukses")
                ),
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
                filterPackages = listOf(
                    PackageFilter("id.dana", "Dana", "")
                ),
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
                filterPackages = emptyList(),
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