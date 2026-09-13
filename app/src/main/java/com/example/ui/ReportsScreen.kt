package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val csvData = viewModel.generateCsvData()
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        out.write(csvData.toByteArray())
                    }
                    Toast.makeText(context, "Data saved successfully to Drive/Device!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    Toast.makeText(context, "Importing data...", Toast.LENGTH_SHORT).show()
                    val count = viewModel.importCsvData(it, context)
                    Toast.makeText(context, "Successfully imported $count records!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var webViewRef by remember { mutableStateOf<android.webkit.WebView?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("उपलब्ध अहवालांची यादी") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ReportCard(
                icon = "📂",
                title = "डेटा इम्पोर्ट (Import from CSV)",
                desc = "जुन्या Excel/CSV फाईलमधून डेटा सिस्टीममध्ये लोड करा.",
                onClick = {
                    importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                }
            )
            ReportCard(
                icon = "💾",
                title = "डेटा एक्सपोर्ट (Save to Google Drive)",
                desc = "सर्व डेटा CSV फॉरमॅटमध्ये Google Drive किंवा फोनवर सेव्ह करा.",
                onClick = {
                    exportLauncher.launch("Malaria_Report_Backup.csv")
                }
            )
            ReportCard(
                icon = "📊",
                title = "मासिक अहवाल (Monthly PDF)",
                desc = "गावनिहाय आणि उपकेंद्रनिहाय महिन्याचा अहवाल. (PDF तयार करा)",
                onClick = {
                    coroutineScope.launch {
                        try {
                            Toast.makeText(context, "Generating PDF...", Toast.LENGTH_SHORT).show()
                            val htmlContent = viewModel.generateMonthlyReportHtml()
                            val webView = android.webkit.WebView(context)
                            webViewRef = webView // Keep strong reference
                            webView.webViewClient = object : android.webkit.WebViewClient() {
                                override fun onPageFinished(view: android.webkit.WebView, url: String) {
                                    val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
                                    val printAdapter = view.createPrintDocumentAdapter("Malaria_Monthly_Report")
                                    printManager.print("Monthly Report", printAdapter, android.print.PrintAttributes.Builder().build())
                                }
                            }
                            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
            ReportCard(
                icon = "🖨️",
                title = "दैनिक पत्र (Letter Print)",
                desc = "विशिष्ट तारखेचे रक्त नमुने तपासणी पत्र."
            )
            ReportCard(
                icon = "📗",
                title = "कर्मचारी नोंदवही (Register)",
                desc = "चालू वर्षातील कर्मचाऱ्यांचा वैयक्तिक अहवाल."
            )
            ReportCard(
                icon = "⚠️",
                title = "कारणे दाखवा नोटीस",
                desc = "उद्दिष्ट पूर्ण न केलेल्या कर्मचाऱ्यांसाठी नोटीस."
            )
            ReportCard(
                icon = "📉",
                title = "कमी कामगिरी अहवाल",
                desc = "निरंक आणि ७५% पेक्षा कमी कामगिरीची यादी."
            )
        }
    }
}

@Composable
fun ReportCard(icon: String, title: String, desc: String, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(icon, style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
