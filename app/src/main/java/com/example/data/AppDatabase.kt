package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import androidx.room.Database
import androidx.room.RoomDatabase

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val name: String,
    val upkendra: String,
    val designation: String,
    val bsCode: String
)

@Entity(tableName = "report_entries")
data class ReportEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateStr: String,
    val upkendra: String,
    val employeeName: String,
    val designation: String,
    val bsCode: String,
    val bundleNo: String,
    val pasun: Int,
    val paraynt: Int,
    val total: Int,
    val monthStr: String 
)

@Entity(tableName = "village_entries")
data class VillageEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reportEntryId: Int = 0,
    val villageName: String,
    val sampleCount: Int,
    val maleCount: Int,
    val femaleCount: Int
)

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Insert
    suspend fun insertReportEntry(reportEntry: ReportEntry): Long

    @Insert
    suspend fun insertVillageEntries(villageEntries: List<VillageEntry>)

    @Query("SELECT * FROM report_entries WHERE monthStr = :monthStr")
    fun getReportEntriesByMonth(monthStr: String): Flow<List<ReportEntry>>

    @Query("SELECT * FROM report_entries WHERE dateStr = :dateStr")
    fun getReportEntriesByDate(dateStr: String): Flow<List<ReportEntry>>
    
    @Query("SELECT * FROM village_entries WHERE reportEntryId = :reportId")
    fun getVillageEntriesForReport(reportId: Int): Flow<List<VillageEntry>>

    @Query("SELECT * FROM report_entries")
    suspend fun getAllReportsSync(): List<ReportEntry>

    @Query("SELECT * FROM village_entries WHERE reportEntryId = :reportId")
    suspend fun getVillageEntriesForReportSync(reportId: Int): List<VillageEntry>
    
    @Transaction
    suspend fun insertFullReport(report: ReportEntry, villages: List<VillageEntry>) {
        val reportId = insertReportEntry(report)
        val villagesWithReportId = villages.map { it.copy(reportEntryId = reportId.toInt()) }
        insertVillageEntries(villagesWithReportId)
    }
}

@Database(entities = [Employee::class, ReportEntry::class, VillageEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
}

class ReportRepository(private val dao: ReportDao) {
    val allEmployees = dao.getAllEmployees()

    suspend fun insertEmployee(employee: Employee) = dao.insertEmployee(employee)
    
    suspend fun insertFullReport(report: ReportEntry, villages: List<VillageEntry>) = 
        dao.insertFullReport(report, villages)
        
    fun getReportEntriesByMonth(monthStr: String) = dao.getReportEntriesByMonth(monthStr)
    fun getReportEntriesByDate(dateStr: String) = dao.getReportEntriesByDate(dateStr)

    suspend fun getAllReportsSync() = dao.getAllReportsSync()
    
    suspend fun getVillagesForReportSync(reportId: Int) = dao.getVillageEntriesForReportSync(reportId)
}
