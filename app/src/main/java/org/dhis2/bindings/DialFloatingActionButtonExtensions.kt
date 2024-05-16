package org.dhis2.bindings

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.dhis2.utils.dialFloatingActionButton.DialFloatingActionButtonLayout

fun FloatingActionButton.rotate(rotate: Boolean): Boolean {
    animate()
        .setDuration(200)
        .setListener(object : AnimatorListenerAdapter() {})
        .rotation(if (rotate) 135f else 0f)
        .start()
    return rotate
}

fun View.showDialItem(onAnimationEnd: () -> Unit = {}) {
    visibility = View.VISIBLE
    post {
        pivotX = (this as ViewGroup).getChildAt(0).x + 20.dp
        pivotY = (this as ViewGroup).getChildAt(0).y + 20.dp
        alpha = 0f
        scaleX = 0.5f
        scaleY = 0.5f
        animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(100)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd()
                    super.onAnimationEnd(animation)
                }
            })
            .alpha(1f)
            .start()
    }
}

fun View.hideDialItem(onAnimationEnd: () -> Unit = {}) {
    visibility = View.VISIBLE
    alpha = 1f
    scaleX = 1f
    scaleY = 1f
    pivotX = (this as ViewGroup).getChildAt(0).x + 20.dp
    pivotY = (this as ViewGroup).getChildAt(0).y + 20.dp
    animate()
        .scaleX(0.5f)
        .scaleY(0.5f)
        .setDuration(100)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
                onAnimationEnd()
                super.onAnimationEnd(animation)
            }
        })
        .alpha(0f)
        .start()
}

@BindingAdapter("is_dial_item")
fun View.initDialItem(isDialItem: Boolean) {
    if (isDialItem) {
        visibility = View.GONE
        translationY = height.toFloat()
        alpha = 0f
    }
}

@BindingAdapter("fab_visibility")
fun DialFloatingActionButtonLayout.setFloatingActionButtonVisibility(hideFab: Boolean) {
    setFabVisible(!hideFab)
}

@BindingAdapter("fab_extra_bottom_margin")
fun DialFloatingActionButtonLayout.setExtraBottomMargin(extraBottomMargin: Int) {
    updateFabMargin(extraBottomMargin.dp)
}
