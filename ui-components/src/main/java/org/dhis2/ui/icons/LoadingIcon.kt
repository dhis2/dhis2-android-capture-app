package org.dhis2.ui.icons

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.ui.theme.colorPrimary

@Preview
@Composable
fun SyncingIcon() {
    Row(
        modifier = Modifier.size(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        val radius = with(LocalDensity.current) {
            2.dp.toPx()
        }

        val transitionDataA = syncAnimation(radius, 0)
        val transitionDataB = syncAnimation(radius, 250)
        val transitionDataC = syncAnimation(radius, 500)

        Box(
            modifier = Modifier
                .size(4.dp),
        ) {
            Canvas(modifier = Modifier) {
                drawCircle(
                    color = transitionDataA.color,
                    radius = radius,
                    center = Offset(radius, transitionDataA.positionY),
                )
            }
        }

        Box(
            modifier = Modifier
                .size(4.dp),
        ) {
            Canvas(modifier = Modifier) {
                drawCircle(
                    color = transitionDataB.color,
                    radius = radius,
                    center = Offset(radius, transitionDataB.positionY),
                )
            }
        }

        Box(
            modifier = Modifier
                .size(4.dp),
        ) {
            Canvas(modifier = Modifier) {
                drawCircle(
                    color = transitionDataC.color,
                    radius = radius,
                    center = Offset(radius, transitionDataC.positionY),
                )
            }
        }
    }
}

@Composable
fun syncAnimation(radius: Float, delayMillis: Int): TransitionData {
    val startColor = colorPrimary
    val endColor = startColor.copy(alpha = 0.3f)

    val infiniteTransition = rememberInfiniteTransition()
    val color by infiniteTransition.animateColor(
        initialValue = startColor,
        targetValue = endColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 250, delayMillis = 250),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delayMillis),
        ),
    )

    val positionY by infiniteTransition.animateFloat(
        initialValue = radius,
        targetValue = -2 * radius,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 250, delayMillis = 250),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delayMillis),
        ),
    )
    return TransitionData(color, positionY)
}

data class TransitionData(val color: Color, val positionY: Float)
