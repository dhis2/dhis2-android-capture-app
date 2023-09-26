package org.dhis2.usescases.searchTrackEntity.ui

import android.transition.ChangeBounds
import android.transition.Transition
import android.transition.TransitionManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import org.dhis2.R

object BackdropManager {
    private const val changeBoundDuration = 200L

    private fun changeBounds(backdropLayout: ConstraintLayout, endID: Int, margin: Int) {
        val transition: Transition = ChangeBounds()
        transition.duration = changeBoundDuration
        TransitionManager.beginDelayedTransition(backdropLayout, transition)

        val initSet = ConstraintSet()
        initSet.clone(backdropLayout)

        initSet.connect(R.id.mainComponent, ConstraintSet.TOP, endID, ConstraintSet.BOTTOM, margin)
        initSet.applyTo(backdropLayout)
    }

    fun changeBoundsIf(
        condition: Boolean,
        backdropLayout: ConstraintLayout,
        endID: Int,
        margin: Int,
    ) {
        if (condition) changeBounds(backdropLayout, endID, margin)
    }
}
