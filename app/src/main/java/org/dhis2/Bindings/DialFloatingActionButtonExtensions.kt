package org.dhis2.Bindings

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun FloatingActionButton.rotate(rotate: Boolean): Boolean {
    animate()
        .setDuration(200)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
            }
        })
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
        translationY = height.toFloat()
        scaleX = 0f
        scaleY = 0f
        animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .translationY(0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
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
    translationY = 0f
    scaleX = 1f
    scaleY = 1f
    pivotX = (this as ViewGroup).getChildAt(0).x + 20.dp
    pivotY = (this as ViewGroup).getChildAt(0).y + 20.dp
    animate()
        .scaleX(0f)
        .scaleY(0f)
        .setDuration(200)
        .translationY(height.toFloat())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
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
