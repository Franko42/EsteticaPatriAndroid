package com.esteticapatri.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Official Brand Identity Palette - Estética Patri
val PastelRose = Color(0xFFFF9CAC) // Rosa principal de marca
val PastelRoseLightContainer = Color(0xFFFFD6DC) // Rosa claro
val PastelRoseOnContainer = Color(0xFF111111) // Negro profundo

val PastelLavender = Color(0xFFE89CB0) // Rosa suave para acentos
val PastelLavenderContainer = Color(0xFFFFF0F3)
val PastelLavenderOnContainer = Color(0xFF111111)

val PastelPeach = Color(0xFFF47791)
val PastelPeachContainer = Color(0xFFFFE5EA)
val PastelPeachOnContainer = Color(0xFF111111)

val PastelBackground = Color(0xFFF7F4F2) // Marfil cálido
val PastelSurface = Color(0xFFFFFFFF)
val PastelSurfaceVariant = Color(0xFFFFF5F7)
val PastelOutline = Color(0xFFEED5DC)
val PastelOnBackground = Color(0xFF111111)
val PastelOnSurface = Color(0xFF111111)

// Dark Palette with Velvet Rose tones
val VelvetRose = Color(0xFFFFB1C8)
val VelvetRoseContainer = Color(0xFF6F1B3B)
val VelvetRoseOnContainer = Color(0xFFFFD9E2)

val VelvetLavender = Color(0xFFD6BCE0)
val VelvetLavenderContainer = Color(0xFF563A60)

val VelvetBackground = Color(0xFF1C1115)
val VelvetSurface = Color(0xFF24161C)
val VelvetSurfaceVariant = Color(0xFF38232B)
val VelvetOutline = Color(0xFF624650)
val VelvetOnBackground = Color(0xFFFCEEED)
val VelvetOnSurface = Color(0xFFFCEEED)

// High-contrast semantic status badges
val StatusScheduledPastel = Color(0xFF2563EB)
val StatusScheduledContainer = Color(0xFFDBEAFE)

val StatusCompletedPastel = Color(0xFF059669)
val StatusCompletedContainer = Color(0xFFD1FAE5)

val StatusCancelledPastel = Color(0xFFDC2626)
val StatusCancelledContainer = Color(0xFFFEE2E2)

val StatusNoShowPastel = Color(0xFFD97706)
val StatusNoShowContainer = Color(0xFFFEF3C7)

// Decorative Soft Gradients for Cards and Headers
val RoseBlushGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFFEEF3), Color(0xFFFFF8FA))
)

val LavenderBlushGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF7ECFF), Color(0xFFFFEEF5))
)

val GoldenRoseGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFF472B6), Color(0xFFFBBF24))
)

val DarkVelvetGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF2D1722), Color(0xFF1E1017))
)
