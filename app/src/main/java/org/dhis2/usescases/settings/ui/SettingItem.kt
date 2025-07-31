package org.dhis2.usescases.settings.ui

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
internal fun SettingItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: AnnotatedString,
    icon: ImageVector,
    extraActions: @Composable () -> Unit,
    showExtraActions: Boolean,
    onClick: () -> Unit,
) {
    SettingItem(
        modifier = modifier,
        title = title,
        subtitle = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        icon = icon,
        extraActions = extraActions,
        showExtraActions = showExtraActions,
        onClick = onClick,
    )
}

@Composable
internal fun SettingItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    extraActions: @Composable () -> Unit,
    showExtraActions: Boolean,
    onClick: () -> Unit,
) {
    SettingItem(
        modifier = modifier,
        title = title,
        subtitle = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        icon = icon,
        extraActions = extraActions,
        showExtraActions = showExtraActions,
        onClick = onClick,
    )
}

@Composable
private fun SettingItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: @Composable () -> Unit,
    icon: ImageVector,
    extraActions: @Composable () -> Unit,
    showExtraActions: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .wrapContentHeight()
            .background(color = Color.White, shape = Shape.Small)
            .clip(Shape.Small)
            .border(
                width = 1.dp,
                color = if (showExtraActions) {
                    SurfaceColor.ContainerHighest
                } else {
                    Color.Transparent
                },
                shape = Shape.Small,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceColor.PrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SurfaceColor.Primary,
                )
            }

            Column(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                subtitle()
            }
        }

        HorizontalDivider(modifier = Modifier.padding(start = 72.dp))

        AnimatedVisibility(
            showExtraActions,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(
                    easing = {
                        OvershootInterpolator().getInterpolation(it)
                    },
                ),
            ),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 72.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                        end = 16.dp,
                    ),
            ) {
                extraActions()
            }
        }
    }
}
