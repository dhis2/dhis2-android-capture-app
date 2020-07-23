package org.dhis2.usescases.programEventDetail

import android.view.animation.DecelerateInterpolator
import org.dhis2.uicomponents.map.views.CarouselView

class ProgramEventDetailAnimations {

    fun initMapLoading(view: CarouselView) {
        view.animate().apply {
            duration = 500
            interpolator = DecelerateInterpolator()
            alpha(0.25f)
            start()
        }
    }

    fun endMapLoading(view: CarouselView) {
        view.animate().apply {
            duration = 500
            interpolator = DecelerateInterpolator()
            alpha(1f)
            start()
        }
    }
}