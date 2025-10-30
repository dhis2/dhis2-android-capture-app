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
    val verticalOffset: Int
    var timeLeft: Int

    fun updateAccuracy(
        scope: CoroutineScope,
        accuracyRange: AccuracyRange,
    )

    fun displayInfo(accuracyRange: AccuracyRange): Boolean

    fun displayMessage(accuracyRange: AccuracyRange): Boolean

    fun accuracyProgress(): Float

    fun updateVerticalOffset(
        scope: CoroutineScope,
        verticalOffset: Int,
    )

    fun shouldDisplayProgress(accuracyRange: AccuracyRange): Boolean
}

@Stable
class AccuracyIndicatorStateImpl(
    private val defaultTimeLeft: Int,
) : AccuracyIndicatorState {
    override val progressPosition: Float
        get() = progressX.value

    override val verticalOffset: Int
        get() = _verticalOffset.value.toInt()

    override var timeLeft by mutableIntStateOf(defaultTimeLeft)

    private var progressX = Animatable(0f)

    private var _verticalOffset = Animatable(0f)

    private var accuracyProgress = Animatable(0f)

    private val animationSpec =
        tween<Float>(
            durationMillis = 300,
            easing = FastOutSlowInEasing,
        )

    override fun accuracyProgress(): Float = accuracyProgress.value

    override fun updateVerticalOffset(
        scope: CoroutineScope,
        verticalOffset: Int,
    ) {
        scope.launch {
            _verticalOffset.animateTo(
                targetValue = verticalOffset.toFloat(),
            )
        }
    }

    override fun updateAccuracy(
        scope: CoroutineScope,
        accuracyRange: AccuracyRange,
    ) {
        scope.launch {
            progressX.animateTo(
                targetValue =
                    if (accuracyRange is AccuracyRange.None) {
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

                accuracyProgress.animateTo(
                    targetValue = 1f - timeLeft / defaultTimeLeft.toFloat(),
                )
            }
        }
    }

    override fun displayInfo(accuracyRange: AccuracyRange): Boolean = (accuracyRange !is AccuracyRange.None) and (progressX.value == 0f)

    override fun displayMessage(accuracyRange: AccuracyRange): Boolean {
        val noLocationNoTimeLeft = (timeLeft == 0) and (accuracyRange is AccuracyRange.None)
        val locationRequiresMessage =
            (accuracyRange is AccuracyRange.Low) or (accuracyRange is AccuracyRange.Medium) or
                (accuracyRange is AccuracyRange.Good)
        return noLocationNoTimeLeft or locationRequiresMessage
    }

    override fun shouldDisplayProgress(accuracyRange: AccuracyRange): Boolean = timeLeft > 0 && (accuracyRange !is AccuracyRange.VeryGood)
}

@Composable
fun rememberAccuracyIndicatorState(timeLeft: Int) =
    remember {
        AccuracyIndicatorStateImpl(timeLeft)
    }
