package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
internal fun DeleteLocalDatabaseSettingItem(
    isOpened: Boolean,
    onClick: () -> Unit,
    onDeleteLocalDataClick: () -> Unit,
) {
    SettingItem(
        title = stringResource(id = R.string.settingsDeleteLocalData),
        subtitle = stringResource(R.string.settingsDeleteLocalData_descr),
        icon = Icons.Outlined.FolderDelete,
        extraActions = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.delete),
                colorStyle = ColorStyle.ERROR,
                style = ButtonStyle.OUTLINED,
                enabled = true,
                onClick = onDeleteLocalDataClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = SurfaceColor.Error,
                    )
                },
            )
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}
