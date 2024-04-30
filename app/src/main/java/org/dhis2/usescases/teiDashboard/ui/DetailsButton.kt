package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle

fun ComposeView?.setButtonContent(trackedEntityName: String, onButtonClicked: () -> Unit) {
    this?.setContent {
        Button(
            text = "${stringResource(id = R.string.edit)} ${trackedEntityName.lowercase()}",
            style = ButtonStyle.TEXT_LIGHT,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    tint = Color.White,
                    contentDescription = "Edit",
                )
            },
            onClick = onButtonClicked,
        )
    }
}
