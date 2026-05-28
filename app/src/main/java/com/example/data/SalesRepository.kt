package com.example.data

import kotlinx.coroutines.flow.Flow

class SalesRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val salesReportDao = db.salesReportDao()
    private val budgetPlanDao = db.budgetPlanDao()

    // --- Users ---
    val allUsers: Flow<List<User>> = userDao.getAllUsersFlow()

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    // --- Sales Reports ---
    val allReports: Flow<List<SalesReport>> = salesReportDao.getAllReportsFlow()

    fun getReportsForStore(storeCode: String): Flow<List<SalesReport>> {
        return salesReportDao.getReportsForStoreFlow(storeCode)
    }

    suspend fun getReportsForStoreList(storeCode: String): List<SalesReport> {
        return salesReportDao.getReportsForStore(storeCode)
    }

    fun getReportsForStoreAndMonth(storeCode: String, month: String): Flow<List<SalesReport>> {
        return salesReportDao.getReportsForStoreAndMonthFlow(storeCode, month)
    }

    suspend fun getReportsForStoreAndMonthList(storeCode: String, month: String): List<SalesReport> {
        return salesReportDao.getReportsForStoreAndMonth(storeCode, month)
    }

    fun getAllReportsForMonth(month: String): Flow<List<SalesReport>> {
        return salesReportDao.getAllReportsForMonthFlow(month)
    }

    suspend fun checkReportExists(date: String, storeCode: String, shift: String): SalesReport? {
        return salesReportDao.getReport(date, storeCode, shift)
    }

    suspend fun insertReport(report: SalesReport): Long {
        return salesReportDao.insertReport(report)
    }

    suspend fun updateReport(report: SalesReport) {
        salesReportDao.updateReport(report)
    }

    suspend fun deleteReportById(id: Int) {
        salesReportDao.deleteReportById(id)
    }

    suspend fun clearAllReports() {
        salesReportDao.clearAllReports()
    }

    // --- Budgets ---
    val allBudgets: Flow<List<BudgetPlan>> = budgetPlanDao.getAllBudgetsFlow()

    suspend fun getBudget(storeCode: String, month: String): BudgetPlan? {
        return budgetPlanDao.getBudget(storeCode, month)
    }

    fun getBudgetsForStore(storeCode: String): Flow<List<BudgetPlan>> {
        return budgetPlanDao.getBudgetsForStoreFlow(storeCode)
    }

    suspend fun saveBudget(budgetPlan: BudgetPlan) {
        budgetPlanDao.insertBudget(budgetPlan)
    }

    suspend fun clearBudgetPlans() {
        budgetPlanDao.clearBudgetPlans()
    }
}
