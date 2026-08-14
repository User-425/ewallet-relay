package com.user_425.ewallet_relay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.user_425.ewallet_relay.data.database.entity.NotificationLogEntity
import com.user_425.ewallet_relay.ui.theme.NotificationListenerTheme
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Composable
fun LogsSection(
    logs: List<NotificationLogEntity>,
    onClearLogs: () -> Unit,
    onShareLogs: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Riwayat Aktivitas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Menampilkan 100 log terakhir",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onShareLogs,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Bagikan Log",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan", style = MaterialTheme.typography.labelMedium)
                    }
                    
                    OutlinedButton(
                        onClick = onClearLogs,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Bersihkan Log",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            
            // Console Terminal container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF090D16) // Obsidian Dark Console
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF1E293B)
                )
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat aktivitas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B) // Slate text
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = true
                    ) {
                        items(logs) { log ->
                            LogItem(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItem(
    log: NotificationLogEntity,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val time = dateFormat.format(Date(log.timestamp))
    
    // Status capsules colors (Soft BG vs Dark text)
    val badgeBg = when (log.type) {
        "SUCCESS" -> Color(0xFFD1FAE5) // Emerald100
        "ERROR" -> Color(0xFFFFE4E6)   // Rose100
        "INFO" -> Color(0xFFF1F5F9)    // Slate100
        "QUEUED" -> Color(0xFFFEF3C7)  // Amber100
        else -> Color(0xFFE2E8F0)      // Slate200
    }
    
    val badgeText = when (log.type) {
        "SUCCESS" -> Color(0xFF065F46) // Emerald800
        "ERROR" -> Color(0xFF9F1239)   // Rose800
        "INFO" -> Color(0xFF475569)    // Slate600
        "QUEUED" -> Color(0xFFB45309)  // Amber700
        else -> Color(0xFF1E293B)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Log Timestamp
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF475569),
            modifier = Modifier.padding(top = 2.dp)
        )
        
        // Log Style Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeBg)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = log.type,
                color = badgeText,
                style = MaterialTheme.typography.labelSmall
            )
        }
        
        // Raw Log Message (clean light gray text)
        Text(
            text = log.message,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFE2E8F0),
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, name = "Logs Section - Empty")
@Composable
fun LogsSectionPreviewEmpty() {
    NotificationListenerTheme {
        LogsSection(
            logs = emptyList(),
            onClearLogs = {},
            onShareLogs = {}
        )
    }
}

@Preview(showBackground = true, name = "Logs Section - With Logs")
@Composable
fun LogsSectionPreviewWithLogs() {
    NotificationListenerTheme {
        LogsSection(
            logs = listOf(
                NotificationLogEntity(
                    id = 1,
                    timestamp = System.currentTimeMillis(),
                    message = "SUCCESS: Notification sent successfully to webhook",
                    type = "SUCCESS"
                ),
                NotificationLogEntity(
                    id = 2,
                    timestamp = System.currentTimeMillis() - 60000,
                    message = "ERROR: Failed to send notification - connection timeout",
                    type = "ERROR"
                ),
                NotificationLogEntity(
                    id = 3,
                    timestamp = System.currentTimeMillis() - 120000,
                    message = "INFO: Service started and listening",
                    type = "INFO"
                ),
                NotificationLogEntity(
                    id = 4,
                    timestamp = System.currentTimeMillis() - 180000,
                    message = "QUEUED: Added to retry queue",
                    type = "QUEUED"
                )
            ),
            onClearLogs = {},
            onShareLogs = {}
        )
    }
}

@Preview(showBackground = true, name = "Log Item - Success")
@Composable
fun LogItemPreviewSuccess() {
    NotificationListenerTheme {
        LogItem(
            log = NotificationLogEntity(
                id = 1,
                timestamp = System.currentTimeMillis(),
                message = "SUCCESS: Notification sent successfully",
                type = "SUCCESS"
            )
        )
    }
}

@Preview(showBackground = true, name = "Log Item - Error")
@Composable
fun LogItemPreviewError() {
    NotificationListenerTheme {
        LogItem(
            log = NotificationLogEntity(
                id = 2,
                timestamp = System.currentTimeMillis(),
                message = "ERROR: Failed to send notification - network error",
                type = "ERROR"
            )
        )
    }
}