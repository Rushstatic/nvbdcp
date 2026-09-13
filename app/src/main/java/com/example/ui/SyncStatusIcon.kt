package com.example.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SyncStatusIcon(isSynced: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 16.dp)
    ) {
        if (isSynced) {
            Icon(Icons.Filled.CloudDone, contentDescription = "Synced", tint = Color(0xFF4CAF50))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Synced", color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall)
        } else {
            Icon(Icons.Filled.CloudOff, contentDescription = "Unsynced", tint = Color(0xFFF44336))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Unsynced", color = Color(0xFFF44336), style = MaterialTheme.typography.bodySmall)
        }
    }
}
