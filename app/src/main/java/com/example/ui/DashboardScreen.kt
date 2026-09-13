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
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlin.random.Random

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
            
            // Generate mock data for the 30-day trend chart
            val positiveEntries = remember { List(30) { entryOf(it, Random.nextInt(0, 5)) } }
            val negativeEntries = remember { List(30) { entryOf(it, Random.nextInt(10, 40)) } }
            val chartEntryModel = remember(positiveEntries, negativeEntries) { entryModelOf(positiveEntries, negativeEntries) }

            Text("30-Day Trend (Positives vs Negatives)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            Card(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                Chart(
                    chart = lineChart(),
                    model = chartEntryModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
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
