package org.dhis2.utils.customviews

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.menu.DropDownMenu
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
fun <T> MoreOptionsWithDropDownMenuButton(
    dropDownMenuItems: List<MenuItemData<T>>,
    expanded: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    onItemClick: (T) -> Unit,
) {
    IconButton(
        modifier = Modifier,
        icon = {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                tint = SurfaceColor.SurfaceBright,
                contentDescription = null,
            )
        },
    ) {
        onMenuToggle(!expanded)
    }

    DropDownMenu(
        items = dropDownMenuItems,
        expanded = expanded,
        onDismissRequest = { onMenuToggle(false) },
    ) { itemId ->
        onMenuToggle(false)
        onItemClick(itemId)
    }
}
