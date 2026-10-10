package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Animated Ambient Background with floating modern glass/neon spheres ("шарики")
 * providing subtle atmospheric depth, multi-layered parallax motion, and glowing highlights.
 */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_spheres")

    // Motion phase 1: Horizontal drift
    val phaseX1 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_x1"
    )

    // Motion phase 2: Vertical drift
    val phaseY1 by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_y1"
    )

    // Motion phase 3: Secondary diagonal drift
    val phaseD by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_d"
    )

    // Gentle radius breathing pulsation
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_ratio"
    )

    // Continuous subtle orbit rotation angle
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    // Sphere color palettes: vibrant cyber-neon in dark, refreshing soft glass in light
    val colorCyan = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.085f) else Color(0xFF0284C7).copy(alpha = 0.08f)
    val colorEmerald = if (isDark) Color(0xFF10B981).copy(alpha = 0.09f) else Color(0xFF059669).copy(alpha = 0.08f)
    val colorViolet = if (isDark) Color(0xFF8B5CF6).copy(alpha = 0.08f) else Color(0xFF7C3AED).copy(alpha = 0.075f)
    val colorRose = if (isDark) Color(0xFFF43F5E).copy(alpha = 0.065f) else Color(0xFFE11D48).copy(alpha = 0.065f)
    val colorAmber = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.06f) else Color(0xFFD97706).copy(alpha = 0.055f)

    val rimColor = if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.06f) else Color(0xFF000000).copy(alpha = 0.045f)
    val highlightColor = if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.14f) else Color(0xFFFFFFFF).copy(alpha = 0.35f)

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Helper to draw a modern 3D-styled glass sphere ("шарик")
            fun drawGlassSphere(
                center: Offset,
                radius: Float,
                baseColor: Color,
                showHighlight: Boolean = true
            ) {
                // 1. Outer ambient radial glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(baseColor, baseColor.copy(alpha = baseColor.alpha * 0.4f), Color.Transparent),
                        center = center,
                        radius = radius * 1.35f
                    ),
                    radius = radius * 1.35f,
                    center = center
                )

                // 2. Solid spherical volume with specular inner gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            baseColor.copy(alpha = baseColor.alpha * 1.4f),
                            baseColor,
                            baseColor.copy(alpha = baseColor.alpha * 0.2f)
                        ),
                        center = Offset(center.x - radius * 0.28f, center.y - radius * 0.28f),
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )

                // 3. Delicate rim stroke
                drawCircle(
                    color = rimColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // 4. Glossy specular reflection highlight arc on top-left rim
                if (showHighlight && radius > 30.dp.toPx()) {
                    val arcOffset = Offset(center.x - radius + 3.dp.toPx(), center.y - radius + 3.dp.toPx())
                    val arcSize = Size((radius - 3.dp.toPx()) * 2, (radius - 3.dp.toPx()) * 2)
                    drawArc(
                        color = highlightColor,
                        startAngle = 200f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = arcOffset,
                        size = arcSize,
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 1. Primary Top-Right Floating Sphere (Cyan / Blue)
            val c1 = Offset(w * 0.82f + phaseX1, h * 0.12f + phaseY1)
            drawGlassSphere(
                center = c1,
                radius = 110.dp.toPx() * pulseRatio,
                baseColor = colorCyan
            )

            // 2. Mid-Left Floating Emerald Sphere (Emerald)
            val c2 = Offset(w * 0.12f - phaseX1 * 0.8f, h * 0.42f - phaseY1 * 0.7f)
            drawGlassSphere(
                center = c2,
                radius = 92.dp.toPx() * (2f - pulseRatio),
                baseColor = colorEmerald
            )

            // 3. Mid-Right Floating Violet Sphere (Violet)
            val c3 = Offset(w * 0.88f + phaseD, h * 0.65f - phaseD * 0.9f)
            drawGlassSphere(
                center = c3,
                radius = 98.dp.toPx() * pulseRatio,
                baseColor = colorViolet
            )

            // 4. Bottom-Left Warm Floating Sphere (Rose / Amber)
            val c4 = Offset(w * 0.22f + phaseD * 0.7f, h * 0.86f + phaseY1 * 0.5f)
            drawGlassSphere(
                center = c4,
                radius = 80.dp.toPx() * (2f - pulseRatio),
                baseColor = colorRose
            )

            // 5. Small Nimble Floating Sphere in Upper Center
            val c5 = Offset(w * 0.46f - phaseX1 * 1.2f, h * 0.24f + phaseD * 0.8f)
            drawGlassSphere(
                center = c5,
                radius = 42.dp.toPx() * pulseRatio,
                baseColor = colorAmber
            )

            // 6. Tiny Satellite Sphere near bottom center
            val c6 = Offset(w * 0.68f - phaseD, h * 0.90f - phaseY1 * 0.6f)
            drawGlassSphere(
                center = c6,
                radius = 28.dp.toPx(),
                baseColor = colorCyan,
                showHighlight = false
            )

            // 7. Decorative geometric orbiting rings with tiny dots
            drawCircle(
                color = rimColor,
                radius = 48.dp.toPx(),
                center = Offset(w * 0.78f - phaseX1 * 0.5f, h * 0.34f + phaseY1 * 0.5f),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = rimColor,
                radius = 24.dp.toPx(),
                center = Offset(w * 0.26f + phaseX1 * 0.5f, h * 0.20f - phaseY1 * 0.4f),
                style = Stroke(width = 0.8.dp.toPx())
            )
        }

        content()
    }
}
