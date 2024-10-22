package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.R
import org.dhis2.commons.data.EventCreationType
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.menu.DropDownMenu
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData

@Composable
fun NewEventOptions(
    options: List<MenuItemData<EventCreationType>>,
    addButtonTestTag: String = TEST_ADD_EVENT_BUTTON,
    onOptionSelected: (EventCreationType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        IconButton(
            modifier = Modifier.testTag(addButtonTestTag),
            style = IconButtonStyle.FILLED,
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_accent),
                    contentDescription = "New event",
                    tint = Color.White,
                )
            },
            onClick = { expanded = !expanded },
        )
        DropDownMenu(
            items = options,
            expanded = expanded,
            onDismissRequest = { expanded = false },
            onItemClick = {
                onOptionSelected.invoke(it)
                expanded = false
            },
        )
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
                MenuItemData(
                    EventCreationType.ADDNEW,
                    "Add new",
                ),
            ),
        ) {}
    }
}

const val TEST_ADD_EVENT_BUTTON = "TEST_ADD_EVENT_BUTTON"
