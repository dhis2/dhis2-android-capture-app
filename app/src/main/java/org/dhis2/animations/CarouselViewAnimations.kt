package org.dhis2.animations

import android.view.animation.DecelerateInterpolator
import org.dhis2.android_maps.views.CarouselView

class CarouselViewAnimations {

    fun initMapLoading(view: org.dhis2.android_maps.views.CarouselView) {
        view.animate().apply {
            duration = 500
            interpolator = DecelerateInterpolator()
            alpha(0.25f)
            withStartAction { view.setEnabledStatus(false) }
            start()
        }
    }

    fun endMapLoading(view: org.dhis2.android_maps.views.CarouselView) {
        view.animate().apply {
            duration = 500
            interpolator = DecelerateInterpolator()
            alpha(1f)
            withEndAction {
                view.setEnabledStatus(true)
                view.selectFirstItem()
            }
            start()
        }
    }
}
