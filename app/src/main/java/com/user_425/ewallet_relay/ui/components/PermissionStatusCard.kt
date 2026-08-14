package com.user_425.ewallet_relay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.user_425.ewallet_relay.ui.theme.NotificationListenerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionStatusCard(
    isGranted: Boolean,
    onCheckStatus: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stripeColor = if (isGranted) {
        Color(0xFF059669) // Emerald600
    } else {
        Color(0xFFE11D48) // Rose600
    }

    val iconContainerBg = if (isGranted) {
        Color(0xFFD1FAE5) // Emerald100
    } else {
        Color(0xFFFFE4E6) // Rose100
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
        Row(
            modifier = Modifier.height(IntrinsicSize.Max)
        ) {
            // Left Status Accent Stripe
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(stripeColor)
            )

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Elevated status icon circle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(iconContainerBg)
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = stripeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isGranted) "Akses Notifikasi Aktif" else "Akses Notifikasi Diperlukan",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isGranted) {
                                "Aplikasi siap menyaring dan meneruskan notifikasi Anda ke server target."
                            } else {
                                "Izin diperlukan agar aplikasi dapat membaca notifikasi sistem dan meneruskannya."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isGranted) {
                        Button(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .weight(1.5f)
                                .heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = stripeColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Pengaturan", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = onCheckStatus,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(
                                enabled = true
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cek Status", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kelola Izin", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = onCheckStatus,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cek Status", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Permission Granted")
@Composable
fun PermissionStatusCardPreviewGranted() {
    NotificationListenerTheme {
        PermissionStatusCard(
            isGranted = true,
            onCheckStatus = {},
            onOpenSettings = {}
        )
    }
}

@Preview(showBackground = true, name = "Permission Denied")
@Composable
fun PermissionStatusCardPreviewDenied() {
    NotificationListenerTheme {
        PermissionStatusCard(
            isGranted = false,
            onCheckStatus = {},
            onOpenSettings = {}
        )
    }
}

@Preview(showBackground = true, name = "Permission Granted - Dark")
@Composable
fun PermissionStatusCardPreviewGrantedDark() {
    NotificationListenerTheme(darkTheme = true) {
        PermissionStatusCard(
            isGranted = true,
            onCheckStatus = {},
            onOpenSettings = {}
        )
    }
}