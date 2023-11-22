package org.dhis2.usescases.sync

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.usescases.general.ActivityGlobalAbstract
import javax.inject.Inject

class SyncAnimations @Inject constructor(
    private val colorUtils: ColorUtils,
) {

    private val themeAnimationDuration = 2000L

    fun startLottieAnimation(view: LottieAnimationView) {
        view.apply {
            repeatCount = LottieDrawable.INFINITE
            repeatMode = LottieDrawable.RESTART
            enableMergePathsForKitKatAndAbove(true)
            playAnimation()
        }
    }

    fun startThemeAnimation(
        activity: ActivityGlobalAbstract,
        initCallback: () -> Unit,
        updateCallback: (Int) -> Unit,
    ) {
        val startColor = colorUtils.getPrimaryColor(
            activity.context,
            ColorType.PRIMARY,
        )
        initCallback()
        val endColor = colorUtils.getPrimaryColor(
            activity.context,
            ColorType.PRIMARY,
        )

        ValueAnimator.ofObject(
            ArgbEvaluator(),
            startColor,
            endColor,
        ).apply {
            duration = themeAnimationDuration
            addUpdateListener { animator: ValueAnimator ->
                updateCallback(animator.animatedValue as Int)
            }
            start()
        }
    }

    fun startFlagAnimation(updateCallback: (Float) -> Unit) {
        val alphaAnimator =
            ValueAnimator.ofFloat(0f, 1f)
        alphaAnimator.duration = 2000
        alphaAnimator.addUpdateListener { animation: ValueAnimator ->
            updateCallback(animation.animatedValue as Float)
        }
        alphaAnimator.start()
    }
}
