package dev.krinry.jarvis.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// LIQUID GLASS DESIGN SYSTEM — JARVIS AI AGENT
// ============================================================

// === Backgrounds ===
val DeepSpace        = Color(0xFF050A18)   // Primary screen background
val NavyDark         = Color(0xFF0A1628)   // Card/surface base
val NavyMid          = Color(0xFF0D1F3C)   // Secondary surfaces

// === Glass Layers (alpha overlays) ===
val GlassWhite       = Color(0x1AFFFFFF)   // 10% white — glass card fill
val GlassBorder      = Color(0x33FFFFFF)   // 20% white — glass card border
val GlassHighlight   = Color(0x0DFFFFFF)   // 5%  white — subtle inner glow

// === Accent Colors ===
val CyanGlow         = Color(0xFF00D4FF)   // Primary accent / active states
val PurpleAccent     = Color(0xFF8B5CF6)   // Secondary accent
val BlueAccent       = Color(0xFF3B82F6)   // Tertiary / icons
val PinkGlow         = Color(0xFFEC4899)   // Danger / destructive

// === Gradient — Primary CTA ===
val GradientStart    = Color(0xFF6366F1)   // Indigo
val GradientEnd      = Color(0xFF8B5CF6)   // Purple

// === Text ===
val TextPrimary      = Color(0xFFFFFFFF)
val TextSecondary    = Color(0xB3FFFFFF)   // 70% white
val TextMuted        = Color(0x66FFFFFF)   // 40% white

// === Status ===
val StatusGreen      = Color(0xFF22C55E)
val StatusAmber      = Color(0xFFF59E0B)
val StatusRed        = Color(0xFFEF4444)

// === Legacy aliases (keep so existing code doesn't break) ===
val JarvisPrimary         = GradientStart
val JarvisSecondary       = CyanGlow
val JarvisAccent          = PurpleAccent
val JarvisSuccess         = StatusGreen
val JarvisWarning         = StatusAmber
val JarvisError           = StatusRed
val JarvisOrange          = Color(0xFFF97316)
val DarkBackground        = DeepSpace
val DarkSurface           = NavyDark
val DarkSurfaceVariant    = NavyMid
val DarkCard              = Color(0xFF0F1E38)   // Slightly lighter for card distinction
val DarkOnSurface         = TextPrimary
val DarkOnSurfaceVariant  = TextSecondary

// Legacy M3 defaults (unused but kept for compiler)
val Purple80  = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80    = Color(0xFFEFB8C8)
val Purple40  = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40    = Color(0xFF7D5260)
