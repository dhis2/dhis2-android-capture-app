package org.dhis2.usescases.settings.ui

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
internal fun SettingItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    additionalInfoList: List<AdditionalInfoItem> = emptyList(),
    icon: ImageVector,
    extraActions: @Composable () -> Unit,
    showExtraActions: Boolean,
    onClick: () -> Unit,
) {
    val borderModifier =
        if (showExtraActions) {
            Modifier.border(
                width = 2.dp,
                color = SurfaceColor.ContainerHighest,
                shape = RoundedCornerShape(Radius.S),
            )
        } else {
            Modifier
        }
    Column(
        modifier =
            Modifier
                .wrapContentHeight()
                .then(borderModifier)
                .clip(RoundedCornerShape(Radius.S))
                .background(color = Color.White),
    ) {
        ListCard(
            modifier = modifier,
            listCardState =
                rememberListCardState(
                    title =
                        ListCardTitleModel(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SurfaceColor.Primary,
                        ),
                    description = ListCardDescriptionModel(text = subtitle),
                    additionalInfoColumnState =
                        rememberAdditionalInfoColumnState(
                            additionalInfoList = additionalInfoList,
                            syncProgressItem =
                                AdditionalInfoItem(
                                    key = "hhh",
                                    value = "",
                                ),
                            scrollableContent = true,
                        ),
                    loading = false,
                    expandable = false,
                ),
            listAvatar = {
                Box(
                    modifier =
                        Modifier
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
            },
            onCardClick = onClick,
        )

        HorizontalDivider(modifier = Modifier.padding(start = 72.dp))

        AnimatedVisibility(
            showExtraActions,
            enter =
                expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec =
                        tween(
                            easing = {
                                OvershootInterpolator().getInterpolation(it)
                            },
                        ),
                ),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Box(
                modifier =
                    Modifier
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
