package org.dhis2.utils.customviews.navigationbar

import android.view.ViewPropertyAnimator
import android.view.animation.AnticipateOvershootInterpolator

class NavigationBottomBarAnimations(val view: NavigationBottomBar) {

    private val hideAnimationDuration = 200L
    private val selectionInDuration = 200L
    private val selectionOutDuration = 200L

    fun hide(animationEndCallback: () -> Unit) {
        view.apply {
            animateHideTranslation(animate(), height, animationEndCallback)
        }
    }

    fun show(animationEndCallback: () -> Unit) {
        view.apply {
            animateShowTranslation(animate(), animationEndCallback)
        }
    }

    private fun animateHideTranslation(
        animate: ViewPropertyAnimator,
        barHeight: Int,
        endAction: () -> Unit,
    ) {
        animate.translationY(barHeight.toFloat())
            .withEndAction { endAction() }
            .apply {
                duration = hideAnimationDuration
            }.start()
    }

    private fun animateShowTranslation(
        animate: ViewPropertyAnimator,
        animationEndCallback: () -> Unit,
    ) {
        animate.translationY(0f)
            .withEndAction(animationEndCallback)
            .apply {
                duration = hideAnimationDuration
            }.start()
    }

    fun animateSelectionIn(animate: ViewPropertyAnimator) {
        animate.scaleY(1f)
            .scaleX(1f)
            .alpha(1f)
            .apply {
                duration = selectionInDuration
            }.start()
    }

    fun animateSelectionOut(animate: ViewPropertyAnimator, endAction: () -> Unit) {
        animate.scaleY(0f)
            .scaleX(0f)
            .alpha(0f)
            .withEndAction { endAction() }
            .apply {
                interpolator = AnticipateOvershootInterpolator()
                duration = selectionOutDuration
            }.start()
    }
}
