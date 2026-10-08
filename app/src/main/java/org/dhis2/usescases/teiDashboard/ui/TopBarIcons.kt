package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import org.dhis2.R
import org.dhis2.tracker.relationships.ui.state.RelationshipTopBarIconState
import org.dhis2.utils.getOnToolbarColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton

fun ComposeView?.setButtonContent(
    trackedEntityName: StateFlow<String?>,
    onButtonClicked: () -> Unit,
) {
    this?.setContent {
        val onToolbarColor = getOnToolbarColor()
        val name by trackedEntityName.collectAsStateWithLifecycle()
        Button(
            text = "${stringResource(id = R.string.edit)} ${name.orEmpty().lowercase()}",
            style = ButtonStyle.TEXT_LIGHT,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    tint = onToolbarColor,
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
    val onToolbarColor = getOnToolbarColor()
    IconButton(
        modifier = Modifier,
        icon = {
            Icon(
                imageVector = relationshipTopBarIconState.icon,
                contentDescription = stringResource(R.string.relationships),
                tint = onToolbarColor,
            )
        },
    ) {
        onButtonClicked()
    }
}
