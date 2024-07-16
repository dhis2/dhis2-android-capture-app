package org.dhis2.usescases.events

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material.icons.outlined.Visibility
import org.dhis2.R
import org.dhis2.bindings.profilePicturePath
import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.fromCache
import org.dhis2.commons.bindings.tei
import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.commons.date.toOverdueOrScheduledUiText
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.maps.model.RelatedInfo
import org.dhis2.ui.avatar.AvatarProviderConfiguration
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import java.util.Date

class EventInfoProvider(
    private val d2: D2,
    private val resourceManager: ResourceManager,
    private val dateLabelProvider: DateLabelProvider,
    private val metadataIconProvider: MetadataIconProvider,
) {
    private val cachedPrograms = mutableMapOf<String, Program>()
    private val cachedDisplayOrgUnit = mutableMapOf<String, Boolean>()
    private val cachedStages = mutableMapOf<String, ProgramStage>()

    fun getEventTitle(event: Event): String {
        return when (event.status()) {
            EventStatus.SCHEDULE -> {
                dateLabelProvider.scheduleFormat(
                    event.eventDate(),
                    R.string.scheduled_for,
                )
            }

            else -> dateLabelProvider.format(event.eventDate())
        }
    }

    fun getAvatar(event: Event, useMetadataIcon: Boolean = false): AvatarProviderConfiguration {
        val stage = event.programStage()?.let { getStage(it) }
        val enrollment = event.enrollment()?.let { d2.enrollment(it) }
        val tei = enrollment?.trackedEntityInstance()?.let { d2.tei(it) }

        return if (tei != null && !useMetadataIcon) {
            val firstAttributeValue = d2.trackedEntityModule().trackedEntitySearch()
                .uid(tei.uid())
                .blockingGet()
                ?.attributeValues
                ?.firstOrNull()
            AvatarProviderConfiguration.ProfilePic(
                profilePicturePath = tei.profilePicturePath(d2, event.program()),
                firstMainValue = firstAttributeValue?.value?.firstOrNull()?.toString()
                    ?: "",
            )
        } else {
            AvatarProviderConfiguration.Metadata(
                metadataIconData = metadataIconProvider(
                    stage?.style() ?: ObjectStyle.builder().build(),
                ),
            )
        }
    }

    fun getEventDescription(event: Event): String? {
        return event.programStage()?.let { getStage(it)?.displayDescription() }
    }

    fun getEventLastUpdated(event: Event): String {
        return dateLabelProvider.span(event.lastUpdated())
    }

    fun getAdditionInfoList(
        event: Event,
    ): MutableList<AdditionalInfoItem> {
        val program = event.program()?.let { getProgram(it) }

        val displayOrgUnit = program?.uid()?.let { getDisplayOrgUnit(it) } == true

        val list = getEventValues(event.uid(), event.programStage()).filter {
            it.second.isNotEmpty() && it.second != "-"
        }.map {
            AdditionalInfoItem(
                key = "${it.first}:",
                value = it.second,
            )
        }.toMutableList()

        if (displayOrgUnit) {
            checkRegisteredIn(
                list = list,
                orgUnitUid = event.organisationUnit(),
            )
        }

        checkCategoryCombination(
            list = list,
            event = event,
            programCatComboUid = program?.categoryComboUid(),
        )

        checkEventStatus(
            list = list,
            status = event.status(),
            dueDate = event.dueDate(),
        )

        checkSyncStatus(
            list = list,
            state = event.aggregatedSyncState(),
        )

        checkViewOnly(
            list = list,
            event = event,
        )

        return list
    }

    fun getRelatedInfo(event: Event): RelatedInfo? {
        val stage = event.programStage()?.let { getStage(it) }
        val enrollment = event.enrollment()?.let { d2.enrollment(it) }
        return if (stage != null) {
            RelatedInfo(
                event = RelatedInfo.Event(
                    stageUid = stage.uid(),
                    stageDisplayName = stage.displayName() ?: stage.uid(),
                    teiUid = enrollment?.uid(),
                ),
            )
        } else {
            null
        }
    }

    private fun getEventValues(
        eventUid: String,
        eventStageUid: String? = null,
    ): List<Pair<String, String>> {
        val stageUid = eventStageUid
            ?: d2.eventModule().events()
                .uid(eventUid)
                .blockingGet()
                ?.programStage()

        val displayInListDataElements = d2.programModule().programStageDataElements()
            .byProgramStage().eq(stageUid)
            .byDisplayInReports().isTrue
            .blockingGet().map {
                it.dataElement()?.uid()!!
            }

        return if (displayInListDataElements.isNotEmpty()) {
            displayInListDataElements.mapNotNull {
                val valueRepo = d2.trackedEntityModule().trackedEntityDataValues()
                    .value(eventUid, it)
                val de = d2.dataElementModule().dataElements()
                    .uid(it).blockingGet()
                if (isAcceptedValueType(de?.valueType())) {
                    Pair(
                        de?.displayFormName() ?: de?.displayName() ?: "-",
                        if (valueRepo.blockingExists()) {
                            valueRepo.blockingGet().userFriendlyValue(d2) ?: "-"
                        } else {
                            "-"
                        },
                    )
                } else {
                    null
                }
            }
        } else {
            emptyList()
        }
    }

    private fun isAcceptedValueType(valueType: ValueType?): Boolean {
        return when (valueType) {
            ValueType.IMAGE, ValueType.COORDINATE, ValueType.FILE_RESOURCE -> false
            else -> true
        }
    }

    private fun checkRegisteredIn(
        list: MutableList<AdditionalInfoItem>,
        orgUnitUid: String?,
    ) {
        val orgUnit = d2.organisationUnitModule().organisationUnits()
            .uid(orgUnitUid)
            .blockingGet()
        list.add(
            AdditionalInfoItem(
                key = resourceManager.getString(R.string.registered_in),
                value = orgUnit?.displayName() ?: "-",
                isConstantItem = true,
            ),
        )
    }

    private fun checkCategoryCombination(
        list: MutableList<AdditionalInfoItem>,
        event: Event,
        programCatComboUid: String?,
    ) {
        val programCatCombo = d2.categoryModule().categoryCombos()
            .uid(programCatComboUid)
            .blockingGet()
        programCatCombo?.let { categoryCombo ->
            val catOptCombo = d2.categoryModule().categoryOptionCombos()
                .uid(event.attributeOptionCombo())
                .blockingGet()

            catOptCombo?.displayName().takeIf { displayName ->
                !displayName.isNullOrEmpty() && displayName != "default"
            }?.let { displayName ->
                list.add(
                    AdditionalInfoItem(
                        key = "${categoryCombo.displayName()}:",
                        value = displayName,
                        isConstantItem = true,
                    ),
                )
            }
        }
    }

    private fun checkEventStatus(
        list: MutableList<AdditionalInfoItem>,
        status: EventStatus?,
        dueDate: Date?,
    ) {
        val item = when (status) {
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

                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = text,
                            tint = AdditionalInfoItemColor.SUCCESS.color,
                        )
                    },
                    value = text,
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.SUCCESS.color,
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

    private fun checkViewOnly(list: MutableList<AdditionalInfoItem>, event: Event) {
        val editable = d2.eventModule().eventService().blockingIsEditable(event.uid())
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

    private fun getStage(programStageUid: String) = fromCache(cachedStages, programStageUid) {
        d2.programModule().programStages()
            .uid(programStageUid)
            .blockingGet()
    }

    private fun getProgram(programUid: String) = fromCache(cachedPrograms, programUid) {
        d2.programModule().programs()
            .uid(programUid)
            .blockingGet()
    }

    private fun getDisplayOrgUnit(programUid: String) =
        fromCache(cachedDisplayOrgUnit, programUid) {
            d2.organisationUnitModule().organisationUnits()
                .byProgramUids(listOf(programUid))
                .blockingGet().size > 1
        } == true
}
