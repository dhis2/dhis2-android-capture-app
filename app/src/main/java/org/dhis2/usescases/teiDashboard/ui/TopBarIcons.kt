package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import org.dhis2.R
import org.dhis2.tracker.relationships.model.RelationshipTopBarIconState
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

@Composable
fun RelationshipTopBarIcon(
    relationshipTopBarIconState: RelationshipTopBarIconState,
    onButtonClicked: () -> Unit,
) {
    Icon(
        modifier = Modifier.clickable {
            onButtonClicked()
        },
        imageVector = relationshipTopBarIconState.icon,
        contentDescription = stringResource(R.string.relationships),
        tint = Color.White,
    )
}
