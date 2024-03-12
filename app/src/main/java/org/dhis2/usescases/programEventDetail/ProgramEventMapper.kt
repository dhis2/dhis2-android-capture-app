package org.dhis2.usescases.programEventDetail

import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.data.ProgramEventViewModel
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import java.util.Date
import java.util.Locale

class ProgramEventMapper(
    val d2: D2,
    val periodUtils: DhisPeriodUtils,
    val metadataIconProvider: MetadataIconProvider,
) {

    fun eventToEventViewModel(event: Event): EventViewModel {
        val programStage =
            d2.programModule().programStages().uid(event.programStage()).blockingGet()

        val eventDate = event.eventDate() ?: event.dueDate()

        return EventViewModel(
            EventViewModelType.EVENT,
            programStage,
            event,
            0,
            event.lastUpdated(),
            isSelected = false,
            canAddNewEvent = true,
            orgUnitName = d2.organisationUnitModule().organisationUnits()
                .uid(event.organisationUnit())
                .blockingGet()?.displayName() ?: "-",
            catComboName = getCatOptionComboName(event.attributeOptionCombo()),
            dataElementValues = getEventValues(event.uid(), event.programStage()!!),
            groupedByStage = true,
            displayDate = eventDate?.let {
                periodUtils.getPeriodUIString(
                    programStage?.periodType() ?: PeriodType.Daily,
                    it,
                    Locale.getDefault(),
                )
            },
            nameCategoryOptionCombo =
            getCategoryComboFromOptionCombo(event.attributeOptionCombo())?.displayName(),
            metadataIconData = metadataIconProvider(
                programStage?.style() ?: ObjectStyle.builder().build(),
            ),
        )
    }

    private fun getCategoryComboFromOptionCombo(categoryOptionComboUid: String?): CategoryCombo? {
        val catOptionComboUid = categoryOptionComboUid?.let {
            d2.categoryModule()
                .categoryOptionCombos()
                .uid(it)
                .blockingGet()?.categoryCombo()?.uid()
        }

        return catOptionComboUid?.let {
            d2.categoryModule()
                .categoryCombos()
                .uid(it)
                .blockingGet()
        }
    }

    fun eventToProgramEvent(event: Event): ProgramEventViewModel {
        val orgUnitName: String = getOrgUnitName(event.organisationUnit()) ?: ""
        val showInReportsDataElements = mutableListOf<String>()
        val programStageDataElements = getProgramStageDataElements(event.programStage())
        programStageDataElements.forEach {
            if (it.displayInReports() == true) {
                it.dataElement()?.let { dataElement ->
                    showInReportsDataElements.add(dataElement.uid())
                }
            }
        }
        val data = getData(
            event.trackedEntityDataValues(),
            showInReportsDataElements,
            event.programStage(),
        )
        val hasExpired = isExpired(event)
        val inOrgUnitRange = checkOrgUnitRange(event.organisationUnit(), event.eventDate()!!)
        val catOptComb = getCategoryOptionCombo(event.attributeOptionCombo())
        val attrOptCombo: String =
            if (catOptComb != null && !catOptComb.displayName().equals("default")) {
                catOptComb.displayName()!!
            } else {
                ""
            }
        val state: State = if (event.state() != null) event.state()!! else State.TO_UPDATE

        return ProgramEventViewModel.create(
            event.uid(),
            event.organisationUnit()!!,
            orgUnitName,
            event.eventDate()!!,
            state,
            data,
            event.status()!!,
            hasExpired || !inOrgUnitRange,
            attrOptCombo,
            event.geometry(),
            d2.eventModule().eventService().blockingIsEditable(event.uid()),
        )
    }

    fun eventsToProgramEvents(events: List<Event>): List<ProgramEventViewModel> {
        return events.filter { it.geometry() != null }.map { event -> eventToProgramEvent(event) }
    }

    private fun getOrgUnitName(orgUnitUid: String?) =
        d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet()?.displayName()

    private fun getProgramStageDataElements(programStageUid: String?) =
        d2.programModule().programStageDataElements()
            .byProgramStage().eq(programStageUid)
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet()

    private fun getData(
        dataValues: List<TrackedEntityDataValue>?,
        showInReportsDataElements: MutableList<String>,
        programStage: String?,
    ): List<Pair<String, String>> {
        val data: MutableList<Pair<String, String>> = mutableListOf()

        dataValues?.let {
            val stageSections = getStageSections(programStage).sortedBy { it.sortOrder() }
            val dataElementsOrder = mutableListOf<String>()
            if (stageSections.isEmpty()) {
                val programStageDataElements = getProgramStageDataElements(programStage)
                programStageDataElements.forEach { stage ->
                    stage.dataElement()?.let {
                        dataElementsOrder.add(it.uid())
                    }
                }
            } else {
                stageSections.forEach {
                    dataElementsOrder.addAll(
                        UidsHelper.getUidsList(it.dataElements() as Collection<DataElement>),
                    )
                }
            }

            dataValues.sortedWith(
                Comparator { de1, de2 ->
                    val pos1 = dataElementsOrder.indexOf(de1.dataElement())
                    val pos2 = dataElementsOrder.indexOf(de2.dataElement())
                    pos1.compareTo(pos2)
                },
            ).forEach {
                val dataElement = getDataElement(it.dataElement())
                if (dataElement != null && showInReportsDataElements.contains(dataElement.uid())) {
                    val displayName = if (!dataElement.displayFormName().isNullOrEmpty()) {
                        dataElement.displayFormName()
                    } else if (!dataElement.displayName().isNullOrEmpty()) {
                        dataElement.displayName()
                    } else if (!dataElement.name().isNullOrEmpty()) {
                        dataElement.name()
                    } else {
                        dataElement.uid()
                    }
                    val value = it.userFriendlyValue(d2) ?: ""
                    if (displayName != null) {
                        data.add(Pair.create(displayName, value))
                    }
                }
            }
        }

        return data
    }

    private fun isExpired(event: Event): Boolean {
        val program = d2.programModule().programs().uid(event.program()).blockingGet()
        return DateUtils.getInstance().isEventExpired(
            event.eventDate(),
            event.completedDate(),
            event.status(),
            program?.completeEventsExpiryDays() ?: -1,
            program?.expiryPeriodType(),
            program?.expiryDays() ?: -1,
        )
    }

    private fun checkOrgUnitRange(orgUnitUid: String?, eventDate: Date): Boolean {
        var inRange = true
        val orgUnit = d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet()
        if (orgUnit?.openingDate() != null && eventDate.before(orgUnit.openingDate())) {
            inRange = false
        }
        if (orgUnit?.closedDate() != null && eventDate.after(orgUnit.closedDate())) {
            inRange = false
        }

        return inRange
    }

    private fun getDataElement(dataElement: String?) =
        d2.dataElementModule().dataElements().uid(dataElement).blockingGet()

    private fun getStageSections(programStage: String?) = d2.programModule().programStageSections()
        .byProgramStageUid().eq(programStage)
        .withDataElements()
        .blockingGet()

    private fun getCategoryOptionCombo(attributeOptionCombo: String?) =
        d2.categoryModule().categoryOptionCombos().uid(attributeOptionCombo).blockingGet()

    private fun getEventValues(
        eventUid: String,
        stageUid: String,
    ): List<kotlin.Pair<String, String?>> {
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
                        de?.displayFormName() ?: de?.displayName() ?: "",
                        if (valueRepo.blockingExists()) {
                            valueRepo.blockingGet().userFriendlyValue(d2)
                        } else {
                            null
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

    private fun getCatOptionComboName(categoryOptionComboUid: String?): String? {
        return categoryOptionComboUid?.let {
            d2.categoryModule().categoryOptionCombos().uid(categoryOptionComboUid).blockingGet()
                ?.displayName()
        }
    }
}
