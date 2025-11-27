package org.dhis2.utils.customviews

import android.content.res.Resources
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.menu.DropDownMenu
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
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
        modifier = Modifier.widthIn(max = 0.7.dw),
        items = dropDownMenuItems,
        expanded = expanded,
        offset = DpOffset(x = -Spacing.Spacing16, y = Spacing.Spacing0),
        onDismissRequest = { onMenuToggle(false) },
        onItemClick = { itemId ->
            onMenuToggle(false)
            onItemClick(itemId)
        },
    )
}

inline val Double.dw: Dp
    get() =
        Resources.getSystem().displayMetrics.let {
            Dp(value = ((this * it.widthPixels) / it.density).toFloat())
        }
