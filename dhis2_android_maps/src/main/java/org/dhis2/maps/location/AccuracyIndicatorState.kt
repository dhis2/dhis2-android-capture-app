package org.dhis2.maps.location

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dhis2.maps.model.AccuracyRange

@Stable
interface AccuracyIndicatorState {
    val progressPosition: Float
    var timeLeft: Int

    fun updateAccuracy(scope: CoroutineScope, accuracyRange: AccuracyRange)
    fun displayInfo(accuracyRange: AccuracyRange): Boolean
    fun displayMessage(accuracyRange: AccuracyRange): Boolean
}

@Stable
class AccuracyIndicatorStateImpl(private val defaultTimeLeft: Int) : AccuracyIndicatorState {
    override val progressPosition: Float
        get() = _progressX.value

    override var timeLeft by mutableIntStateOf(defaultTimeLeft)

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

            timeLeft = defaultTimeLeft

            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
        }
    }

    override fun displayInfo(accuracyRange: AccuracyRange): Boolean {
        return (accuracyRange !is AccuracyRange.None) and (_progressX.value == 0f)
    }

    override fun displayMessage(accuracyRange: AccuracyRange): Boolean {
        val noLocationNoTimeLeft = (timeLeft == 0) and (accuracyRange is AccuracyRange.None)
        val locationRequiresMessage =
            (accuracyRange is AccuracyRange.Low) or (accuracyRange is AccuracyRange.Medium)
        return noLocationNoTimeLeft or locationRequiresMessage
    }
}

@Composable
fun rememberAccuracyIndicatorState(timeLeft: Int) = remember {
    AccuracyIndicatorStateImpl(timeLeft)
}
