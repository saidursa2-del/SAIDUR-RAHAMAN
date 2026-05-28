package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val password: String,
    val role: String, // "admin", "manager" or "user"
    val status: String = "active",
    val storesCsv: String // e.g., "311,317" or "" for admin (all allowed)
) {
    fun isStoreAllowed(code: String): Boolean {
        if (role == "admin") return true
        val list = storesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return list.isEmpty() || list.contains(code)
    }

    fun getAllowedStores(): List<String> {
        if (role == "admin") return emptyList() // indicates all allowed, but list of STORE_CODES is master
        return storesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}

@Entity(
    tableName = "sales_reports",
    indices = [Index(value = ["storeCode", "date", "shift"], unique = true)]
)
data class SalesReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val storeCode: String,
    val storeName: String,
    val date: String, // YYYY-MM-DD
    val shift: String, // "Morning", "Night", "Full Day"
    val netSales: Double,
    val budgetSale: Double,
    val homeDelivery: Double,
    val guestCount: Int,
    val dineIn: Double,
    val ltoSale: Double,
    val mochaQty: Int,
    val mixedBoxQty: Int,
    val feedback: Double,
    val feedbackComment: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    // Computed values
    val variance: Double get() = netSales - budgetSale
    val achievementPercentage: Double get() = if (budgetSale > 0.0) (netSales / budgetSale) * 100.0 else 0.0
    val averageCheck: Double get() = if (guestCount > 0) netSales / guestCount else 0.0
    val mochaPercentage: Double get() = if (guestCount > 0) (mochaQty.toDouble() / guestCount.toDouble()) * 100.0 else 0.0
    val mixedBoxPercentage: Double get() = if (guestCount > 0) (mixedBoxQty.toDouble() / guestCount.toDouble()) * 100.0 else 0.0
}

@Entity(
    tableName = "budget_plans",
    primaryKeys = ["storeCode", "month"]
)
data class BudgetPlan(
    val storeCode: String,
    val month: String, // YYYY-MM
    val target: Double,
    val workingDays: Int
)
