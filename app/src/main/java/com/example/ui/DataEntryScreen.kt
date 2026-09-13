package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.VillageEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEntryScreen(viewModel: MainViewModel) {
    val employees by viewModel.allEmployees.collectAsStateWithLifecycle()
    
    var dateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var selectedUpkendra by remember { mutableStateOf<String?>(null) }
    var selectedEmployee by remember { mutableStateOf<com.example.data.Employee?>(null) }
    var bundleNo by remember { mutableStateOf("") }
    var pasun by remember { mutableStateOf("") }
    var paraynt by remember { mutableStateOf("") }
    
    val upkendras = remember(employees) { employees.map { it.upkendra }.distinct().sorted() }
    val filteredEmployees = remember(employees, selectedUpkendra) {
        if (selectedUpkendra != null) employees.filter { it.upkendra == selectedUpkendra } else employees
    }
    
    val total = if (pasun.isNotEmpty() && paraynt.isNotEmpty()) {
        (paraynt.toIntOrNull() ?: 0) - (pasun.toIntOrNull() ?: 0) + 1
    } else 0
    
    var villages by remember { mutableStateOf(listOf<VillageEntry>()) }

    var villageName by remember { mutableStateOf("") }
    var sampleCount by remember { mutableStateOf("") }
    var maleCount by remember { mutableStateOf("") }

    val isSynced by viewModel.isSynced.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("नवीन रक्त नमुने नोंदणी") },
                actions = { SyncStatusIcon(isSynced) }
            )
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
            OutlinedTextField(
                value = dateStr,
                onValueChange = { dateStr = it },
                label = { Text("निवडा तारीख (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = bundleNo,
                onValueChange = { bundleNo = it },
                label = { Text("बंडल क्रमांक") },
                modifier = Modifier.fillMaxWidth()
            )

            // Subcenter Dropdown
            var expandedUpkendra by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedUpkendra,
                onExpandedChange = { expandedUpkendra = !expandedUpkendra }
            ) {
                OutlinedTextField(
                    value = selectedUpkendra ?: "Select Subcenter",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("उपकेंद्र निवडा") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUpkendra) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedUpkendra,
                    onDismissRequest = { expandedUpkendra = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Subcenters") },
                        onClick = {
                            selectedUpkendra = null
                            selectedEmployee = null
                            expandedUpkendra = false
                        }
                    )
                    upkendras.forEach { upkendra ->
                        DropdownMenuItem(
                            text = { Text(upkendra) },
                            onClick = {
                                selectedUpkendra = upkendra
                                selectedEmployee = null // reset employee when subcenter changes
                                expandedUpkendra = false
                            }
                        )
                    }
                }
            }

            // Employee Dropdown
            var expandedEmployee by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedEmployee,
                onExpandedChange = { expandedEmployee = !expandedEmployee }
            ) {
                OutlinedTextField(
                    value = selectedEmployee?.name ?: "Select Employee",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("कर्मचारी नाव") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEmployee) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedEmployee,
                    onDismissRequest = { expandedEmployee = false }
                ) {
                    filteredEmployees.forEach { emp ->
                        DropdownMenuItem(
                            text = { Text("${emp.name} - ${emp.upkendra}") },
                            onClick = {
                                selectedEmployee = emp
                                // auto-select upkendra if it wasn't selected
                                if (selectedUpkendra == null) {
                                    selectedUpkendra = emp.upkendra
                                }
                                expandedEmployee = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = pasun,
                    onValueChange = { pasun = it },
                    label = { Text("पासून (क्रमांक)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = paraynt,
                    onValueChange = { paraynt = it },
                    label = { Text("पर्यंत (क्रमांक)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = total.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("एकूण रक्त नमुने") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()
            Text("📍 गावनिहाय तपशील", style = MaterialTheme.typography.titleMedium)
            
            villages.forEach { v ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${v.villageName}: ${v.sampleCount} (M:${v.maleCount}, F:${v.femaleCount})")
                        Button(onClick = { villages = villages.filter { it != v } }) {
                            Text("Remove")
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = villageName, onValueChange = { villageName = it }, label = { Text("Village") }, modifier = Modifier.weight(2f))
                OutlinedTextField(value = sampleCount, onValueChange = { sampleCount = it }, label = { Text("Total") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = maleCount, onValueChange = { maleCount = it }, label = { Text("Male") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }
            Button(
                onClick = {
                    val sCount = sampleCount.toIntOrNull() ?: 0
                    val mCount = maleCount.toIntOrNull() ?: 0
                    val fCount = maxOf(0, sCount - mCount)
                    if (villageName.isNotEmpty() && sCount > 0) {
                        villages = villages + VillageEntry(villageName = villageName, sampleCount = sCount, maleCount = mCount, femaleCount = fCount)
                        villageName = ""
                        sampleCount = ""
                        maleCount = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ गाव जोडा")
            }
            
            var showError by remember { mutableStateOf(false) }
            val villagesTotal = villages.sumOf { it.sampleCount }
            
            if (villagesTotal != total && total > 0) {
                showError = true
                Text("⚠️ बेरीज जुळत नाही! एकूण नमुने आणि गावनिहाय बेरीज तपासा.", color = MaterialTheme.colorScheme.error)
            } else {
                showError = false
            }

            Button(
                onClick = {
                    if (selectedEmployee != null && !showError && total > 0) {
                        viewModel.submitReport(
                            dateStr = dateStr,
                            upkendra = selectedEmployee!!.upkendra,
                            employeeName = selectedEmployee!!.name,
                            designation = selectedEmployee!!.designation,
                            bsCode = selectedEmployee!!.bsCode,
                            bundleNo = bundleNo,
                            pasun = pasun.toIntOrNull() ?: 0,
                            paraynt = paraynt.toIntOrNull() ?: 0,
                            total = total,
                            villages = villages
                        )
                        // Reset form
                        bundleNo = ""
                        pasun = ""
                        paraynt = ""
                        villages = emptyList()
                    }
                },
                enabled = selectedEmployee != null && !showError && total > 0,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("✅ डेटा सुरक्षित जतन करा")
            }
        }
    }
}
