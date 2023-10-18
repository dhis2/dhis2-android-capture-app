package org.dhis2.usescases.programEventDetail.eventList.ui.mapper

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.R
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.date.toUiText
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.ui.model.ListCardUiModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import java.util.Date

class EventCardMapper(
    val context: Context,
    val resourceManager: ResourceManager,
) {
    fun map(
        event: EventViewModel,
        editable: Boolean,
        onSyncIconClick: () -> Unit,
        onCardClick: () -> Unit,
    ): ListCardUiModel {
        return ListCardUiModel(
            title = event.displayDate ?: "",
            lastUpdated = event.lastUpdate.toDateSpan(context),
            additionalInfo = getAdditionalInfoList(event, editable),
            actionButton = {
                ProvideSyncButton(
                    state = event.event?.aggregatedSyncState(),
                    onSyncIconClick = onSyncIconClick,
                )
            },
            expandLabelText = resourceManager.getString(R.string.show_more),
            shrinkLabelText = resourceManager.getString(R.string.show_less),
            onCardCLick = onCardClick,
        )
    }

    private fun getAdditionalInfoList(
        event: EventViewModel,
        editable: Boolean,
    ): List<AdditionalInfoItem> {
        val list = event.dataElementValues?.filter {
            !it.second.isNullOrEmpty()
        }?.map {
            AdditionalInfoItem(
                key = "${it.first}:",
                value = it.second ?: "",
            )
        }?.toMutableList() ?: mutableListOf()

        checkRegisteredIn(
            list = list,
            orgUnit = event.orgUnitName,
        )

        checkCategoryCombination(
            list = list,
            event = event,
        )

        checkEventStatus(
            list = list,
            status = event.event?.status(),
            dueDate = event.event?.dueDate(),
        )

        checkSyncStatus(
            list = list,
            state = event.event?.aggregatedSyncState(),
        )

        checkViewOnly(
            list = list,
            editable = editable,
        )

        return list
    }

    private fun checkViewOnly(list: MutableList<AdditionalInfoItem>, editable: Boolean) {
        if (!editable) {
            list.add(
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = resourceManager.getString(R.string.view_only),
                            tint = AdditionalInfoItemColor.DISABLED.color,
                        )
                    },
                    value = resourceManager.getString(R.string.view_only),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.DISABLED.color,
                ),
            )
        }
    }

    private fun checkRegisteredIn(
        list: MutableList<AdditionalInfoItem>,
        orgUnit: String,
    ) {
        list.add(
            AdditionalInfoItem(
                key = resourceManager.getString(R.string.registered_in),
                value = orgUnit,
                isConstantItem = true,
            ),
        )
    }

    private fun checkCategoryCombination(
        list: MutableList<AdditionalInfoItem>,
        event: EventViewModel,
    ) {
        if (!event.nameCategoryOptionCombo.isNullOrEmpty() &&
            event.nameCategoryOptionCombo != "default"
        ) {
            list.add(
                AdditionalInfoItem(
                    key = "${event.nameCategoryOptionCombo}:",
                    value = event.catComboName ?: "",
                    isConstantItem = true,
                ),
            )
        }
    }

    private fun checkEventStatus(
        list: MutableList<AdditionalInfoItem>,
        status: EventStatus?,
        dueDate: Date?,
    ) {
        val item = when (status) {
            EventStatus.COMPLETED -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = resourceManager.getString(R.string.event_completed),
                            tint = AdditionalInfoItemColor.SUCCESS.color,
                        )
                    },
                    value = resourceManager.getString(R.string.event_completed),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.SUCCESS.color,
                )
            }

            EventStatus.SCHEDULE -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = resourceManager.getString(
                                R.string.scheduled,
                                dueDate?.toUiText(context) ?: "",
                            ),
                            tint = AdditionalInfoItemColor.DEFAULT_VALUE.color,
                        )
                    },
                    value = resourceManager.getString(R.string.scheduled),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.DEFAULT_VALUE.color,
                )
            }

            EventStatus.SKIPPED -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.EventBusy,
                            contentDescription = resourceManager.getString(R.string.skipped),
                            tint = AdditionalInfoItemColor.DEFAULT_KEY.color,
                        )
                    },
                    value = resourceManager.getString(R.string.skipped),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.DEFAULT_KEY.color,
                )
            }

            EventStatus.OVERDUE -> {
                val overdueText = resourceManager.getString(
                    R.string.overdue,
                    dueDate?.toUiText(context) ?: "",
                )

                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.EventBusy,
                            contentDescription = overdueText,
                            tint = AdditionalInfoItemColor.ERROR.color,
                        )
                    },
                    value = overdueText,
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.ERROR.color,
                )
            }

            else -> null
        }
        item?.let { list.add(it) }
    }

    @Composable
    private fun ProvideSyncButton(state: State?, onSyncIconClick: () -> Unit) {
        val buttonText = when (state) {
            State.TO_POST,
            State.TO_UPDATE,
            -> {
                resourceManager.getString(R.string.sync)
            }

            State.ERROR,
            State.WARNING,
            -> {
                resourceManager.getString(R.string.sync_retry)
            }

            else -> null
        }
        buttonText?.let {
            Button(
                style = ButtonStyle.TONAL,
                text = it,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = it,
                        tint = TextColor.OnPrimaryContainer,
                    )
                },
                onClick = { onSyncIconClick() },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    private fun checkSyncStatus(
        list: MutableList<AdditionalInfoItem>,
        state: State?,
    ) {
        val item = when (state) {
            State.TO_POST,
            State.TO_UPDATE,
            -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.SyncDisabled,
                            contentDescription = resourceManager.getString(R.string.not_synced),
                            tint = AdditionalInfoItemColor.DISABLED.color,
                        )
                    },
                    value = resourceManager.getString(R.string.not_synced),
                    color = AdditionalInfoItemColor.DISABLED.color,
                    isConstantItem = true,
                )
            }

            State.UPLOADING -> {
                AdditionalInfoItem(
                    icon = {
                        ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)
                    },
                    value = resourceManager.getString(R.string.syncing),
                    color = SurfaceColor.Primary,
                    isConstantItem = true,
                )
            }

            State.ERROR -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.SyncProblem,
                            contentDescription = resourceManager.getString(R.string.sync_error_title),
                            tint = AdditionalInfoItemColor.ERROR.color,
                        )
                    },
                    value = resourceManager.getString(R.string.sync_error_title),
                    color = AdditionalInfoItemColor.ERROR.color,
                    isConstantItem = true,
                )
            }

            State.WARNING -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.SyncProblem,
                            contentDescription = resourceManager.getString(R.string.sync_dialog_title_warning),
                            tint = AdditionalInfoItemColor.WARNING.color,
                        )
                    },
                    value = resourceManager.getString(R.string.sync_dialog_title_warning),
                    color = AdditionalInfoItemColor.WARNING.color,
                    isConstantItem = true,
                )
            }

            else -> null
        }
        item?.let { list.add(it) }
    }
}
