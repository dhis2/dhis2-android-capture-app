package org.dhis2.usescases.programEventDetail

import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.data.tuples.Pair
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import java.util.Date
import javax.inject.Inject

class ProgramEventMapper @Inject constructor(val d2: D2) {

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
            event.programStage()
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
            isEventEditable(event.uid())
        )
    }

    fun isEventEditable(eventUid: String): Boolean {
        val event =
            d2.eventModule().events().uid(eventUid).blockingGet()
        val program = d2.programModule().programs().uid(event.program()).blockingGet()
        val stage =
            d2.programModule().programStages().uid(event.programStage()).blockingGet()
        val isExpired = DateUtils.getInstance().isEventExpired(
            event.eventDate(),
            event.completedDate(),
            event.status(),
            program.completeEventsExpiryDays()!!,
            if (stage.periodType() != null) stage.periodType() else program.expiryPeriodType(),
            program.expiryDays()!!
        )
        val blockAfterComplete =
            event.status() == EventStatus.COMPLETED && stage.blockEntryForm()!!
        val isInCaptureOrgUnit = d2.organisationUnitModule().organisationUnits()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .byUid().eq(event.organisationUnit()).one().blockingExists()
        val hasCatComboAccess =
            event.attributeOptionCombo() == null || getCatComboAccess(event)
        return !blockAfterComplete && !isExpired &&
                getAccessDataWrite(eventUid) && inOrgUnitRange(eventUid) && isInCaptureOrgUnit && hasCatComboAccess
    }

    private fun inOrgUnitRange(eventUid: String): Boolean {
        val event =
            d2.eventModule().events().uid(eventUid).blockingGet()
        val orgUnitUid = event.organisationUnit()
        val eventDate = event.eventDate()
        var inRange = true
        val orgUnit =
            d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet()
        if (eventDate != null && orgUnit.openingDate() != null && eventDate.before(orgUnit.openingDate())) inRange =
            false
        if (eventDate != null && orgUnit.closedDate() != null && eventDate.after(orgUnit.closedDate())) inRange =
            false
        return inRange
    }

    fun getAccessDataWrite(eventUid: String): Boolean {
        var canWrite: Boolean
        canWrite = d2.programModule().programs().uid(
            d2.eventModule().events().uid(eventUid).blockingGet().program()
        ).blockingGet().access().data().write()
        if (canWrite) canWrite = d2.programModule().programStages().uid(
            d2.eventModule().events().uid(eventUid).blockingGet().programStage()
        ).blockingGet().access().data().write()
        return canWrite
    }

    private fun getCatComboAccess(event: Event): Boolean {
        return if (event.attributeOptionCombo() != null) {
            val optionUid =
                UidsHelper.getUidsList(
                    d2.categoryModule()
                        .categoryOptionCombos().withCategoryOptions()
                        .uid(event.attributeOptionCombo())
                        .blockingGet().categoryOptions()
                )
            val options =
                d2.categoryModule().categoryOptions().byUid().`in`(optionUid).blockingGet()
            var access = true
            val eventDate = event.eventDate()
            for (option in options) {
                if (!option.access().data().write()) access = false
                if (eventDate != null && option.startDate() != null && eventDate.before(option.startDate())) access =
                    false
                if (eventDate != null && option.endDate() != null && eventDate.after(option.endDate())) access =
                    false
            }
            access
        } else true
    }



    fun eventsToProgramEvents(events: List<Event>): List<ProgramEventViewModel> {
        return events.map { event -> eventToProgramEvent(event) }
    }

    private fun getOrgUnitName(orgUnitUid: String?) =
        d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet().displayName()

    private fun getProgramStageDataElements(programStageUid: String?) =
        d2.programModule().programStageDataElements()
            .byProgramStage().eq(programStageUid)
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet()

    private fun getData(
        dataValues: List<TrackedEntityDataValue>?,
        showInReportsDataElements: MutableList<String>,
        programStage: String?
    ): List<Pair<String, String>> {
        val data: MutableList<Pair<String, String>> = mutableListOf()

        dataValues?.let {
            val stageSections = getStageSections(programStage)
            stageSections.sortBy { it.sortOrder() }
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
                    dataElementsOrder.addAll(UidsHelper.getUidsList(it.dataElements()))
                }
            }

            dataValues.sortedWith(
                Comparator { de1, de2 ->
                    val pos1 = dataElementsOrder.indexOf(de1.dataElement())
                    val pos2 = dataElementsOrder.indexOf(de2.dataElement())
                    pos1.compareTo(pos2)
                }
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
            program.completeEventsExpiryDays() ?: -1,
            program.expiryPeriodType(),
            program.expiryDays() ?: -1
        )
    }

    private fun checkOrgUnitRange(orgUnitUid: String?, eventDate: Date): Boolean {
        var inRange = true
        val orgUnit = d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet()
        if (orgUnit.openingDate() != null && eventDate.before(orgUnit.openingDate())) {
            inRange = false
        }
        if (orgUnit.closedDate() != null && eventDate.after(orgUnit.closedDate())) {
            inRange = false
        }

        return inRange
    }

    private fun getDataElement(dataElement: String?) =
        d2.dataElementModule().dataElements().uid(dataElement).blockingGet()

    private fun getStageSections(programStage: String?) =
        d2.programModule().programStageSections()
            .byProgramStageUid().eq(programStage)
            .withDataElements()
            .blockingGet()

    private fun getCategoryOptionCombo(attributeOptionCombo: String?) =
        d2.categoryModule().categoryOptionCombos().uid(attributeOptionCombo).blockingGet()
}
