package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SalesReport
import com.example.data.User
import com.example.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: SalesViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Observe sharing triggers
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentUser == null) {
                LoginView(viewModel = viewModel)
            } else {
                MainAppLayout(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LoginView(viewModel: SalesViewModel) {
    var selectedLoginStore by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorVisible by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var masterCode by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBg)
            .drawBehind {
                // Top-right glowing emerald orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x2210B981), Color.Transparent),
                        center = Offset(size.width * 1.0f, size.height * -0.05f),
                        radius = size.minDimension * 0.7f
                    ),
                    radius = size.minDimension * 0.7f,
                    center = Offset(size.width * 1.0f, size.height * -0.05f)
                )
                // Bottom-left glowing blue orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1F3B82F6), Color.Transparent),
                        center = Offset(size.width * -0.05f, size.height * 1.05f),
                        radius = size.minDimension * 0.8f
                    ),
                    radius = size.minDimension * 0.8f,
                    center = Offset(size.width * -0.05f, size.height * 1.05f)
                )
            }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .border(BorderStroke(1.dp, ImmersiveCardBorder), RoundedCornerShape(24.dp))
                .testTag("login_card"),
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏬",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "SAIDUR INTELLIGENCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveEmeraldLight,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Reporter ",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "PRO",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = ImmersiveEmerald
                    )
                }
                Text(
                    text = "Developed by SAIDUR RAHAMAN",
                    fontSize = 11.sp,
                    color = ImmersiveTextSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Store Selection
                Text(
                    text = "SELECT STORE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
                
                var expandedStoreDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    OutlinedButton(
                        onClick = { expandedStoreDropdown = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("login_store_select"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, ImmersiveCardBorder)
                    ) {
                        Text(
                            text = if (selectedLoginStore.isEmpty()) "-- Select Store --" 
                                   else if (selectedLoginStore == "admin") "Admin Control" 
                                   else "$selectedLoginStore - ${viewModel.getStoreName(selectedLoginStore)}",
                            fontSize = 14.sp,
                            maxLines = 1,
                            color = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = expandedStoreDropdown,
                        onDismissRequest = { expandedStoreDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.85f).background(Color(0xFF0F172A)).border(BorderStroke(1.dp, ImmersiveCardBorder))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Admin Control Portal", color = Color.White) },
                            onClick = {
                                selectedLoginStore = "admin"
                                expandedStoreDropdown = false
                            }
                        )
                        viewModel.STORE_CODES.forEach { code ->
                            DropdownMenuItem(
                                text = { Text("$code - ${viewModel.getStoreName(code)}", color = Color.White) },
                                onClick = {
                                    selectedLoginStore = code
                                    expandedStoreDropdown = false
                                }
                            )
                        }
                    }
                }

                // Password / Code field
                Text(
                    text = "PASSWORD / STORE CODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter credentials", color = ImmersiveTextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("password_input"),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ImmersiveEmerald,
                        unfocusedBorderColor = ImmersiveCardBorder,
                        focusedPlaceholderColor = ImmersiveTextSecondary,
                        unfocusedPlaceholderColor = ImmersiveTextSecondary
                    ),
                    singleLine = true
                )

                if (errorVisible) {
                    Text(
                        text = "Invalid store code or password. Please try again.",
                        color = WarningRed,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Button(
                    onClick = {
                        val userToFind = if (selectedLoginStore == "admin") "admin" else selectedLoginStore
                        viewModel.login(userToFind, password) { success ->
                            if (success) {
                                errorVisible = false
                            } else {
                                errorVisible = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveEmerald)
                ) {
                    Text("🔐 Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ImmersiveBg)
                }

                TextButton(
                    onClick = { showForgotDialog = true },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Forgot Password?", color = ImmersiveEmeraldLight, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showForgotDialog) {
        Dialog(onDismissRequest = { showForgotDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .border(BorderStroke(1.dp, ImmersiveCardBorder), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Password Self Reset", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                    Text(
                        "Please input the default master/manager passcode to perform administrative reset.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    OutlinedTextField(
                        value = masterCode,
                        onValueChange = { masterCode = it },
                        label = { Text("Master Account Secret Code") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showForgotDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            onClick = {
                                if (masterCode == "337" || masterCode == "#337#") {
                                    viewModel.forceResetPassword("admin") { resultMsg ->
                                        Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
                                    }
                                    showForgotDialog = false
                                } else {
                                    Toast.makeText(context, "Invalid master passcode", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Reset Admin Code", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: SalesViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val storeCode by viewModel.selectedStoreCode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showStoreSelector by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBg)
            .drawBehind {
                // Top-right glowing emerald orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1A10B981), Color.Transparent),
                        center = Offset(size.width * 1.0f, size.height * -0.05f),
                        radius = size.minDimension * 0.7f
                    ),
                    radius = size.minDimension * 0.7f,
                    center = Offset(size.width * 1.0f, size.height * -0.05f)
                )
                // Bottom-left glowing blue orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x143B82F6), Color.Transparent),
                        center = Offset(size.width * -0.05f, size.height * 1.05f),
                        radius = size.minDimension * 0.8f
                    ),
                    radius = size.minDimension * 0.8f,
                    center = Offset(size.width * -0.05f, size.height * 1.05f)
                )
            }
    ) {
        // App Header Bar (Glass-slate with soft border)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A).copy(alpha = 0.40f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Reporter ",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "PRO",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = ImmersiveEmerald
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(ImmersiveEmerald.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                                .border(BorderStroke(1.dp, ImmersiveEmerald.copy(alpha = 0.4f)), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("SAIDUR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = ImmersiveEmerald)
                        }
                    }
                    Text(
                        text = "Store: $storeCode - ${viewModel.getStoreName(storeCode)}",
                        fontSize = 12.sp,
                        color = ImmersiveTextSecondary,
                        modifier = Modifier.clickable { showStoreSelector = true }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Switch Store selector trigger
                    IconButton(onClick = { showStoreSelector = true }) {
                        Icon(Icons.Filled.List, contentDescription = "Switch Store", tint = Color.White)
                    }
                    // Admin settings
                    if (currentUser?.role == "admin") {
                        IconButton(onClick = { showSettingsModal = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }
                    // Logout
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Logout", tint = ImmersiveRed)
                    }
                }
            }
        }

        // Dashboard Stats Overview Card (Dynamic calculated MTD totals)
        QuickStatsOverviewRow(viewModel = viewModel)

        // Navigation tab bar
        ScrollableTabRow(viewModel = viewModel)

        // Active workspace view switcher
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (activeTab) {
                "entry" -> EntryTab(viewModel = viewModel)
                "bulk" -> BulkTab(viewModel = viewModel)
                "preview" -> PreviewTab(viewModel = viewModel)
                "dashboard" -> DashboardTab(viewModel = viewModel)
                "trends" -> TrendsTab(viewModel = viewModel)
                "feedback_admin" -> FeedbackAdminTab(viewModel = viewModel)
                "budget" -> BudgetTab(viewModel = viewModel)
                "history" -> HistoryTab(viewModel = viewModel)
                "multistore" -> MultiStoreTab(viewModel = viewModel)
            }
        }
    }

    // Modal trigger: Authorized Store Selector
    if (showStoreSelector) {
        Dialog(onDismissRequest = { showStoreSelector = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, ImmersiveCardBorder), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Select Active Store", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val allowedStores = currentUser?.getAllowedStores() ?: emptyList()
                    val targetList = if (currentUser?.role == "admin") viewModel.STORE_CODES else allowedStores
                    
                    Box(modifier = Modifier.heightIn(max = 280.dp)) {
                        ScrollableColumnWrapper {
                            targetList.forEach { code ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectedStoreCode.value = code
                                            viewModel.clearDailyForm()
                                            showStoreSelector = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("$code - ${viewModel.getStoreName(code)}", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                    if (storeCode == code) {
                                        Icon(Icons.Filled.Check, contentDescription = "Active", tint = ImmersiveEmerald)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(ImmersiveCardBorder)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Settings Modal (Administrative options & Password controls)
    if (showSettingsModal && currentUser?.role == "admin") {
        AdminSettingsDialog(viewModel = viewModel) {
            showSettingsModal = false
        }
    }
}

@Composable
fun QuickStatsOverviewRow(viewModel: SalesViewModel) {
    val mtdReportsList by viewModel.mtdReports.collectAsStateWithLifecycle()
    val mtdSalesVal by viewModel.mtdNetSales.collectAsStateWithLifecycle()
    val mtdGuestsVal by viewModel.mtdGuestCount.collectAsStateWithLifecycle()
    val mtdAchVal by viewModel.mtdAchievement.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Total Reports",
            value = mtdReportsList.size.toString(),
            color = SuccessGreen,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "MTD Sales",
            value = "$${viewModel.formatDouble(mtdSalesVal, 0)}",
            color = NavySecondary,
            modifier = Modifier.weight(1.2f)
        )
        StatCard(
            title = "Achieve%",
            value = "${viewModel.formatDouble(mtdAchVal, 1)}%",
            color = GoldAccent,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "MTD Guests",
            value = mtdGuestsVal.toString(),
            color = Color(0xFF6B7C9A),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ImmersiveCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .drawBehind {
                drawLine(
                    color = color,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 10f
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White, maxLines = 1)
            Text(text = title.uppercase(), fontSize = 9.sp, color = ImmersiveTextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun ScrollableTabRow(viewModel: SalesViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    // Config flags for section customizability
    val showTrendsConfig by viewModel.configShowTrends.collectAsStateWithLifecycle()
    val showFeedbackConfig by viewModel.configShowFeedbackAnalytics.collectAsStateWithLifecycle()
    val showBulkConfig by viewModel.configShowBulkTab.collectAsStateWithLifecycle()
    val showBudgetConfig by viewModel.configShowBudgetTab.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val role = currentUser?.role ?: "user"
        val tabs = mutableListOf<Pair<String, String>>()

        when (role) {
            "admin" -> {
                tabs.add("entry" to "📝 Entry")
                if (showBulkConfig) tabs.add("bulk" to "📥 Bulk")
                tabs.add("preview" to "👁 Preview")
                tabs.add("dashboard" to "📊 Dashboard")
                if (showTrendsConfig) tabs.add("trends" to "📈 Trends")
                if (showFeedbackConfig) tabs.add("feedback_admin" to "💬 Feedback")
                if (showBudgetConfig) tabs.add("budget" to "🎯 Budget")
                tabs.add("history" to "📚 History")
                tabs.add("multistore" to "🏬 All Stores")
            }
            "manager" -> {
                // Managers can enter/preview, see metrics, trends, feedback summary, budget, and history, but not All-Stores management or Admin settings
                tabs.add("entry" to "📝 Entry")
                tabs.add("preview" to "👁 Preview")
                tabs.add("dashboard" to "📊 Dashboard")
                if (showTrendsConfig) tabs.add("trends" to "📈 Trends")
                if (showFeedbackConfig) tabs.add("feedback_admin" to "💬 Feedback")
                if (showBudgetConfig) tabs.add("budget" to "🎯 Budget")
                tabs.add("history" to "📚 History")
            }
            else -> { // "user"
                // Store-level users can ONLY input and preview daily data — they cannot access analysis, history, or configurations
                tabs.add("entry" to "📝 Entry")
                tabs.add("preview" to "👁 Preview")
            }
        }

        tabs.forEach { (tabId, tabName) ->
            val isSelected = activeTab == tabId
            Button(
                onClick = { viewModel.activeTab.value = tabId },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) ImmersiveEmerald else Color(0x330F172A),
                    contentColor = if (isSelected) ImmersiveBg else ImmersiveTextSecondary
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isSelected) ImmersiveEmerald else ImmersiveCardBorder),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(38.dp).testTag("tab_btn_$tabId")
            ) {
                Text(text = tabName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- TAB WORKSPACE VIEW IMPLEMENTATIONS ---

@Composable
fun EntryTab(viewModel: SalesViewModel) {
    val date by viewModel.formDate.collectAsStateWithLifecycle()
    val shift by viewModel.formShift.collectAsStateWithLifecycle()
    val storeCode by viewModel.selectedStoreCode.collectAsStateWithLifecycle()

    // Sales metrics string inputs
    val netSales by viewModel.formNetSales.collectAsStateWithLifecycle()
    val budgetSale by viewModel.formBudgetSale.collectAsStateWithLifecycle()
    val deliverySale by viewModel.formHomeDelivery.collectAsStateWithLifecycle()
    val guestsStr by viewModel.formGuestCount.collectAsStateWithLifecycle()
    val ltoSale by viewModel.formLtoSale.collectAsStateWithLifecycle()
    val mochaQty by viewModel.formMochaQty.collectAsStateWithLifecycle()
    val mixedQty by viewModel.formMixedBoxQty.collectAsStateWithLifecycle()
    val feedbackStr by viewModel.formFeedback.collectAsStateWithLifecycle()
    val feedbackComment by viewModel.formFeedbackComment.collectAsStateWithLifecycle()

    // Auto outputs
    val dineInVal by viewModel.currentDineInValue.collectAsStateWithLifecycle()
    val varianceVal by viewModel.currentVariance.collectAsStateWithLifecycle()
    val achievementVal by viewModel.currentAchievement.collectAsStateWithLifecycle()
    val avgCheckVal by viewModel.currentAvgCheck.collectAsStateWithLifecycle()
    val mochaPct by viewModel.currentMochaPct.collectAsStateWithLifecycle()
    val mixedPct by viewModel.currentMixedBoxPct.collectAsStateWithLifecycle()

    // Edit indicator
    val editId by viewModel.editingReportId.collectAsStateWithLifecycle()

    ScrollableColumnWrapper {
        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, ImmersiveCardBorder), RoundedCornerShape(16.dp))
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🏪 Store & Date setup", fontWeight = FontWeight.Bold, color = ImmersiveEmeraldLight, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = {
                            viewModel.formDate.value = it
                            viewModel.loadExistingReportForDate()
                        },
                        label = { Text("Date (YYYY-MM-DD)", color = ImmersiveTextSecondary) },
                        modifier = Modifier.weight(1f).testTag("date_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ImmersiveEmerald,
                            unfocusedBorderColor = ImmersiveCardBorder
                        )
                    )
                    
                    var expandedShift by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                        OutlinedButton(
                            onClick = { expandedShift = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("shift_selector"),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ImmersiveCardBorder)
                        ) {
                            Text(shift, color = Color.White, fontSize = 13.sp)
                        }
                        DropdownMenu(
                            expanded = expandedShift,
                            onDismissRequest = { expandedShift = false },
                            modifier = Modifier.background(Color(0xFF0F172A)).border(BorderStroke(1.dp, ImmersiveCardBorder))
                        ) {
                            listOf("Morning", "Night", "Full Day").forEach { choice ->
                                DropdownMenuItem(
                                    text = { Text(choice, color = Color.White) },
                                    onClick = {
                                        viewModel.formShift.value = choice
                                        viewModel.loadExistingReportForDate()
                                        expandedShift = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Smart load trigger
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = ImmersiveEmerald),
            shape = RoundedCornerShape(10.dp),
            onClick = { viewModel.smartLoadMTDValues() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text("📋 Smart Load MTD counters", fontWeight = FontWeight.Bold, color = ImmersiveBg)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, ImmersiveCardBorder), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💰 Daily figures", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Net Sales", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = netSales,
                            onValueChange = { viewModel.formNetSales.value = it },
                            modifier = Modifier.fillMaxWidth().testTag("net_sales_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Budget Sale", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = budgetSale,
                            onValueChange = { viewModel.formBudgetSale.value = it },
                            modifier = Modifier.fillMaxWidth().testTag("budget_sale_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Home Delivery", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = deliverySale,
                            onValueChange = { viewModel.formHomeDelivery.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Guest Count", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = guestsStr,
                            onValueChange = { viewModel.formGuestCount.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calculated Read-only blocks
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dine IN (Auto)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("$${viewModel.formatDouble(dineInVal)}", fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Variance", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = viewModel.formatSignDouble(varianceVal),
                                fontWeight = FontWeight.Bold,
                                color = if (varianceVal >= 0) SuccessGreen else WarningRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Achievement", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("${viewModel.formatDouble(achievementVal)}%", fontWeight = FontWeight.Bold, color = NavySecondary)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AVG Check", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("$${viewModel.formatDouble(avgCheckVal)}", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Text("🎯 Product metrics & attachments", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("LTO Sale", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = ltoSale,
                            onValueChange = { viewModel.formLtoSale.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Customer feedback", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = feedbackStr,
                            onValueChange = { viewModel.formFeedback.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mocha Qty", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = mochaQty,
                            onValueChange = { viewModel.formMochaQty.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Text("Mocha: ${viewModel.formatDouble(mochaPct, 1)}%", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mixed Box Qty", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = mixedQty,
                            onValueChange = { viewModel.formMixedBoxQty.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Text("MixBox: ${viewModel.formatDouble(mixedPct, 1)}%", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Feedback Comment Box
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Customer Feedback Comments", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImmersiveTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = feedbackComment,
                        onValueChange = { viewModel.formFeedbackComment.value = it },
                        placeholder = { Text("Enter manager summary / customer themes / suggestions...", color = ImmersiveTextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .testTag("feedback_comment_input"),
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ImmersiveEmerald,
                            unfocusedBorderColor = ImmersiveCardBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveOrUpdateDailyReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).testTag("save_report_btn")
                    ) {
                        Text(if (editId != null) "✅ Update Report" else "💾 Save Report", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    
                    if (editId != null) {
                        OutlinedButton(
                            onClick = { viewModel.clearDailyFormKeepDate() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearDailyForm() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🗑 Reset", color = WarningRed)
                    }
                }
            }
        }
    }
}

@Composable
fun BulkTab(viewModel: SalesViewModel) {
    val mode by viewModel.bulkMode.collectAsStateWithLifecycle()
    val rawText by viewModel.bulkInputField.collectAsStateWithLifecycle()
    val parsedList by viewModel.bulkParseResults.collectAsStateWithLifecycle()

    ScrollableColumnWrapper {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📥 Bulk Data Import", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isCsv = mode == "csv"
                    Button(
                        onClick = { viewModel.bulkMode.value = "csv" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isCsv) NavySecondary else Color.Transparent),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("CSV Format", color = if (isCsv) Color.White else Color.Gray, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { viewModel.bulkMode.value = "whatsapp" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isCsv) NavySecondary else Color.Transparent),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("WhatsApp Text", color = if (!isCsv) Color.White else Color.Gray, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                val specText = if (mode == "csv") {
                    "CSV Line Expected format:\nDate,Shift,Net Sales,Budget,Dine IN,Delivery,Guests,LTO Sale,Mocha Qty,Mixed Box Qty"
                } else {
                    "WhatsApp Expected template: Paste a standard WhatsApp output message generated from this app to automatically extract metrics!"
                }
                Text(
                    text = specText,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.background(Color(0xFFFAF8F3), RoundedCornerShape(6.dp)).padding(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { viewModel.bulkInputField.value = it },
                    placeholder = { Text("Paste bulk contents directly here…") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    maxLines = 10,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.parseBulkInput() },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🔍 Parse Entries Overview", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        if (parsedList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Parsed Results Preview (${parsedList.size} items)",
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    parsedList.forEach { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${p.date} (${p.shift})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Sales: $${viewModel.formatDouble(p.netSales, 0)} (Bud: $${viewModel.formatDouble(p.budgetSale, 0)})", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.saveBulkParsedReports() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("💾 Save All Parsed Reports", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewTab(viewModel: SalesViewModel) {
    val text = viewModel.getWhatsAppText()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    ScrollableColumnWrapper {
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👁 WhatsApp Live Preview", fontWeight = FontWeight.Bold, color = GoldHighlight, fontSize = 14.sp)
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Copied report message!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Copy text", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F2547), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    clipboardManager.setText(AnnotatedString(text))
                    try {
                        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                            `package` = "com.whatsapp"
                        }
                        context.startActivity(whatsappIntent)
                    } catch (e: Exception) {
                        // fallback chooser
                        val shareIntent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }, "Share via")
                        context.startActivity(shareIntent)
                    }
                }
            ) {
                Text("📤 WhatsApp Share", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied directly to Clipboard!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("📋 Copy Code Text", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun DashboardTab(viewModel: SalesViewModel) {
    val mtdReportsList by viewModel.mtdReports.collectAsStateWithLifecycle()
    val mtdSales by viewModel.mtdNetSales.collectAsStateWithLifecycle()
    val mtdBud by viewModel.mtdBudget.collectAsStateWithLifecycle()
    val mtdAch by viewModel.mtdAchievement.collectAsStateWithLifecycle()
    val mtdFeed by viewModel.mtdFeedbackAvg.collectAsStateWithLifecycle()
    val storeCode by viewModel.selectedStoreCode.collectAsStateWithLifecycle()
    val selectedMon by viewModel.selectedMonth.collectAsStateWithLifecycle()

    var showMonthPicker by remember { mutableStateOf(false) }

    ScrollableColumnWrapper {
        // Month Selector card
        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, ImmersiveCardBorder), RoundedCornerShape(16.dp))
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Select Analysis Month".uppercase(), fontWeight = FontWeight.Bold, color = ImmersiveTextSecondary, fontSize = 9.sp)
                    Text(selectedMon, fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
                }
                Button(
                    onClick = { showMonthPicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveEmerald),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📅 Select", color = ImmersiveBg, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Custom canvas trend chart
        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, ImmersiveCardBorder), RoundedCornerShape(16.dp))
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📈 Daily Performance Trend (Net Sales)".uppercase(), fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                
                if (mtdReportsList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No reports saved for this month yet.", color = ImmersiveTextSecondary, fontSize = 13.sp)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        SalesTrendCanvasChart(reports = mtdReportsList)
                    }
                }
            }
        }

        // Sector KPIs Breakdown Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, ImmersiveCardBorder), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📊 KPI Achievement Summary".uppercase(), fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiIndicatorCard(label = "Actual Sales", value = "$${viewModel.formatDouble(mtdSales, 0)}", modifier = Modifier.weight(1f))
                    KpiIndicatorCard(label = "Target Budget", value = "$${viewModel.formatDouble(mtdBud, 0)}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiIndicatorCard(label = "Achievement", value = "${viewModel.formatDouble(mtdAch, 1)}%", modifier = Modifier.weight(1f))
                    KpiIndicatorCard(label = "Avg Feedback", value = "${viewModel.formatDouble(mtdFeed, 1)} / 5", modifier = Modifier.weight(1f))
                }
            }
        }
    }

    if (showMonthPicker) {
        Dialog(onDismissRequest = { showMonthPicker = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Select Target Month", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val monthsList = listOf("2026-01", "2026-02", "2026-03", "2026-04", "2026-05", "2026-06", "2026-07", "2026-08", "2026-09", "2026-10", "2026-11", "2026-12")
                    Box(modifier = Modifier.heightIn(max = 240.dp)) {
                        ScrollableColumnWrapper {
                            monthsList.forEach { m ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectedMonth.value = m
                                            showMonthPicker = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                ) {
                                    Text(m, fontWeight = if (selectedMon == m) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiIndicatorCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(BorderStroke(1.dp, ImmersiveCardBorder), RoundedCornerShape(12.dp))
            .background(Color(0x1A718096), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(label.uppercase(), fontSize = 9.sp, color = ImmersiveTextSecondary, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun SalesTrendCanvasChart(reports: List<SalesReport>) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
            .padding(bottom = 20.dp, top = 10.dp, start = 30.dp, end = 10.dp)
    ) {
        val maxSales = reports.maxOf { it.netSales }.coerceAtLeast(100.0)
        val sorted = reports.sortedBy { it.date }
        val barCount = sorted.size
        
        val width = size.width
        val height = size.height
        val barWidth = (width / barCount.coerceAtLeast(1)) * 0.7f
        val gap = (width / barCount.coerceAtLeast(1)) * 0.3f

        // Draw basic lines
        drawLine(
            color = Color(0xFFE2E8F0),
            start = Offset(0f, 0f),
            end = Offset(0f, height),
            strokeWidth = 2f
        )
        drawLine(
            color = Color(0xFFE2E8F0),
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 2f
        )

        // Draw grids
        val gridCount = 4
        for (i in 1..gridCount) {
            val gridY = height - (height / gridCount) * i
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(0f, gridY),
                end = Offset(width, gridY),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Render actual data bars
        sorted.forEachIndexed { idx, item ->
            val barHeight = ((item.netSales / maxSales) * height).toFloat()
            val x = idx * (barWidth + gap)
            val y = height - barHeight

            drawRoundRect(
                color = if (item.netSales >= item.budgetSale) SuccessGreen else NavySecondary,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            
            // Draw brief label indices for every alternate bar to look clean
            if (idx % 2 == 0) {
                drawIntoCanvas { canvas ->
                    val dateOnly = item.date.substringAfterLast("-")
                    canvas.nativeCanvas.drawText(
                        dateOnly,
                        x + barWidth / 2 - 6f,
                        height + 14.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 8.dp.toPx()
                            isAntiAlias = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetTab(viewModel: SalesViewModel) {
    val storeCode by viewModel.selectedStoreCode.collectAsStateWithLifecycle()
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val activeBudgetPlan by viewModel.activeStoreBudgetPlan.collectAsStateWithLifecycle()
    val reportsList by viewModel.mtdReports.collectAsStateWithLifecycle()
    val currentMtdSales by viewModel.mtdNetSales.collectAsStateWithLifecycle()

    var customTarget by remember { mutableStateOf("") }
    var workingDays by remember { mutableStateOf("26") }

    LaunchedEffect(activeBudgetPlan) {
        if (activeBudgetPlan != null) {
            customTarget = activeBudgetPlan?.target?.toString() ?: ""
            workingDays = activeBudgetPlan?.workingDays?.toString() ?: "26"
        } else {
            customTarget = ""
            workingDays = "26"
        }
    }

    ScrollableColumnWrapper {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎯 Monthly Budget Planner", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = month,
                        onValueChange = { viewModel.selectedMonth.value = it },
                        label = { Text("Active Month") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        readOnly = true
                    )
                    OutlinedTextField(
                        value = workingDays,
                        onValueChange = { workingDays = it },
                        label = { Text("Working Days") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customTarget,
                    onValueChange = { customTarget = it },
                    label = { Text("Monthly Budget Target ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.saveTargetBudget(customTarget, workingDays) },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("💾 Save Budget Settings", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress breakdown details
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📈 Achievement Progress bar", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                val budgetVal = customTarget.toDoubleOrNull() ?: 1.0
                val progressFraction = (currentMtdSales / budgetVal).coerceIn(0.0, 1.0).toFloat()
                
                // ProgressBar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(SuccessGreen, SuccessGreenLight)
                                ),
                                RoundedCornerShape(8.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Achieved: $${viewModel.formatDouble(currentMtdSales, 0)} / $${viewModel.formatDouble(budgetVal, 0)}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Progress: ${viewModel.formatDouble(progressFraction * 100.0, 1)}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryTab(viewModel: SalesViewModel) {
    val reportsList by viewModel.mtdReports.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text("📚 Saved Reports for current month", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        
        if (reportsList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No historical data entries saved.", color = Color.Gray)
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                ScrollableColumnWrapper {
                    reportsList.forEach { r ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = r.date, fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 14.sp)
                                    Text(
                                        text = "Shift: ${r.shift} | Sales: $${viewModel.formatDouble(r.netSales, 0)}",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Text(
                                        text = "Achieve: ${viewModel.formatDouble(r.achievementPercentage, 1)}% | Guests: ${r.guestCount}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (r.achievementPercentage >= 100) SuccessGreen else WarningRed,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            viewModel.formDate.value = r.date
                                            viewModel.formShift.value = r.shift
                                            viewModel.editingReportId.value = r.id
                                            viewModel.formNetSales.value = r.netSales.toString()
                                            viewModel.formBudgetSale.value = r.budgetSale.toString()
                                            viewModel.formHomeDelivery.value = r.homeDelivery.toString()
                                            viewModel.formGuestCount.value = r.guestCount.toString()
                                            viewModel.formDineInOverride.value = r.dineIn
                                            viewModel.formLtoSale.value = r.ltoSale.toString()
                                            viewModel.formMochaQty.value = r.mochaQty.toString()
                                            viewModel.formMixedBoxQty.value = r.mixedBoxQty.toString()
                                            viewModel.formFeedback.value = r.feedback.toString()
                                            viewModel.activeTab.value = "entry"
                                        }
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = NavySecondary)
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteReport(r.id) }
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = WarningRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action tools for export
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val csv = viewModel.generateBackupCsv()
                    clipboardManager.setText(AnnotatedString(csv))
                    Toast.makeText(context, "Historical CSV copied to Clipboard!", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("📋 Export CSV List", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun MultiStoreTab(viewModel: SalesViewModel) {
    val allReportsList by viewModel.allReports.collectAsStateWithLifecycle()
    val selectedMonthActive by viewModel.selectedMonth.collectAsStateWithLifecycle()

    ScrollableColumnWrapper {
        Text(
            text = "Leaderboard: Store achievements for $selectedMonthActive",
            fontWeight = FontWeight.Bold,
            color = NavyDark,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        viewModel.STORE_CODES.forEach { code ->
            // Filter reports of active store/month
            val storeReports = allReportsList.filter { r ->
                r.storeCode == code && r.date.startsWith(selectedMonthActive)
            }
            val totalSales = storeReports.sumOf { it.netSales }
            val totalBudget = storeReports.sumOf { it.budgetSale }
            val ach = if (totalBudget > 0) (totalSales / totalBudget) * 100.0 else 0.0

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(text = "$code - ${viewModel.getStoreName(code)}", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 14.sp)
                        Text(text = "${storeReports.size} daily submissions saved", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(text = "$${viewModel.formatDouble(totalSales, 0)}", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 14.sp)
                        Text(
                            text = "${viewModel.formatDouble(ach, 1)}% Ach",
                            fontWeight = FontWeight.Bold,
                            color = if (ach >= 100.0) SuccessGreen else WarningRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// Dialog for Administrative Management & Controls (Settings trigger)
@Composable
fun AdminSettingsDialog(viewModel: SalesViewModel, onDismiss: () -> Unit) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    // User addition states
    var addUsername by remember { mutableStateOf("") }
    var addPassword by remember { mutableStateOf("") }
    var assignedStoreCsv by remember { mutableStateOf("") }
    var assignedRole by remember { mutableStateOf("user") }

    val context = LocalContext.current
    val allActiveUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                ScrollableColumnWrapper(modifier = Modifier.padding(20.dp)) {
                    Text("⚙️ Administrative Settings Portal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyDark)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Change Password Block
                    Text("🔒 Change Account Password", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Target Username") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Old Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.changePassword(username, oldPassword, newPassword) { status, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (status) {
                                    oldPassword = ""
                                    newPassword = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Update credentials", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic User listing & additions
                    Text("👥 Manage System Logins", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    allActiveUsers.forEach { user ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${user.username} (${user.role}) - Store codes: [${user.storesCsv}]", fontSize = 12.sp)
                            IconButton(
                                onClick = { viewModel.deleteUser(user) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete User", tint = WarningRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("➕ Add Login Credentials", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = addUsername,
                        onValueChange = { addUsername = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = addPassword,
                        onValueChange = { addPassword = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = assignedStoreCsv,
                        onValueChange = { assignedStoreCsv = it },
                        label = { Text("Assigned Store CSV (e.g. 311,317)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.addNewUser(addUsername, addPassword, assignedStoreCsv, assignedRole) { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                addUsername = ""
                                addPassword = ""
                                assignedStoreCsv = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Account", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // System Data Control resets
                    Text("🚨 System database controls", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarningRed)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.systemResetAll()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("⚠️ Hard clean delete data bases", color = WarningRed)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close Portal", color = Color.Gray)
                    }
                }
            }
        }
    }
}

// Helpers wrappers to handle scroll constraints inside dialogs/scroll blocks safely
@Composable
fun ScrollableColumnWrapper(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        content()
    }
}

// --- INTERACTIVE TRENDS & DYNAMIC CHARTS ---

data class TrendPoint(
    val label: String,
    val fullDate: String,
    val sales: Double,
    val budget: Double
)

fun processTrendsData(storeReports: List<SalesReport>, timeframe: String): List<TrendPoint> {
    if (storeReports.isEmpty()) return emptyList()
    return when (timeframe) {
        "7days" -> storeReports.groupBy { it.date }
            .map { (d, r) -> TrendPoint(if (d.length >= 10) d.substring(5) else d, d, r.sumOf { it.netSales }, r.sumOf { it.budgetSale }) }
            .sortedBy { it.fullDate }.takeLast(7)
        "30days" -> storeReports.groupBy { it.date }
            .map { (d, r) -> TrendPoint(if (d.length >= 10) d.substring(5) else d, d, r.sumOf { it.netSales }, r.sumOf { it.budgetSale }) }
            .sortedBy { it.fullDate }.takeLast(30)
        "year" -> storeReports.groupBy { it.date.substring(0, minOf(7, it.date.length)) }
            .map { (m, r) -> 
                val monthLabels = mapOf("01" to "Jan", "02" to "Feb", "03" to "Mar", "04" to "Apr", "05" to "May", "06" to "Jun", "07" to "Jul", "08" to "Aug", "09" to "Sep", "10" to "Oct", "11" to "Nov", "12" to "Dec")
                val labelName = monthLabels[if (m.length >= 7) m.substring(5) else m] ?: m
                TrendPoint(labelName, m, r.sumOf { it.netSales }, r.sumOf { it.budgetSale })
            }.sortedBy { it.fullDate }.takeLast(12)
        else -> emptyList()
    }
}

@Composable
fun TrendsTab(viewModel: SalesViewModel) {
    val reports by viewModel.allReports.collectAsStateWithLifecycle()
    val activeStore by viewModel.selectedStoreCode.collectAsStateWithLifecycle()
    var timeframe by remember { mutableStateOf("7days") }

    val storeReports = remember(reports, activeStore) {
        reports.filter { it.storeCode == activeStore }.sortedBy { it.date }
    }
    val chartData = remember(storeReports, timeframe) {
        processTrendsData(storeReports, timeframe)
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ImmersiveCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("TIME RANGE VISUALIZER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ImmersiveTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("7days" to "Last 7D", "30days" to "Last 30D", "year" to "12 Months").forEach { (id, lbl) ->
                        val isSel = timeframe == id
                        Button(
                            onClick = { timeframe = id },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) ImmersiveEmerald else Color(0x330F172A),
                                contentColor = if (isSel) ImmersiveBg else ImmersiveTextSecondary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isSel) ImmersiveEmerald else ImmersiveCardBorder),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ImmersiveCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("SALES vs TARGET TREND", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImmersiveEmeraldLight)
                Spacer(modifier = Modifier.height(14.dp))
                if (chartData.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        Text("No reports available for Store $activeStore.", color = ImmersiveTextSecondary, fontSize = 11.sp)
                    }
                } else {
                    InteractiveSalesChart(chartData = chartData)
                }
            }
        }
    }
}

@Composable
fun InteractiveSalesChart(chartData: List<TrendPoint>) {
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    var touchX by remember { mutableStateOf(-1f) }

    val maxSales = remember(chartData) {
        val maxVal = maxOf(chartData.maxOf { maxOf(it.sales, it.budget) }, 100.0)
        maxVal * 1.15
    }

    Box(modifier = Modifier.fillMaxWidth().height(270.dp)) {
        Column {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .pointerInput(chartData) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                touchX = offset.x
                                hoveredIndex = (touchX / (size.width / (chartData.size - 1).coerceAtLeast(1))).roundToInt().coerceIn(0, chartData.size - 1)
                            },
                            onDrag = { change, _ ->
                                touchX = change.position.x
                                hoveredIndex = (touchX / (size.width / (chartData.size - 1).coerceAtLeast(1))).roundToInt().coerceIn(0, chartData.size - 1)
                            },
                            onDragEnd = { hoveredIndex = null; touchX = -1f },
                            onDragCancel = { hoveredIndex = null; touchX = -1f }
                        )
                    }
                    .pointerInput(chartData) {
                        detectTapGestures { offset ->
                            touchX = offset.x
                            hoveredIndex = (touchX / (size.width / (chartData.size - 1).coerceAtLeast(1))).roundToInt().coerceIn(0, chartData.size - 1)
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val pointsCount = chartData.size

                for (i in 0..4) {
                    val y = (canvasHeight / 4) * i
                    drawLine(color = Color.White.copy(alpha = 0.08f), start = Offset(0f, y), end = Offset(canvasWidth, y), strokeWidth = 1.dp.toPx())
                }

                if (pointsCount > 1) {
                    val dx = canvasWidth / (pointsCount - 1)
                    val sCoords = chartData.mapIndexed { idx, pt -> Offset(idx * dx, canvasHeight - (pt.sales.toFloat() / maxSales.toFloat() * canvasHeight)) }
                    val bCoords = chartData.mapIndexed { idx, pt -> Offset(idx * dx, canvasHeight - (pt.budget.toFloat() / maxSales.toFloat() * canvasHeight)) }

                    val pathSalesFill = Path().apply {
                        moveTo(0f, canvasHeight)
                        sCoords.forEach { pt -> lineTo(pt.x, pt.y) }
                        lineTo(canvasWidth, canvasHeight)
                        close()
                    }
                    drawPath(path = pathSalesFill, brush = Brush.verticalGradient(colors = listOf(ImmersiveEmerald.copy(alpha = 0.20f), Color.Transparent)))

                    sCoords.forEachIndexed { i, cur ->
                        if (i < sCoords.size - 1) {
                            drawLine(color = ImmersiveEmerald, start = cur, end = sCoords[i + 1], strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                        }
                    }
                    bCoords.forEachIndexed { i, cur ->
                        if (i < bCoords.size - 1) {
                            drawLine(color = Color(0xFF3B82F6).copy(alpha = 0.5f), start = cur, end = bCoords[i + 1], strokeWidth = 1.8.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f), cap = StrokeCap.Round)
                        }
                    }

                    hoveredIndex?.let { hIdx ->
                        if (hIdx in 0 until pointsCount) {
                            val hPt = sCoords[hIdx]
                            drawLine(color = ImmersiveEmeraldLight.copy(alpha = 0.40f), start = Offset(hPt.x, 0f), end = Offset(hPt.x, canvasHeight), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
                            drawCircle(color = ImmersiveBg, radius = 6.dp.toPx(), center = hPt)
                            drawCircle(color = ImmersiveEmerald, radius = 4.dp.toPx(), center = hPt)
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                chartData.forEachIndexed { idx, pt ->
                    if (chartData.size <= 8 || idx == 0 || idx == chartData.lastIndex || idx == chartData.size / 2 || (chartData.size == 30 && idx % 10 == 0)) {
                        Text(text = pt.label, fontSize = 9.sp, color = ImmersiveTextSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val selectIdx = hoveredIndex
            if (selectIdx != null && selectIdx in chartData.indices) {
                val pt = chartData[selectIdx]
                val variance = pt.sales - pt.budget
                val ach = if (pt.budget > 0) (pt.sales / pt.budget) * 100 else 0.0
                Box(modifier = Modifier.fillMaxWidth().background(ImmersiveEmerald.copy(alpha = 0.12f), RoundedCornerShape(10.dp)).border(BorderStroke(1.dp, ImmersiveEmerald.copy(alpha = 0.25f)), RoundedCornerShape(10.dp)).padding(10.dp)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("📅 ${pt.fullDate}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Achieved: ${String.format("%.1f", ach)}%", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (variance >= 0) ImmersiveEmeraldLight else WarningRed)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("NET SALES", fontSize = 8.sp, color = ImmersiveTextSecondary, fontWeight = FontWeight.Bold)
                                Text("$${String.format("%,.0f", pt.sales)}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Column {
                                Text("BUDGET", fontSize = 8.sp, color = ImmersiveTextSecondary, fontWeight = FontWeight.Bold)
                                Text("$${String.format("%,.0f", pt.budget)}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                            }
                            Column {
                                Text("VARIANCE", fontSize = 8.sp, color = ImmersiveTextSecondary, fontWeight = FontWeight.Bold)
                                Text((if (variance >= 0) "+" else "") + String.format("%,.0f", variance), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (variance >= 0) ImmersiveEmeraldLight else WarningRed)
                            }
                        }
                    }
                }
            } else {
                val avgSales = chartData.map { it.sales }.average()
                val totalSalesSum = chartData.sumOf { it.sales }
                val totalTargetSum = chartData.sumOf { it.budget }
                val overallAch = if (totalTargetSum > 0) (totalSalesSum / totalTargetSum) * 100.0 else 0.0
                Box(modifier = Modifier.fillMaxWidth().background(Color(0x0AFFFFFF), RoundedCornerShape(10.dp)).border(BorderStroke(1.dp, ImmersiveCardBorder), RoundedCornerShape(10.dp)).padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("AVG DAILY SALES", fontSize = 8.sp, color = ImmersiveTextSecondary, fontWeight = FontWeight.Bold)
                            Text("$${String.format("%,.0f", avgSales)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Column {
                            Text("OVERALL ACH%", fontSize = 8.sp, color = ImmersiveTextSecondary, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.1f", overallAch)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ImmersiveEmeraldLight)
                        }
                        Text("Touch point to inspect", fontSize = 9.sp, color = ImmersiveTextSecondary)
                    }
                }
            }
        }
    }
}

// --- CUSTOMER FEEDBACK & STATS ANALYTICS ---

@Composable
fun FeedbackAdminTab(viewModel: SalesViewModel) {
    val reports by viewModel.allReports.collectAsStateWithLifecycle()
    var showConfigBlock by remember { mutableStateOf(false) }

    val showTrendsConfig by viewModel.configShowTrends.collectAsStateWithLifecycle()
    val showFeedbackConfig by viewModel.configShowFeedbackAnalytics.collectAsStateWithLifecycle()
    val showBulkConfig by viewModel.configShowBulkTab.collectAsStateWithLifecycle()
    val showBudgetConfig by viewModel.configShowBudgetTab.collectAsStateWithLifecycle()

    val fReports = remember(reports) { reports.filter { it.feedback > 0 || it.feedbackComment.isNotEmpty() } }
    val storeAverages = remember(fReports) {
        fReports.filter { it.feedback > 0 }.groupBy { it.storeCode }.map { (st, r) -> st to r.map { it.feedback }.average() }.sortedByDescending { it.second }
    }

    val themes = remember(fReports) {
        val comments = fReports.map { it.feedbackComment.lowercase() }.filter { it.isNotEmpty() }
        val serviceMatches = comments.count { s -> listOf("staff", "service", "helpful", "friendly", "rude", "manager", "behaviour").any { s.contains(it) } }
        val qualityMatches = comments.count { s -> listOf("taste", "food", "delicious", "mocha", "mixed", "drink", "box", "quality").any { s.contains(it) } }
        val speedMatches = comments.count { s -> listOf("speed", "fast", "slow", "wait", "delay", "minutes", "time").any { s.contains(it) } }
        val cleanMatches = comments.count { s -> listOf("clean", "dirty", "hygiene", "smell", "table").any { s.contains(it) } }
        val total = (serviceMatches + qualityMatches + speedMatches + cleanMatches).coerceAtLeast(1)
        listOf(
            Triple("Service & Team Hospitality", serviceMatches, (serviceMatches.toFloat() / total * 100)),
            Triple("Product Quality & Taste", qualityMatches, (qualityMatches.toFloat() / total * 100)),
            Triple("Speed of Checkout/Dine-In", speedMatches, (speedMatches.toFloat() / total * 100)),
            Triple("Cleanliness & Store Ambient", cleanMatches, (cleanMatches.toFloat() / total * 100))
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ImmersiveCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("CUSTOMER REVIEWS OVERVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImmersiveEmeraldLight)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val countRatings = fReports.count { it.feedback > 0 }
                    val overallAvg = if (countRatings > 0) fReports.filter { it.feedback > 0 }.map { it.feedback }.average() else 0.0
                    Column(modifier = Modifier.weight(1f).background(Color(0x0AFFFFFF), RoundedCornerShape(10.dp)).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("AVERAGE SCORE", fontSize = 8.sp, color = ImmersiveTextSecondary)
                        Text(String.format("%.2f ★", overallAvg), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ImmersiveEmeraldLight)
                        Text("$countRatings ratings", fontSize = 8.sp, color = ImmersiveTextSecondary)
                    }
                    Column(modifier = Modifier.weight(1f).background(Color(0x0AFFFFFF), RoundedCornerShape(10.dp)).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("STORE COMMENTS", fontSize = 8.sp, color = ImmersiveTextSecondary)
                        Text("${fReports.count { it.feedbackComment.isNotEmpty() }} written", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Active logs", fontSize = 8.sp, color = ImmersiveTextSecondary)
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ImmersiveCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("RATING RUNDOWN BY STORE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ImmersiveTextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                if (storeAverages.isEmpty()) {
                    Text("No score ratings saved yet.", color = ImmersiveTextSecondary, fontSize = 11.sp)
                } else {
                    storeAverages.take(8).forEach { (store, avg) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Store $store - ${viewModel.getStoreName(store)}", fontSize = 11.sp, color = Color.White)
                            Text(String.format("%.1f ★", avg), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImmersiveEmeraldLight)
                        }
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ImmersiveCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("IDENTIFIED KEYWORDS & THEMES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ImmersiveTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                themes.forEach { (name, count, pct) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, fontSize = 11.sp, color = Color.White)
                            Text("$count matches (${pct.roundToInt()}%)", fontSize = 10.sp, color = ImmersiveTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(5.dp).background(Color(0x11FFFFFF), RoundedCornerShape(3.dp))) {
                            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(pct / 100f).background(ImmersiveEmerald, RoundedCornerShape(3.dp)))
                        }
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ImmersiveCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("RECENT WRITTEN FEEDBACK LOGS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ImmersiveTextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                val withComments = fReports.filter { it.feedbackComment.isNotEmpty() }.sortedByDescending { it.date }.take(10)
                if (withComments.isEmpty()) {
                    Text("No customer feedback text responses added.", color = ImmersiveTextSecondary, fontSize = 11.sp)
                } else {
                    withComments.forEach { rep ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).background(Color(0x0AFFFFFF), RoundedCornerShape(6.dp)).padding(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Store ${rep.storeCode} - ${rep.date}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ImmersiveEmeraldLight)
                                Text("${rep.feedback.toInt()} ★", fontSize = 9.sp, color = ImmersiveTextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("\"${rep.feedbackComment}\"", fontSize = 11.sp, color = Color.White, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                }
            }
        }

        // Feature Config customizability
        Card(
            colors = CardDefaults.cardColors(containerColor = ImmersiveCardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ImmersiveCardBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showConfigBlock = !showConfigBlock },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️ BUSINESS FEATURES CONFIGURATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Toggle Configs",
                        tint = Color.White,
                        modifier = Modifier.rotate(if (showConfigBlock) 180f else 0f)
                    )
                }
                if (showConfigBlock) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Modify sections and tabs currently accessible by branches. Functionalities can be enabled or disabled instantaneously as per changing business guidelines:", fontSize = 9.sp, color = ImmersiveTextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    ConfigToggleRow("Enable Trends Visualizer (Graph Tab)", showTrendsConfig) { viewModel.configShowTrends.value = it }
                    ConfigToggleRow("Enable Reviews Analyses summaries", showFeedbackConfig) { viewModel.configShowFeedbackAnalytics.value = it }
                    ConfigToggleRow("Enable Bulk Imports tab", showBulkConfig) { viewModel.configShowBulkTab.value = it }
                    ConfigToggleRow("Enable Target Budget planners", showBudgetConfig) { viewModel.configShowBudgetTab.value = it }
                }
            }
        }
    }
}

@Composable
fun ConfigToggleRow(label: String, value: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(1f))
        Switch(
            checked = value,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ImmersiveBg,
                checkedTrackColor = ImmersiveEmerald,
                uncheckedThumbColor = ImmersiveTextSecondary,
                uncheckedTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}
