package org.dhis2.ui

import org.dhis2.R


sealed class DialogButtonStyle(
    open val textResource: Int,
    val colorResource: Int? = null,
    val iconResource: Int? = null
) {

    data class MainButton(override val textResource: Int) : DialogButtonStyle(
        textResource = textResource,
        colorResource = R.color.white
    )

    data class SecondaryButton(override val textResource: Int) : DialogButtonStyle(
        textResource = textResource,
        colorResource = R.color.colorPrimary
    )

    object CompleteButton : DialogButtonStyle(
        textResource = R.string.complete,
        colorResource = R.color.white,
        iconResource = R.drawable.ic_event_status_complete
    )

    object DiscardButton : DialogButtonStyle(
        textResource = R.string.discard_changes,
        colorResource = R.color.section_warning_color
    )
}
