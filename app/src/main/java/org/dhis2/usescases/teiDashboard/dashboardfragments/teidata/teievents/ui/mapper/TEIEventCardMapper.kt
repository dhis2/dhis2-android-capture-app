package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.ui.mapper

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.dhis2.R
import org.dhis2.commons.data.EventModel
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.date.toOverdueOrScheduledUiText
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.ui.model.ListCardUiModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyleData
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatarSize
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import java.util.Date

class TEIEventCardMapper(
    val resourceManager: ResourceManager,
    val dateUtils: DateUtils,
) {
    fun map(
        event: EventModel,
        editable: Boolean,
        displayOrgUnit: Boolean,
        onCardClick: () -> Unit,
    ): ListCardUiModel =
        ListCardUiModel(
            avatar =
                if (event.groupedByStage != true) {
                    {
                        ProvideAvatar(eventItem = event)
                    }
                } else {
                    null
                },
            title = getTitle(event),
            description = getDescription(event),
            additionalInfo = getAdditionalInfoList(event, editable, displayOrgUnit, event.orgUnitIsInCaptureScope),
            actionButton = {
                if (event.orgUnitIsInCaptureScope) {
                    ProvideActionButton(
                        event = event,
                        onActionButtonClick = onCardClick,
                    )
                }
            },
            expandLabelText = resourceManager.getString(R.string.show_more),
            shrinkLabelText = resourceManager.getString(R.string.show_less),
            onCardCLick = onCardClick,
        )

    @Composable
    private fun ProvideAvatar(eventItem: EventModel) {
        Avatar(
            style =
                AvatarStyleData.Metadata(
                    imageCardData = eventItem.metadataIconData.imageCardData,
                    avatarSize = MetadataAvatarSize.M(),
                    tintColor = eventItem.metadataIconData.color,
                ),
        )
    }

    private fun getTitle(event: EventModel): String =
        when (event.event?.status()) {
            EventStatus.SCHEDULE -> {
                resourceManager
                    .getString(R.string.scheduled_for)
                    .format(event.displayDate ?: "")
            }

            else -> event.displayDate ?: ""
        }

    private fun getDescription(event: EventModel): String? =
        if (event.groupedByStage == true) {
            null
        } else {
            event.stage?.displayName()
        }

    private fun getAdditionalInfoList(
        event: EventModel,
        editable: Boolean,
        displayOrgUnit: Boolean,
        eventOrgUnitIsInCaptureScope: Boolean,
    ): List<AdditionalInfoItem> {
        val list =
            event.dataElementValues
                ?.filter {
                    !it.second.isNullOrEmpty() && it.second != "-"
                }?.map {
                    AdditionalInfoItem(
                        key = it.first,
                        value = it.second ?: "-",
                    )
                }?.toMutableList() ?: mutableListOf()

        if (displayOrgUnit) {
            checkRegisteredIn(
                list = list,
                orgUnit = event.orgUnitName,
            )
        }

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
            hasAccessToOrgUnit = eventOrgUnitIsInCaptureScope,
        )

        checkHasNoAccessToOrgUnit(
            list = list,
            eventOrgUnit = event.orgUnitName,
            eventOrgUnitIsInCaptureScope,
        )
        return list
    }

    private fun checkHasNoAccessToOrgUnit(
        list: MutableList<AdditionalInfoItem>,
        eventOrgUnit: String,
        orgUnitIsInCaptureScope: Boolean,
    ) {
        if (!orgUnitIsInCaptureScope) {
            list.add(
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Block,
                            contentDescription = resourceManager.getString(R.string.at_enroll_org_unit),
                            tint = AdditionalInfoItemColor.WARNING.color,
                        )
                    },
                    value =
                        resourceManager
                            .getString(R.string.at_enroll_org_unit)
                            .format(eventOrgUnit),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.WARNING.color,
                ),
            )
            list.add(
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Block,
                            contentDescription = "no_access_to_org_unit",
                            tint = Color.Transparent,
                        )
                    },
                    value =
                        resourceManager
                            .getString(R.string.no_access_to_it),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.WARNING.color,
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
        event: EventModel,
    ) {
        if (!event.nameCategoryOptionCombo.isNullOrEmpty() &&
            event.nameCategoryOptionCombo != "default"
        ) {
            list.add(
                AdditionalInfoItem(
                    key = event.nameCategoryOptionCombo,
                    value = event.catComboName ?: "-",
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
        val item =
            when (status) {
                EventStatus.ACTIVE -> {
                    AdditionalInfoItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = resourceManager.getString(R.string.event_not_completed),
                                tint = SurfaceColor.Primary,
                            )
                        },
                        value = resourceManager.getString(R.string.event_not_completed),
                        isConstantItem = true,
                        color = SurfaceColor.Primary,
                    )
                }

                EventStatus.SCHEDULE -> {
                    val text = dueDate.toOverdueOrScheduledUiText(resourceManager)
                    val color =
                        if (dateUtils.isEventDueDateOverdue(
                                dueDate,
                            )
                        ) {
                            AdditionalInfoItemColor.ERROR.color
                        } else {
                            AdditionalInfoItemColor.SUCCESS.color
                        }
                    val icon = if (dateUtils.isEventDueDateOverdue(dueDate)) Icons.Outlined.EventBusy else Icons.Outlined.Event
                    AdditionalInfoItem(
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = text,
                                tint = color,
                            )
                        },
                        value = text,
                        isConstantItem = true,
                        color = color,
                    )
                }

                EventStatus.SKIPPED -> {
                    AdditionalInfoItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.EventBusy,
                                contentDescription = resourceManager.getString(R.string.skipped),
                                tint = AdditionalInfoItemColor.DISABLED.color,
                            )
                        },
                        value = resourceManager.getString(R.string.skipped),
                        isConstantItem = true,
                        color = AdditionalInfoItemColor.DISABLED.color,
                    )
                }

                EventStatus.OVERDUE -> {
                    val overdueText = dueDate.toOverdueOrScheduledUiText(resourceManager)

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

    private fun checkSyncStatus(
        list: MutableList<AdditionalInfoItem>,
        state: State?,
    ) {
        val item =
            when (state) {
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

    private fun checkViewOnly(
        list: MutableList<AdditionalInfoItem>,
        editable: Boolean,
        hasAccessToOrgUnit: Boolean,
    ) {
        if (!editable && hasAccessToOrgUnit) {
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

    @Composable
    private fun ProvideActionButton(
        event: EventModel,
        onActionButtonClick: () -> Unit,
    ) {
        when (event.event?.status()) {
            EventStatus.SCHEDULE -> {
                Button(
                    style = ButtonStyle.TONAL,
                    text =
                        resourceManager.getString(R.string.enter_event_data).format(
                            event.stage?.displayEventLabel() ?: resourceManager.getString(R.string.event),
                        ),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription =
                                resourceManager
                                    .getString(R.string.enter_event_data)
                                    .format(
                                        event.stage?.displayEventLabel() ?: resourceManager.getString(
                                            R.string.event,
                                        ),
                                    ),
                            tint = TextColor.OnPrimaryContainer,
                        )
                    },
                    onClick = { onActionButtonClick() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            EventStatus.OVERDUE -> {
                Button(
                    style = ButtonStyle.TONAL,
                    text =
                        resourceManager.getString(R.string.enter_cancel_event_data).format(
                            event.stage?.displayEventLabel() ?: resourceManager.getString(R.string.event),
                        ),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription =
                                resourceManager
                                    .getString(R.string.enter_event_data)
                                    .format(
                                        event.stage?.displayEventLabel() ?: resourceManager.getString(
                                            R.string.event,
                                        ),
                                    ),
                            tint = TextColor.OnPrimaryContainer,
                        )
                    },
                    onClick = { onActionButtonClick() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            else -> {
                // No action button
            }
        }
    }
}
