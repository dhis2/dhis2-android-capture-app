package org.dhis2.maps.location

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.maps.model.AccuracyRange

@Stable
interface AccuracyIndicatorState {
    val progressPosition: Float

    fun updateAccuracy(scope: CoroutineScope, accuracyRange: AccuracyRange)
    fun displayInfo(accuracyRange: AccuracyRange): Boolean
}

@Stable
class AccuracyIndicatorStateImpl() : AccuracyIndicatorState {
    override val progressPosition: Float
        get() = _progressX.value

    private var _progressX = Animatable(0f)

    private val animationSpec = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing,
    )

    override fun updateAccuracy(scope: CoroutineScope, accuracyRange: AccuracyRange) {
        scope.launch {
            _progressX.animateTo(
                targetValue = if (accuracyRange is AccuracyRange.None) {
                    1f
                } else {
                    0f
                },
                animationSpec = animationSpec,
            )
        }
    }

    override fun displayInfo(accuracyRange: AccuracyRange): Boolean {
        return (accuracyRange !is AccuracyRange.None) and (_progressX.value == 0f)
    }
}

@Composable
fun rememberAccuracyIndicatorState() = remember {
    AccuracyIndicatorStateImpl()
}
