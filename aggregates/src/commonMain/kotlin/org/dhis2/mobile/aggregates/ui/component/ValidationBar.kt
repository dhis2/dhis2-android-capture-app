package org.dhis2.mobile.aggregates.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.dhis2.mobile.aggregates.ui.states.ValidationBarUiState
import org.hisp.dhis.mobile.ui.designsystem.component.Badge
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
internal fun ValidationBar(
    uiState: ValidationBarUiState,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
        modifier = Modifier
            .fillMaxWidth()
            .background(color = SurfaceColor.ErrorContainer)
            .padding(
                start = Spacing.Spacing16,
                end = Spacing.Spacing4,
            ),
    ) {
        Badge(
            modifier = Modifier
                .padding(
                    start = Spacing.Spacing4,
                    end = Spacing.Spacing4,
                ),
            text = uiState.quantity.toString(),
            color = SurfaceColor.Error,
            textColor = TextColor.OnPrimary,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = uiState.description,
            style = MaterialTheme.typography.bodyMedium,
        )
        IconButton(
            style = IconButtonStyle.STANDARD,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.ExpandLess,
                    contentDescription = "Expand",
                    tint = TextColor.OnSurfaceVariant,
                )
            },
            onClick = uiState.onExpandErrors,
        )
    }
}
