package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SalesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = SalesRepository(database)

    // Master constants
    val STORE_CODES = listOf("311", "317", "320", "326", "327", "328", "329", "337", "346", "348", "359", "361")
    val STORE_NAMES = mapOf(
        "311" to "Cinnabon Khobar Mall",
        "317" to "Cinnabon Dahran Mall",
        "320" to "Cinnabon Dahran Mall KIOSK",
        "326" to "Cinnabon Venicia",
        "327" to "Cinnabon Venicia SBC",
        "328" to "Cinnabon Dahran Mall SBC KIOSK G2",
        "329" to "Cinnabon Othim Mall HUFUF",
        "337" to "Cinnabon Al Ehssa Mall KIOSK",
        "346" to "Cinnabon Al Ehsa Panda",
        "348" to "Cinnabon Khaldiya Hasa",
        "359" to "Cinnabon Sulaiman Al Habib",
        "361" to "Cinnabon Al Rakah Square"
    )

    // Current Session States
    val currentUser = MutableStateFlow<User?>(null)
    val selectedStoreCode = MutableStateFlow("")
    val activeTab = MutableStateFlow("entry") // entry, bulk, preview, dashboard, budget, history, multistore
    
    // Month selector for Dashboard, Budget & Multi-Store (Format: YYYY-MM)
    val selectedMonth = MutableStateFlow(getCurrentMonthString())

    // CSV or WhatsApp raw input text for bulk parsing
    val bulkInputField = MutableStateFlow("")
    val bulkParseResults = MutableStateFlow<List<SalesReport>>(emptyList())
    val bulkMode = MutableStateFlow("csv") // csv, whatsapp

    // Master lists from database
    val allReports = repository.allReports.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allUsers = repository.allUsers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allBudgets = repository.allBudgets.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // --- Daily Sales Form States ---
    val formDate = MutableStateFlow(getCurrentDateString())
    val formShift = MutableStateFlow("Full Day") // Morning, Night, Full Day
    val formNetSales = MutableStateFlow("")
    val formBudgetSale = MutableStateFlow("")
    val formHomeDelivery = MutableStateFlow("")
    val formGuestCount = MutableStateFlow("")
    val formDineInOverride = MutableStateFlow<Double?>(null) // null means auto-calculated: netSales - homeDelivery
    val formLtoSale = MutableStateFlow("")
    val formMochaQty = MutableStateFlow("")
    val formMixedBoxQty = MutableStateFlow("")
    val formFeedback = MutableStateFlow("4.0")
    val formFeedbackComment = MutableStateFlow("")
    val editingReportId = MutableStateFlow<Int?>(null)

    // Toast and Dialog communication states
    val toastMessage = MutableSharedFlow<String>()
    val showResetDialog = MutableStateFlow(false)

    // Dynamic Section Configurations (for future customization and section toggling)
    val configShowTrends = MutableStateFlow(true)
    val configShowFeedbackAnalytics = MutableStateFlow(true)
    val configShowBulkTab = MutableStateFlow(true)
    val configShowBudgetTab = MutableStateFlow(true)

    init {
        // Observe selection changes and trigger updates
        viewModelScope.launch {
            selectedStoreCode.collect { code ->
                if (code.isNotEmpty()) {
                    // Update active form's store fields if necessary
                }
            }
        }
    }

    // Helper to get name of selected store
    fun getStoreName(code: String): String = STORE_NAMES[code] ?: "Unknown Store"

    // Dynamic calculations of form fields
    val currentDineInValue = combine(formNetSales, formHomeDelivery, formDineInOverride) { net, del, override ->
        if (override != null) return@combine override
        val netVal = net.toDoubleOrNull() ?: 0.0
        val delVal = del.toDoubleOrNull() ?: 0.0
        maxOf(0.0, netVal - delVal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentVariance = combine(formNetSales, formBudgetSale) { net, bud ->
        val netVal = net.toDoubleOrNull() ?: 0.0
        val budVal = bud.toDoubleOrNull() ?: 0.0
        netVal - budVal
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentAchievement = combine(formNetSales, formBudgetSale) { net, bud ->
        val netVal = net.toDoubleOrNull() ?: 0.0
        val budVal = bud.toDoubleOrNull() ?: 0.0
        if (budVal > 0.0) (netVal / budVal) * 100.0 else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentAvgCheck = combine(formNetSales, formGuestCount) { net, guests ->
        val netVal = net.toDoubleOrNull() ?: 0.0
        val guestVal = guests.toIntOrNull() ?: 0
        if (guestVal > 0) netVal / guestVal else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMochaPct = combine(formMochaQty, formGuestCount) { mocha, guests ->
        val mochaVal = mocha.toDoubleOrNull() ?: 0.0
        val guestVal = guests.toDoubleOrNull() ?: 0.0
        if (guestVal > 0) (mochaVal / guestVal) * 100.0 else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMixedBoxPct = combine(formMixedBoxQty, formGuestCount) { mixed, guests ->
        val mixedVal = mixed.toDoubleOrNull() ?: 0.0
        val guestVal = guests.toDoubleOrNull() ?: 0.0
        if (guestVal > 0) (mixedVal / guestVal) * 100.0 else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Dynamic Month-to-Date (MTD) sums for Selected Store & Selected Month ---
    val mtdReports = combine(allReports, selectedStoreCode, formDate) { reports, storeCode, date ->
        val targetMonth = date.take(7) // YYYY-MM
        reports.filter { r ->
            r.storeCode == storeCode && r.date.startsWith(targetMonth) && r.date <= date
        }.sortedBy { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mtdNetSales = mtdReports.map { list -> list.sumOf { it.netSales } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val mtdBudget = mtdReports.map { list -> list.sumOf { it.budgetSale } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val mtdDineIn = mtdReports.map { list -> list.sumOf { it.dineIn } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val mtdHomeDelivery = mtdReports.map { list -> list.sumOf { it.homeDelivery } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val mtdGuestCount = mtdReports.map { list -> list.sumOf { it.guestCount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val mtdLtoSale = mtdReports.map { list -> list.sumOf { it.ltoSale } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val mtdMochaQty = mtdReports.map { list -> list.sumOf { it.mochaQty } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val mtdMixedBoxQty = mtdReports.map { list -> list.sumOf { it.mixedBoxQty } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val mtdFeedbackAvg = mtdReports.map { list ->
        if (list.isNotEmpty()) list.map { it.feedback }.average() else 4.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4.0)

    // Derived MTD Calculations
    val mtdVariance = combine(mtdNetSales, mtdBudget) { net, bud -> net - bud }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val mtdAchievement = combine(mtdNetSales, mtdBudget) { net, bud ->
        if (bud > 0.0) (net / bud) * 100.0 else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val mtdAvgCheck = combine(mtdNetSales, mtdGuestCount) { net, guests ->
        if (guests > 0) net / guests else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val mtdMochaPct = combine(mtdMochaQty, mtdGuestCount) { mocha, guests ->
        if (guests > 0) (mocha.toDouble() / guests.toDouble()) * 100.0 else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val mtdMixedBoxPct = combine(mtdMixedBoxQty, mtdGuestCount) { mixed, guests ->
        if (guests > 0) (mixed.toDouble() / guests.toDouble()) * 100.0 else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Active Store Target Budget for selectedMonth ---
    val activeStoreBudgetPlan = combine(allBudgets, selectedStoreCode, selectedMonth) { budgets, storeCode, month ->
        budgets.firstOrNull { it.storeCode == storeCode && it.month == month }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Authentication Actions ---
    fun login(username: String, passStr: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user != null && user.password == passStr && user.status == "active") {
                currentUser.value = user
                // Dynamic tab routing based on user level on login
                when (user.role) {
                    "admin" -> activeTab.value = "entry"
                    "manager" -> activeTab.value = "dashboard"
                    else -> activeTab.value = "entry"
                }
                // Automatically route store code
                val allowed = user.getAllowedStores()
                if (allowed.isNotEmpty()) {
                    selectedStoreCode.value = allowed.first()
                } else {
                    selectedStoreCode.value = STORE_CODES.first()
                }
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun logout() {
        currentUser.value = null
        selectedStoreCode.value = ""
        activeTab.value = "entry"
    }

    fun forceResetPassword(username: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user != null) {
                val updated = user.copy(password = "saidur337")
                repository.updateUser(updated)
                onComplete("Password reset to: saidur337")
            } else {
                onComplete("User not found.")
            }
        }
    }

    // --- Daily Form Operations ---
    fun loadExistingReportForDate() {
        viewModelScope.launch {
            val dateStr = formDate.value
            val storeCodeStr = selectedStoreCode.value
            val shiftStr = formShift.value
            val existing = repository.checkReportExists(dateStr, storeCodeStr, shiftStr)
            if (existing != null) {
                editingReportId.value = existing.id
                formNetSales.value = existing.netSales.toString()
                formBudgetSale.value = existing.budgetSale.toString()
                formHomeDelivery.value = existing.homeDelivery.toString()
                formGuestCount.value = existing.guestCount.toString()
                formDineInOverride.value = existing.dineIn
                formLtoSale.value = existing.ltoSale.toString()
                formMochaQty.value = existing.mochaQty.toString()
                formMixedBoxQty.value = existing.mixedBoxQty.toString()
                formFeedback.value = existing.feedback.toString()
                formFeedbackComment.value = existing.feedbackComment
                toastMessage.emit("Loaded existing report for $dateStr ($shiftStr)")
            } else {
                clearDailyFormKeepDate()
            }
        }
    }

    fun clearDailyFormKeepDate() {
        editingReportId.value = null
        formNetSales.value = ""
        formBudgetSale.value = ""
        formHomeDelivery.value = ""
        formGuestCount.value = ""
        formDineInOverride.value = null
        formLtoSale.value = ""
        formMochaQty.value = ""
        formMixedBoxQty.value = ""
        formFeedback.value = "4.0"
        formFeedbackComment.value = ""
    }

    fun clearDailyForm() {
        formDate.value = getCurrentDateString()
        formShift.value = "Full Day"
        clearDailyFormKeepDate()
    }

    fun saveOrUpdateDailyReport() {
        viewModelScope.launch {
            val storeCodeStr = selectedStoreCode.value
            val storeNameStr = getStoreName(storeCodeStr)
            val dateStr = formDate.value
            val shiftStr = formShift.value

            val salesVal = formNetSales.value.toDoubleOrNull() ?: 0.0
            val budgetVal = formBudgetSale.value.toDoubleOrNull() ?: 0.0
            val deliveryVal = formHomeDelivery.value.toDoubleOrNull() ?: 0.0
            val guestVal = formGuestCount.value.toIntOrNull() ?: 0
            val dineInVal = currentDineInValue.value
            val ltoVal = formLtoSale.value.toDoubleOrNull() ?: 0.0
            val mochaVal = formMochaQty.value.toIntOrNull() ?: 0
            val mixedVal = formMixedBoxQty.value.toIntOrNull() ?: 0
            val feedVal = formFeedback.value.toDoubleOrNull() ?: 4.0
            val feedCommentVal = formFeedbackComment.value

            if (salesVal <= 0.0 && budgetVal <= 0.0) {
                toastMessage.emit("Error: Net Sales or Budget is required")
                return@launch
            }

            val existingId = editingReportId.value
            if (existingId != null) {
                val report = SalesReport(
                    id = existingId,
                    storeCode = storeCodeStr,
                    storeName = storeNameStr,
                    date = dateStr,
                    shift = shiftStr,
                    netSales = salesVal,
                    budgetSale = budgetVal,
                    homeDelivery = deliveryVal,
                    guestCount = guestVal,
                    dineIn = dineInVal,
                    ltoSale = ltoVal,
                    mochaQty = mochaVal,
                    mixedBoxQty = mixedVal,
                    feedback = feedVal,
                    feedbackComment = feedCommentVal
                )
                repository.updateReport(report)
                toastMessage.emit("Report successfully updated!")
            } else {
                // Check double entries
                val doubleEntry = repository.checkReportExists(dateStr, storeCodeStr, shiftStr)
                if (doubleEntry != null) {
                    val report = doubleEntry.copy(
                        netSales = salesVal,
                        budgetSale = budgetVal,
                        homeDelivery = deliveryVal,
                        guestCount = guestVal,
                        dineIn = dineInVal,
                        ltoSale = ltoVal,
                        mochaQty = mochaVal,
                        mixedBoxQty = mixedVal,
                        feedback = feedVal,
                        feedbackComment = feedCommentVal,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.updateReport(report)
                    toastMessage.emit("Existing entry updated for $dateStr")
                } else {
                    val report = SalesReport(
                        storeCode = storeCodeStr,
                        storeName = storeNameStr,
                        date = dateStr,
                        shift = shiftStr,
                        netSales = salesVal,
                        budgetSale = budgetVal,
                        homeDelivery = deliveryVal,
                        guestCount = guestVal,
                        dineIn = dineInVal,
                        ltoSale = ltoVal,
                        mochaQty = mochaVal,
                        mixedBoxQty = mixedVal,
                        feedback = feedVal,
                        feedbackComment = feedCommentVal
                    )
                    repository.insertReport(report)
                    toastMessage.emit("Daily Report saved successfully!")
                }
            }
            clearDailyForm()
        }
    }

    fun deleteReport(id: Int) {
        viewModelScope.launch {
            repository.deleteReportById(id)
            toastMessage.emit("Report deleted.")
        }
    }

    fun smartLoadMTDValues() {
        viewModelScope.launch {
            // Evaluates dynamic values for the store and current date up to date
            val reportsList = mtdReports.value
            if (reportsList.isNotEmpty()) {
                toastMessage.emit("MTD Stats loaded from ${reportsList.size} past records!")
            } else {
                toastMessage.emit("No past records for the active store and month found.")
            }
        }
    }

    // --- Bulk Import Parsing ---
    fun parseBulkInput() {
        viewModelScope.launch {
            val text = bulkInputField.value.trim()
            if (text.isEmpty()) {
                toastMessage.emit("Bulk text input is empty!")
                return@launch
            }

            val parsedList = mutableListOf<SalesReport>()
            val storeCodeStr = selectedStoreCode.value
            val storeNameStr = getStoreName(storeCodeStr)

            if (bulkMode.value == "csv") {
                // Formatting: Date,Shift,Net Sales,Budget,Dine IN,Delivery,Guests,LTO Sale,Mocha Qty,Mixed Box Qty
                val lines = text.split("\n")
                for (line in lines) {
                    val parts = line.split(",").map { it.trim() }
                    if (parts.size >= 4) {
                        try {
                            val parsedDate = parts[0] // YYYY-MM-DD
                            val parsedShift = parts[1] // Morning / Night / Full Day
                            val netSalesVal = parts[2].toDoubleOrNull() ?: 0.0
                            val budgetVal = parts[3].toDoubleOrNull() ?: 0.0
                            val dineInVal = parts.getOrNull(4)?.toDoubleOrNull()
                            val deliveryVal = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                            val guestVal = parts.getOrNull(6)?.toIntOrNull() ?: 0
                            val ltoVal = parts.getOrNull(7)?.toDoubleOrNull() ?: 0.0
                            val mochaVal = parts.getOrNull(8)?.toIntOrNull() ?: 0
                            val mixedVal = parts.getOrNull(9)?.toIntOrNull() ?: 0

                            val finalDineInVal = dineInVal ?: maxOf(0.0, netSalesVal - deliveryVal)

                            parsedList.add(
                                SalesReport(
                                    storeCode = storeCodeStr,
                                    storeName = storeNameStr,
                                    date = parsedDate,
                                    shift = parsedShift,
                                    netSales = netSalesVal,
                                    budgetSale = budgetVal,
                                    homeDelivery = deliveryVal,
                                    guestCount = guestVal,
                                    dineIn = finalDineInVal,
                                    ltoSale = ltoVal,
                                    mochaQty = mochaVal,
                                    mixedBoxQty = mixedVal,
                                    feedback = 4.0
                                )
                            )
                        } catch (e: Exception) {
                            // Skip invalid lines
                        }
                    }
                }
            } else {
                // WhatsApp reporting Parser
                // Key value keyword detection
                val lines = text.split("\n")
                var extractedDate = formDate.value
                var extractedShift = formShift.value
                var extractedNetSales = 0.0
                var extractedBudget = 0.0
                var extractedDelivery = 0.0
                var extractedGuests = 0
                var extractedDineIn: Double? = null
                var extractedLto = 0.0
                var extractedMocha = 0
                var extractedMixed = 0

                for (line in lines) {
                    val l = line.lowercase(Locale.ROOT)
                    when {
                        l.contains("date:") || l.contains("date :") -> {
                            val v = line.substringAfter(":").trim()
                            if (v.isNotEmpty()) extractedDate = v
                        }
                        l.contains("shift:") || l.contains("shift :") -> {
                            val v = line.substringAfter(":").trim()
                            if (v.isNotEmpty()) extractedShift = v
                        }
                        l.contains("net sales:") || l.contains("net sales :") || l.contains("sales:") -> {
                            extractedNetSales = line.substringAfter(":").trim().replace(",", "").toDoubleOrNull() ?: 0.0
                        }
                        l.contains("budget:") || l.contains("budget :") -> {
                            extractedBudget = line.substringAfter(":").trim().replace(",", "").toDoubleOrNull() ?: 0.0
                        }
                        l.contains("delivery:") || l.contains("delivery :") || l.contains("home delivery:") -> {
                            extractedDelivery = line.substringAfter(":").trim().replace(",", "").toDoubleOrNull() ?: 0.0
                        }
                        l.contains("dine in:") || l.contains("dine in :") -> {
                            extractedDineIn = line.substringAfter(":").trim().replace(",", "").toDoubleOrNull()
                        }
                        l.contains("guest:") || l.contains("guest count:") || l.contains("guests:") -> {
                            extractedGuests = line.substringAfter(":").trim().replace(",", "").toIntOrNull() ?: 0
                        }
                        l.contains("lto:") || l.contains("lto sale:") -> {
                            extractedLto = line.substringAfter(":").trim().replace(",", "").toDoubleOrNull() ?: 0.0
                        }
                        l.contains("mocha qty:") || l.contains("mocha sales:") || l.contains("mocha:") -> {
                            extractedMocha = line.substringAfter(":").trim().replace(",", "").toIntOrNull() ?: 0
                        }
                        l.contains("mixed box:") || l.contains("mixed box qty:") || l.contains("mixbox:") -> {
                            extractedMixed = line.substringAfter(":").trim().replace(",", "").toIntOrNull() ?: 0
                        }
                    }
                }

                parsedList.add(
                    SalesReport(
                        storeCode = storeCodeStr,
                        storeName = storeNameStr,
                        date = extractedDate,
                        shift = extractedShift,
                        netSales = extractedNetSales,
                        budgetSale = extractedBudget,
                        homeDelivery = extractedDelivery,
                        guestCount = extractedGuests,
                        dineIn = extractedDineIn ?: maxOf(0.0, extractedNetSales - extractedDelivery),
                        ltoSale = extractedLto,
                        mochaQty = extractedMocha,
                        mixedBoxQty = extractedMixed,
                        feedback = 4.0
                    )
                )
            }

            if (parsedList.isNotEmpty()) {
                bulkParseResults.value = parsedList
                toastMessage.emit("Successfully parsed ${parsedList.size} entries. Review below to save.")
            } else {
                toastMessage.emit("Could not parse any reports. Check format.")
            }
        }
    }

    fun saveBulkParsedReports() {
        viewModelScope.launch {
            val list = bulkParseResults.value
            var savedCount = 0
            for (report in list) {
                // Check duplicate
                val doubleEntry = repository.checkReportExists(report.date, report.storeCode, report.shift)
                if (doubleEntry != null) {
                    repository.updateReport(report.copy(id = doubleEntry.id))
                } else {
                    repository.insertReport(report)
                }
                savedCount++
            }
            bulkInputField.value = ""
            bulkParseResults.value = emptyList()
            toastMessage.emit("Saved $savedCount reports successfully!")
            activeTab.value = "entry"
        }
    }

    // --- Budget Planning Actions ---
    fun saveTargetBudget(targetStr: String, daysStr: String) {
        viewModelScope.launch {
            val storeCodeStr = selectedStoreCode.value
            val monthStr = selectedMonth.value
            val targetVal = targetStr.toDoubleOrNull() ?: 0.0
            val daysVal = daysStr.toIntOrNull() ?: 26

            if (targetVal <= 0.0) {
                toastMessage.emit("Please enter a valid target budget.")
                return@launch
            }

            val budgetPlan = BudgetPlan(
                storeCode = storeCodeStr,
                month = monthStr,
                target = targetVal,
                workingDays = daysVal
            )

            repository.saveBudget(budgetPlan)
            toastMessage.emit("Monthly target for $monthStr updated and saved!")
        }
    }

    // --- Settings / Admin Management Actions ---
    fun changePassword(username: String, oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user != null && user.password == oldPass) {
                val updatedUser = user.copy(password = newPass)
                repository.updateUser(updatedUser)
                // update current session user if changed
                if (currentUser.value?.username == username) {
                    currentUser.value = updatedUser
                }
                onResult(true, "Password successfully updated!")
            } else {
                onResult(false, "Invalid current password.")
            }
        }
    }

    fun addNewUser(username: String, passStr: String, storesCsv: String, role: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (username.isEmpty() || passStr.isEmpty()) {
                onComplete("Username and Password are required.")
                return@launch
            }
            val existing = repository.getUserByUsername(username)
            if (existing != null) {
                onComplete("User already exists!")
                return@launch
            }
            val newUser = User(
                username = username,
                password = passStr,
                role = role,
                status = "active",
                storesCsv = storesCsv
            )
            repository.insertUser(newUser)
            onComplete("User $username added successfully!")
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            if (user.username == "admin") {
                toastMessage.emit("Cannot delete superadmin accounts!")
                return@launch
            }
            repository.deleteUser(user)
            toastMessage.emit("User administrative account deleted.")
        }
    }

    fun systemResetAll() {
        viewModelScope.launch {
            repository.clearAllReports()
            repository.clearBudgetPlans()
            toastMessage.emit("System database reset successfully!")
        }
    }

    // Backup as CSV string
    fun generateBackupCsv(): String {
        val s = StringBuilder()
        s.append("StoreCode,StoreName,Date,Shift,NetSales,Budget,HomeDelivery,GuestCount,DineIn,LtoSale,MochaQty,MixedBoxQty,Feedback\n")
        allReports.value.forEach { r ->
            s.append("${r.storeCode},${r.storeName},${r.date},${r.shift},${r.netSales},${r.budgetSale},${r.homeDelivery},${r.guestCount},${r.dineIn},${r.ltoSale},${r.mochaQty},${r.mixedBoxQty},${r.feedback}\n")
        }
        return s.toString()
    }

    // Generating WhatsApp Message
    fun getWhatsAppText(): String {
        val storeCodeStr = selectedStoreCode.value
        val dateStr = formDate.value
        val shiftStr = formShift.value

        val sales = formNetSales.value.toDoubleOrNull() ?: 0.0
        val budget = formBudgetSale.value.toDoubleOrNull() ?: 0.0
        val variance = currentVariance.value
        val ach = currentAchievement.value
        val delivery = formHomeDelivery.value.toDoubleOrNull() ?: 0.0
        val dine = currentDineInValue.value
        val guests = formGuestCount.value.toIntOrNull() ?: 0
        val avgCheck = currentAvgCheck.value
        val lto = formLtoSale.value.toDoubleOrNull() ?: 0.0
        val mocha = formMochaQty.value.toIntOrNull() ?: 0
        val mochaPctVal = currentMochaPct.value
        val mixed = formMixedBoxQty.value.toIntOrNull() ?: 0
        val mixedPctVal = currentMixedBoxPct.value

        val mtdS = mtdNetSales.value
        val mtdB = mtdBudget.value
        val mtdV = mtdVariance.value
        val mtdA = mtdAchievement.value
        val mtdD = mtdDineIn.value
        val mtdH = mtdHomeDelivery.value
        val mtdG = mtdGuestCount.value
        val mtdAc = mtdAvgCheck.value
        val mtdL = mtdLtoSale.value
        val mtdM = mtdMochaQty.value
        val mtdMp = mtdMochaPct.value
        val mtdMb = mtdMixedBoxQty.value
        val mtdMbp = mtdMixedBoxPct.value
        val mtdFeed = mtdFeedbackAvg.value

        return """
*📊 SMART SALES PRO - DAILY REPORT*
*👨‍💻 Developed by SAIDUR RAHAMAN*
──────────────────────
*🏪 Store:* $storeCodeStr - ${getStoreName(storeCodeStr)}
*📅 Date:* $dateStr
*⏰ Shift:* $shiftStr
──────────────────────
*💰 TODAY'S PERFORMANCE:*
• Net Sales: $${formatDouble(sales)}
• Daily Budget: $${formatDouble(budget)}
• Variance: $${formatSignDouble(variance)}
• Achievement: ${formatDouble(ach)}%
──────────────────────
*🛒 SECTOR SALES:*
• Dine-In: $${formatDouble(dine)}
• Home Delivery: $${formatDouble(delivery)}
• Guests: $guests
• Avg Check: $${formatDouble(avgCheck)}
──────────────────────
*🎯 ATTACHMENTS:*
• LTO Sale: $${formatDouble(lto)}
• Mocha Sales Qty: $mocha (${formatDouble(mochaPctVal)}%)
• Mixed Box Qty: $mixed (${formatDouble(mixedPctVal)}%)
──────────────────────
*📈 MONTH-TO-DATE (MTD):*
• MTD Net Sales: $${formatDouble(mtdS)}
• MTD Budget: $${formatDouble(mtdB)}
• MTD Variance: $${formatSignDouble(mtdV)}
• MTD Achievement: ${formatDouble(mtdA)}%
• MTD Dine-In: $${formatDouble(mtdD)}
• MTD Delivery: $${formatDouble(mtdH)}
• MTD Guests: $mtdG
• MTD Avg Check: $${formatDouble(mtdAc)}
• MTD LTO Sale: $${formatDouble(mtdL)}
• MTD Mocha Qty: $mtdM (${formatDouble(mtdMp)}%)
• MTD Mixed Box Qty: $mtdMb (${formatDouble(mtdMbp)}%)
• Customer Feedback: ${formatDouble(mtdFeed, 1)} / 5
──────────────────────
*✦ Ultimate Store Intelligence Platform ✦*
""".trimIndent()
    }

    // --- Date Format Helpers ---
    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentMonthString(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDouble(v: Double, d: Int = 2): String {
        return String.format(Locale.US, "%.${d}f", v)
    }

    fun formatSignDouble(v: Double): String {
        val sign = if (v >= 0.0) "+" else ""
        return sign + formatDouble(v)
    }
}
