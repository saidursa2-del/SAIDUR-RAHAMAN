package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int
}

@Dao
interface SalesReportDao {
    @Query("SELECT * FROM sales_reports ORDER BY date DESC, shift DESC")
    fun getAllReportsFlow(): Flow<List<SalesReport>>

    @Query("SELECT * FROM sales_reports ORDER BY date DESC, shift DESC")
    suspend fun getAllReports(): List<SalesReport>

    @Query("SELECT * FROM sales_reports WHERE storeCode = :storeCode ORDER BY date DESC, shift DESC")
    fun getReportsForStoreFlow(storeCode: String): Flow<List<SalesReport>>

    @Query("SELECT * FROM sales_reports WHERE storeCode = :storeCode ORDER BY date DESC, shift DESC")
    suspend fun getReportsForStore(storeCode: String): List<SalesReport>

    @Query("SELECT * FROM sales_reports WHERE storeCode = :storeCode AND date LIKE :month || '%' ORDER BY date DESC, shift DESC")
    fun getReportsForStoreAndMonthFlow(storeCode: String, month: String): Flow<List<SalesReport>>

    @Query("SELECT * FROM sales_reports WHERE storeCode = :storeCode AND date LIKE :month || '%' ORDER BY date DESC, shift DESC")
    suspend fun getReportsForStoreAndMonth(storeCode: String, month: String): List<SalesReport>

    @Query("SELECT * FROM sales_reports WHERE date LIKE :month || '%' ORDER BY date DESC, shift DESC")
    fun getAllReportsForMonthFlow(month: String): Flow<List<SalesReport>>

    @Query("SELECT * FROM sales_reports WHERE date = :date AND storeCode = :storeCode AND shift = :shift LIMIT 1")
    suspend fun getReport(date: String, storeCode: String, shift: String): SalesReport?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: SalesReport): Long

    @Update
    suspend fun updateReport(report: SalesReport)

    @Query("DELETE FROM sales_reports WHERE id = :id")
    suspend fun deleteReportById(id: Int)

    @Query("DELETE FROM sales_reports")
    suspend fun clearAllReports()
}

@Dao
interface BudgetPlanDao {
    @Query("SELECT * FROM budget_plans")
    fun getAllBudgetsFlow(): Flow<List<BudgetPlan>>

    @Query("SELECT * FROM budget_plans WHERE storeCode = :storeCode AND month = :month LIMIT 1")
    suspend fun getBudget(storeCode: String, month: String): BudgetPlan?

    @Query("SELECT * FROM budget_plans WHERE storeCode = :storeCode")
    fun getBudgetsForStoreFlow(storeCode: String): Flow<List<BudgetPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budgetPlan: BudgetPlan)

    @Query("DELETE FROM budget_plans")
    suspend fun clearBudgetPlans()
}
