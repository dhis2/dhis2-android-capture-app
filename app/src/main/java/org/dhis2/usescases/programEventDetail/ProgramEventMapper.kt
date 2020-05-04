package org.dhis2.usescases.programEventDetail

import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.data.tuples.Pair
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStageSection
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import java.util.Collections
import java.util.Date
import javax.inject.Inject

class ProgramEventMapper @Inject constructor(val d2: D2) {

    fun eventToProgramEvent(event: Event): ProgramEventViewModel {
        val orgUnitName: String = getOrgUnitName(event.organisationUnit()) ?: ""
        val showInReportsDataElements = mutableListOf<String>()
        val programStageDataElements = getProgramStageDataElements(event.programStage())
        programStageDataElements.forEach {
            if(it.displayInReports() == true) {
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
            attrOptCombo
        )
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

            dataValues.sortedWith(Comparator { de1, de2 ->
                val pos1 = dataElementsOrder.indexOf(de1.dataElement())
                val pos2 = dataElementsOrder.indexOf(de2.dataElement())
                pos1.compareTo(pos2)
            }).forEach {
                val dataElement = getDataElement(it.dataElement())
                if (dataElement != null && showInReportsDataElements.contains(dataElement.uid())) {
                    val displayName = if (!dataElement.displayFormName().isNullOrEmpty()) {
                        dataElement.displayFormName()
                    } else if (!dataElement.displayName().isNullOrEmpty()){
                        dataElement.displayName()
                    } else if (!dataElement.name().isNullOrEmpty())
                        dataElement.name()
                    else {
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
        if (orgUnit.openingDate() != null && eventDate.before(orgUnit.openingDate()))
            inRange = false
        if (orgUnit.closedDate() != null && eventDate.after(orgUnit.closedDate()))
            inRange = false

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
