package org.dhis2.usescases.main.program

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.view.animation.OvershootInterpolator
import org.dhis2.Bindings.dp
import org.dhis2.BuildConfig

class ProgramAnimation {

    fun initBackdropCorners(gd: GradientDrawable) {
        if (BuildConfig.FLAVOR != "dhisUITesting") {
            ValueAnimator.ofInt(0, 16.dp)
                .apply {
                    duration = 700
                    startDelay = 700
                    interpolator = OvershootInterpolator()
                    addUpdateListener {
                        val value = (it.animatedValue as Int).toFloat()
                        gd.cornerRadii = floatArrayOf(value, value, value, value, 0f, 0f, 0f, 0f)
                    }
                }.start()
        }
    }

    fun reverseBackdropCorners(gd: GradientDrawable) {
        if (BuildConfig.FLAVOR != "dhisUITesting") {
            ValueAnimator.ofInt(16.dp, 0)
                .apply {
                    duration = 200
                    interpolator = OvershootInterpolator()
                    addUpdateListener {
                        val value = (it.animatedValue as Int).toFloat()
                        gd.cornerRadius = value
                    }
                }.start()
        }
    }
}