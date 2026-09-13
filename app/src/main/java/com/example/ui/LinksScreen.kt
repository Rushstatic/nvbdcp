package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ImportantLink(val name: String, val desc: String, val url: String, val icon: String)

val dummyLinks = listOf(
    ImportantLink("Index Report Entry", "इंडेक्स रिपोर्ट डेटा भरण्यासाठी", "https://script.google.com/macros/s/...", "📝"),
    ImportantLink("Gov Health Portal", "राष्ट्रीय आरोग्य अभियान", "https://nhm.gov.in/", "🌐"),
    ImportantLink("Drive Folder", "मासिक अहवाल फोल्डर", "https://drive.google.com/", "📁")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinksScreen() {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("महत्त्वाच्या लिंक्स") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            dummyLinks.forEach { link ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                        context.startActivity(intent)
                    }) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(link.icon, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(link.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(link.desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("↗️", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
