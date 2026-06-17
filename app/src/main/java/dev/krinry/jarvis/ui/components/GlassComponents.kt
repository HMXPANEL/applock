package dev.krinry.jarvis.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.krinry.jarvis.ui.theme.*
import kotlin.math.sin

// ============================================================
// AnimatedBackgroundOrbs — animated cyan + purple blobs
// Place this BEHIND all content on every screen
// ============================================================
@Composable
fun AnimatedBackgroundOrbs(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_phase"
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        val cyanY = size.height * (0.08f + 0.06f * sin(phase.toDouble()).toFloat())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CyanGlow.copy(alpha = 0.28f), Color.Transparent),
                center = Offset(size.width * 0.15f, cyanY),
                radius = size.width * 0.65f
            )
        )
        val purpleY = size.height * (0.72f - 0.05f * sin((phase + 1.5).toDouble()).toFloat())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PurpleAccent.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(size.width * 0.85f, purpleY),
                radius = size.width * 0.55f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(BlueAccent.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.45f),
                radius = size.width * 0.4f
            )
        )
    }
}

// ============================================================
// GlassCard — frosted glass container with haze blur
// ============================================================
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    cornerRadius: Dp = 20.dp,
    borderAlpha: Float = 0.20f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(GlassWhite, RoundedCornerShape(cornerRadius))
            .hazeChild(
                state = hazeState,
                style = HazeMaterials.ultraThin()
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = borderAlpha),
                        Color.White.copy(alpha = borderAlpha * 0.4f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

// ============================================================
// GradientButton — indigo→purple CTA button
// ============================================================
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = TextMuted
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    ) else Brush.horizontalGradient(
                        colors = listOf(NavyMid, NavyMid)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        }
    }
}
