package org.dhis2.utils.customviews

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import androidx.core.animation.addListener
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import org.dhis2.Bindings.dp


class DhisBottomNavigationBarAnimations(val view: DhisBottomNavigationBar) {

    private val collapsedWidth = 52.dp
    private val collapseAnimationDuration = 1200L
    private val collapseAnimationDelay = 700L
    private val expandAnimationDuration = 1200L
    private val expandAnimationDelay = 700L

    private val hideTranslation by lazy { (view.width - collapsedWidth) }


    fun collapse(animationEndCallback: () -> Unit) {
        view.apply {
            showExpandButton()
            animateToCollapsedCorners((background as MaterialShapeDrawable), animationEndCallback)
            animateCollapsedTranslation(animate())

        }
    }

    fun expand(animationEndCallback: () -> Unit) {
        view.apply {
            hideExpandButton()
            animateToExpandedCorners((background as MaterialShapeDrawable), animationEndCallback)
            animateExpandedTranslation(animate())

        }
    }

    private fun animateCollapsedTranslation(animate: ViewPropertyAnimator) {
        animate.translationX(hideTranslation.toFloat())
            .apply {
                duration = collapseAnimationDuration
            }.start()
    }

    private fun animateExpandedTranslation(animate: ViewPropertyAnimator) {
        animate.translationX(0f)
            .apply {
                duration = expandAnimationDuration
            }.start()
    }

    private fun animateToCollapsedCorners(
        shapeDrawable: MaterialShapeDrawable,
        animationEndCallback: () -> Unit
    ) {
        ValueAnimator.ofInt(0, 16.dp)
            .apply {
                duration = collapseAnimationDuration
                startDelay = collapseAnimationDelay
                addUpdateListener {
                    val value = (it.animatedValue as Int).toFloat()

                    shapeDrawable.shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                        .setBottomLeftCornerSize(value)
                        .setTopLeftCornerSize(value)
                        .setTopRightCornerSize(value)
                        .build()
                }
                addListener(onEnd = {
                    animationEndCallback()
                })
            }.start()
    }

    private fun animateToExpandedCorners(
        shapeDrawable: MaterialShapeDrawable,
        animationEndCallback: () -> Unit
    ) {
        ValueAnimator.ofInt(16.dp, 0)
            .apply {
                duration = expandAnimationDuration
                startDelay = expandAnimationDelay
                addUpdateListener {
                    val value = (it.animatedValue as Int).toFloat()
                    shapeDrawable.shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                        .setBottomLeftCornerSize(value)
                        .setTopLeftCornerSize(value)
                        .setTopRightCornerSize(value)
                        .build()
                }
                addListener(onEnd = {
                    animationEndCallback()
                })
            }.start()
    }

    private fun showExpandButton() {
        expandButton().animate()
            .scaleY(1f)
            .scaleX(1f)
            .setDuration(collapseAnimationDuration)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                    expandButton().visibility = View.VISIBLE
                }
            })
            .start()
    }

    private fun hideExpandButton() {
        expandButton().animate()
            .scaleY(0f)
            .scaleX(0f)
            .setDuration(expandAnimationDuration)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    expandButton().visibility = View.GONE
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }
            })
            .start()
    }

    private fun expandButton(): ImageView {
        return view.findViewWithTag(EXPAND_BUTTON_TAG)
    }
}