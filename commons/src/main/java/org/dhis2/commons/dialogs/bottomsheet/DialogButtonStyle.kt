package org.dhis2.commons.dialogs.bottomsheet

import androidx.annotation.ColorRes
import org.dhis2.commons.R

sealed class DialogButtonStyle(
    open val textResource: Int,
    val colorResource: Int? = null,
    val iconResource: Int? = null,
    @ColorRes val backgroundColor: Int? = null
) {

    data class MainButton(override val textResource: Int) : DialogButtonStyle(
        textResource = textResource,
        colorResource = R.color.white
    )

    data class SecondaryButton(override val textResource: Int) : DialogButtonStyle(
        textResource = textResource,
        colorResource = R.color.colorPrimary
    )

    class CompleteButton : DialogButtonStyle(
        textResource = R.string.complete,
        colorResource = R.color.white,
        iconResource = R.drawable.ic_event_status_complete
    )

    class DiscardButton : DialogButtonStyle(
        textResource = R.string.discard_changes,
        colorResource = R.color.section_warning_color
    )

    class NeutralButton(override val textResource: Int) : DialogButtonStyle(
        textResource = textResource,
        colorResource = R.color.colorPrimary,
        backgroundColor = R.color.white
    )
}
