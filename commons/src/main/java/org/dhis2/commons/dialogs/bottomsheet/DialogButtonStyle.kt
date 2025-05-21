package org.dhis2.commons.dialogs.bottomsheet

import androidx.compose.ui.graphics.Color
import org.dhis2.ui.R
import org.dhis2.ui.theme.colorPrimary
import org.dhis2.ui.theme.warningColor
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle

sealed class DialogButtonStyle(
    open val textLabel: String? = null,
    open val textResource: Int = -1,
    open val buttonStyle: ButtonStyle = ButtonStyle.TEXT,
    val colorResource: Color? = null,
    val iconResource: Int? = null,
    val backgroundColor: Color? = null,
) {

    data class MainButtonLabel(override val textLabel: String) : DialogButtonStyle(
        textLabel = textLabel,
        colorResource = Color.White,
    )
    data class MainButton(override val textResource: Int) : DialogButtonStyle(
        textResource = textResource,
        colorResource = Color.White,
    )

    data class SecondaryButtonLabel(override val textLabel: String) : DialogButtonStyle(
        textLabel = textLabel,
        colorResource = colorPrimary,
    )
    data class SecondaryButton(override val textResource: Int, override val buttonStyle: ButtonStyle = ButtonStyle.TEXT) : DialogButtonStyle(
        textResource = textResource,
        colorResource = colorPrimary,
        buttonStyle = ButtonStyle.TEXT,
    )

    data object CompleteButton : DialogButtonStyle(
        textResource = R.string.complete,
        colorResource = Color.White,
        iconResource = R.drawable.ic_event_status_complete,
    )

    class DiscardButton : DialogButtonStyle(
        textResource = R.string.discard_changes,
        colorResource = warningColor,
    )

    class NeutralButton(override val textResource: Int) : DialogButtonStyle(
        textResource = textResource,
        colorResource = colorPrimary,
        backgroundColor = Color.White,
    )
}
