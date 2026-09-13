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
}
