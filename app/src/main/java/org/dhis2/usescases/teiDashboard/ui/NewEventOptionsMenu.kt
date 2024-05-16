package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.R
import org.dhis2.commons.data.EventCreationType
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle

@Composable
fun NewEventOptions(
    options: List<EventCreationOptions>,
    onOptionSelected: (EventCreationType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        IconButton(
            style = IconButtonStyle.FILLED,
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_accent),
                    contentDescription = "New event",
                    tint = MaterialTheme.colors.onPrimary,
                )
            },
            onClick = { expanded = !expanded },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach {
                DropdownMenuItem(
                    content = { Text(it.name) },
                    onClick = {
                        onOptionSelected.invoke(it.type)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun NewEventOptionsPreview() {
    Surface(
        contentColor = Color.White,
    ) {
        NewEventOptions(
            listOf(
                EventCreationOptions(
                    EventCreationType.SCHEDULE,
                    "Schedule",
                ),
            ),
        ) {}
    }
}

data class EventCreationOptions(val type: EventCreationType, val name: String)
