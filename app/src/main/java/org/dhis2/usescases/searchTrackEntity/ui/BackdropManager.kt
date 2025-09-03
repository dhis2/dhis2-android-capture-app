package org.dhis2.usescases.searchTrackEntity.ui

import android.transition.ChangeBounds
import android.transition.Transition
import android.transition.TransitionManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import org.dhis2.R

object BackdropManager {
    private const val CHANGE_BOUND_DURATION = 200L

    private fun changeBounds(
        isNavigationBarVisible: Boolean,
        backdropLayout: ConstraintLayout,
        endID: Int,
        margin: Int,
    ) {
        val transition: Transition = ChangeBounds()
        transition.duration = CHANGE_BOUND_DURATION
        TransitionManager.beginDelayedTransition(backdropLayout, transition)

        val initSet = ConstraintSet()
        initSet.clone(backdropLayout)

        initSet.connect(R.id.mainComponent, ConstraintSet.TOP, endID, ConstraintSet.BOTTOM, margin)
        if (isNavigationBarVisible) {
            initSet.connect(
                R.id.mainComponent,
                ConstraintSet.BOTTOM,
                R.id.navigationBar,
                ConstraintSet.TOP,
                0,
            )
        } else {
            initSet.connect(
                R.id.mainComponent,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                0,
            )
        }
        initSet.applyTo(backdropLayout)
    }

    fun changeBoundsIf(
        condition: Boolean,
        isNavigationBarVisible: Boolean,
        backdropLayout: ConstraintLayout,
        endID: Int,
        margin: Int,
    ) {
        if (condition) changeBounds(isNavigationBarVisible, backdropLayout, endID, margin)
    }
}
