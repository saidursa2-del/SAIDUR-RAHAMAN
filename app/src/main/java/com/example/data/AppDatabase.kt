package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, SalesReport::class, BudgetPlan::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun salesReportDao(): SalesReportDao
    abstract fun budgetPlanDao(): BudgetPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_sales_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val userDao = db.userDao()
            if (userDao.countUsers() == 0) {
                // Prepopulate Admin User
                userDao.insertUser(
                    User(
                        username = "admin",
                        password = "saidur337",
                        role = "admin",
                        status = "active",
                        storesCsv = ""
                    )
                )

                // Prepopulate Manager User
                userDao.insertUser(
                    User(
                        username = "manager",
                        password = "manager123",
                        role = "manager",
                        status = "active",
                        storesCsv = "311,317,320" // can view multiple but not necessarily all if csv specific, or all if we leave empty. Let's list a few as sample or leave empty for generic access to all. Let's put empty for full mult-store visibility.
                    )
                )

                // Master store codes
                val storeCodes = listOf("311", "317", "320", "326", "327", "328", "329", "337", "346", "348", "359", "361")
                for (code in storeCodes) {
                    userDao.insertUser(
                        User(
                            username = code,
                            password = code,
                            role = "user",
                            status = "active",
                            storesCsv = code
                        )
                    )
                }
            }
        }
    }
}
