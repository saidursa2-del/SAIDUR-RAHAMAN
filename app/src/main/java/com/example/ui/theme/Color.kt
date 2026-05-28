package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// IMMERSIVE UI PREMIUM COLOR THEME DEFINITIONS
// ==========================================
val ImmersiveBg = Color(0xFF050811)              // Deep Space Midnight Background
val ImmersiveCardBg = Color(0xFF0F172A).copy(alpha = 0.85f) // Glowing Semi-Transparent Glass Slate
val ImmersiveCardBorder = Color(0x1F718096)      // Subtle 12% border tint
val ImmersiveTextPrimary = Color(0xFFF8FAFC)     // Crisp Cool White (Slate-50)
val ImmersiveTextSecondary = Color(0xFF94A3B8)   // Slate-400
val ImmersiveEmerald = Color(0xFF10B981)         // Classic Emerald Accent (Reporter PRO)
val ImmersiveEmeraldLight = Color(0xFF34D399)    // Glowing Neo-Mint Accent (Emerald-400)
val ImmersiveBlue = Color(0xFF3B82F6)            // Blue secondary glow
val ImmersiveAmber = Color(0xFFF59E0B)           // Amber highlight
val ImmersiveRed = Color(0xFFEF4444)             // Alert red

// ==========================================
// LEGACY COMPATIBILITY ROUTING
// Mapping older variables to new immersive ones to prevent layout text-invisible issues
// and immediately convert all cards/backgrounds into deep-mode.
// ==========================================
val NavyDark = ImmersiveTextPrimary              // Now maps to bright white text
val NavySecondary = ImmersiveEmerald             // Selected highlight maps to emerald
val GoldAccent = ImmersiveEmerald                // Accent maps to emerald
val GoldHighlight = ImmersiveEmeraldLight         // Glowing highlight
val CreamBg = ImmersiveBg                         // Background maps to space-dark
val SuccessGreen = ImmersiveEmerald              // Success maps to emerald
val SuccessGreenLight = ImmersiveEmeraldLight
val WarningRed = ImmersiveRed                     // Red alert

// Unused standard M3 mappings
val Purple80 = ImmersiveEmerald
val PurpleGrey80 = ImmersiveCardBg
val Pink80 = ImmersiveEmeraldLight

val Purple40 = ImmersiveBg
val PurpleGrey40 = ImmersiveTextSecondary
val Pink40 = ImmersiveRed
