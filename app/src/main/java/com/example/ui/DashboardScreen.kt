package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val reports by viewModel.currentMonthReports.collectAsStateWithLifecycle()
    val monthStr by viewModel.currentMonthStr.collectAsStateWithLifecycle()

    var totalSamples = 0
    var activeSamples = 0
    var passiveSamples = 0

    reports.forEach { report ->
        totalSamples += report.total
        if (report.designation.contains("आरोग्य सेवक") || report.designation.contains("आरोग्य सेविका") || report.designation.contains("आशा")) {
            activeSamples += report.total
        } else {
            passiveSamples += report.total
        }
    }

    val activePercent = if (totalSamples > 0) (activeSamples * 100) / totalSamples else 0
    val passivePercent = if (totalSamples > 0) (passiveSamples * 100) / totalSamples else 0

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("मासिक आढावा (चालू महिना)") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Current Month: $monthStr", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "एकूण नमुने",
                    value = totalSamples.toString(),
                    subtitle = "Total Samples",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "ॲक्टिव्ह",
                    value = activeSamples.toString(),
                    subtitle = "$activePercent%",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "पॅसिव्ह",
                    value = passiveSamples.toString(),
                    subtitle = "$passivePercent%",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Simplified Chart representation since we don't have a chart library installed
            Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text("Chart Visualization", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Active: $activeSamples ($activePercent%)", color = MaterialTheme.colorScheme.primary)
                    Text("Passive: $passiveSamples ($passivePercent%)", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
