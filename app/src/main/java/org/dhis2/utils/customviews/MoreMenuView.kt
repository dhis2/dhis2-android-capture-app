package org.dhis2.utils.customviews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.menu.DropDownMenu
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

fun <T> setupDropDownMenu(
    composeView: ComposeView,
    dropDownMenuItems: List<MenuItemData<T>>,
    onItemClick: (T) -> Unit,
) {
    composeView.setContent {
        var expanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.padding(
                end = Spacing.Spacing16
            )
        ) {
            IconButton(
                modifier = Modifier.offset(x = Spacing.Spacing16),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        tint = SurfaceColor.SurfaceBright,
                        contentDescription = null,
                    )
                }
            ) {
                expanded = !expanded
            }

            DropDownMenu(
                items = dropDownMenuItems,
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) { itemId ->
                expanded = !expanded
                onItemClick(itemId)
            }
        }
    }
}