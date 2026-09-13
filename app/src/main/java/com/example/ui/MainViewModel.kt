package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.Employee
import com.example.data.ReportEntry
import com.example.data.ReportRepository
import com.example.data.VillageEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "malaria-database"
    ).build()

    private val repository = ReportRepository(db.reportDao())

    val allEmployees = repository.allEmployees.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val prefs = application.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    private val _isSynced = MutableStateFlow(prefs.getBoolean("is_synced", true))
    val isSynced: StateFlow<Boolean> = _isSynced

    private fun markAsModified() {
        prefs.edit().putBoolean("is_synced", false).apply()
        _isSynced.value = false
    }

    fun markAsSynced() {
        prefs.edit().putBoolean("is_synced", true).apply()
        _isSynced.value = true
    }

    private val _currentMonthStr = MutableStateFlow(getCurrentMonthString())
    val currentMonthStr: StateFlow<String> = _currentMonthStr

    init {
        // Pre-populate some employees if empty
        viewModelScope.launch {
            allEmployees.collect { employees ->
                if (employees.isEmpty()) {
                    repository.insertEmployee(Employee("Rahul Jadhav", "Bhada", "आरोग्य सेवक", "B001"))
                    repository.insertEmployee(Employee("Priya Patil", "Bhada", "आरोग्य सेविका", "B002"))
                    repository.insertEmployee(Employee("Sita Ram", "Ausa", "आशा", "A001"))
                }
            }
        }
    }

    private fun getCurrentMonthString(): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        return sdf.format(Date())
    }

    fun submitReport(
        dateStr: String,
        upkendra: String,
        employeeName: String,
        designation: String,
        bsCode: String,
        bundleNo: String,
        pasun: Int,
        paraynt: Int,
        total: Int,
        villages: List<VillageEntry>
    ) {
        viewModelScope.launch {
            val report = ReportEntry(
                dateStr = dateStr,
                upkendra = upkendra,
                employeeName = employeeName,
                designation = designation,
                bsCode = bsCode,
                bundleNo = bundleNo,
                pasun = pasun,
                paraynt = paraynt,
                total = total,
                monthStr = _currentMonthStr.value
            )
            repository.insertFullReport(report, villages)
            markAsModified()
        }
    }

    // Dashboard stats calculation based on current month
    // In a real app we'd combine flows from db, here we can simplify
    val currentMonthReports = repository.getReportEntriesByMonth(getCurrentMonthString()).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    suspend fun generateCsvData(): String {
        val reports = repository.getAllReportsSync()
        val sb = StringBuilder()
        sb.append("Date,Upkendra,Employee,Designation,BS Code,Bundle No,Pasun,Paraynt,Total,Village,Village Samples,Male,Female\n")
        for (r in reports) {
            val villages = repository.getVillagesForReportSync(r.id)
            if (villages.isEmpty()) {
                sb.append("${r.dateStr},${r.upkendra},${r.employeeName},${r.designation},${r.bsCode},${r.bundleNo},${r.pasun},${r.paraynt},${r.total},,,,\n")
            } else {
                for (v in villages) {
                    sb.append("${r.dateStr},${r.upkendra},${r.employeeName},${r.designation},${r.bsCode},${r.bundleNo},${r.pasun},${r.paraynt},${r.total},${v.villageName},${v.sampleCount},${v.maleCount},${v.femaleCount}\n")
                }
            }
        }
        return sb.toString()
    }

    suspend fun generateMonthlyReportHtml(): String {
        val monthStr = _currentMonthStr.value
        val allReports = repository.getAllReportsSync()
        val thisMonthReports = allReports.filter { it.monthStr == monthStr }
        
        val villagesMap = mutableMapOf<Int, List<VillageEntry>>()
        for (r in thisMonthReports) {
            villagesMap[r.id] = repository.getVillagesForReportSync(r.id)
        }
        
        return MonthlyReportGenerator.generateHtml(monthStr, thisMonthReports, villagesMap)
    }

    suspend fun importCsvData(uri: Uri, context: Context): Int {
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = inputStream?.bufferedReader()
            val lines = reader?.readLines() ?: return@withContext 0
            if (lines.size <= 1) return@withContext 0
            
            val dataLines = lines.drop(1)
            
            val reportMap = mutableMapOf<String, ReportEntry>()
            val villageMap = mutableMapOf<String, MutableList<VillageEntry>>()
            
            var importedCount = 0

            for (line in dataLines) {
                val parts = line.split(",")
                if (parts.size >= 9) {
                    val dateStr = parts[0]
                    val upkendra = parts[1]
                    val employeeName = parts[2]
                    val designation = parts[3]
                    val bsCode = parts[4]
                    val bundleNo = parts[5]
                    val pasun = parts[6].toIntOrNull() ?: 0
                    val paraynt = parts[7].toIntOrNull() ?: 0
                    val total = parts[8].toIntOrNull() ?: 0
                    
                    val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
                    var monthStr = _currentMonthStr.value
                    try {
                        val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
                        if (parsedDate != null) {
                           monthStr = sdf.format(parsedDate)
                        }
                    } catch(e: Exception) {}
                    
                    val key = "$dateStr-$upkendra-$employeeName-$bundleNo"
                    if (!reportMap.containsKey(key)) {
                        reportMap[key] = ReportEntry(
                            dateStr = dateStr, upkendra = upkendra, employeeName = employeeName,
                            designation = designation, bsCode = bsCode, bundleNo = bundleNo,
                            pasun = pasun, paraynt = paraynt, total = total, monthStr = monthStr
                        )
                        villageMap[key] = mutableListOf()
                        importedCount++
                    }
                    
                    if (parts.size >= 13 && parts[9].isNotEmpty()) {
                        val villageName = parts[9]
                        val vSamples = parts[10].toIntOrNull() ?: 0
                        val vMale = parts[11].toIntOrNull() ?: 0
                        val vFemale = parts[12].toIntOrNull() ?: 0
                        villageMap[key]?.add(
                            VillageEntry(villageName = villageName, sampleCount = vSamples, maleCount = vMale, femaleCount = vFemale)
                        )
                    }
                }
            }
            
            for ((key, report) in reportMap) {
                val villages = villageMap[key] ?: emptyList()
                repository.insertFullReport(report, villages)
            }
            if (importedCount > 0) {
                markAsModified()
            }
            importedCount
        }
    }
}
