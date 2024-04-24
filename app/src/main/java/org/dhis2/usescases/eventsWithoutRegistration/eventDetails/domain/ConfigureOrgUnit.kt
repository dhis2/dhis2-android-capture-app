package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.ADDNEW
import org.dhis2.commons.data.EventCreationType.DEFAULT
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.prefs.Preference.Companion.CURRENT_ORG_UNIT
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import java.util.Date

class ConfigureOrgUnit(
    private val creationType: EventCreationType,
    private val repository: EventDetailsRepository,
    private val preferencesProvider: PreferenceProvider,
    private val programUid: String,
    private val initialOrgUnitUid: String?,
) {

    operator fun invoke(
        selectedDate: Date? = null,
        selectedOrgUnit: String? = null,
    ): Flow<EventOrgUnit> {
        return flowOf(
            EventOrgUnit(
                visible = isVisible(),
                enable = isEnable(),
                fixed = isFixed(),
                selectedOrgUnit = getSelectedOrgUnit(selectedDate, selectedOrgUnit),
                orgUnits = getOrgUnitsByProgramId(),
                programUid = programUid,
            ),
        )
    }

    private fun isEnable(): Boolean {
        if (repository.getEvent() != null) {
            return false
        }

        val canWrite = repository.hasAccessDataWrite()
        val isEnrollmentOpen = repository.isEnrollmentOpen()
        if (!canWrite || !isEnrollmentOpen) {
            return false
        }

        return true
    }

    private fun isVisible(): Boolean {
        return creationType != SCHEDULE
    }

    private fun isFixed(): Boolean {
        return creationType == SCHEDULE || repository.getEvent() != null
    }

    private fun getSelectedOrgUnit(
        selectedDate: Date?,
        selectedOrgUnit: String?,
    ): OrganisationUnit? {
        val orgUnit: OrganisationUnit? = selectedDate?.let { date ->
            getOrgUnitBySelectedDate(date) ?: getStoredOrgUnit(selectedOrgUnit)
        } ?: getStoredOrgUnit(selectedOrgUnit) ?: getOrgUnitIfOnlyOne()

        orgUnit?.let {
            setCurrentOrgUnit(it.uid())
        }
        return orgUnit
    }

    private fun getOrgUnitBySelectedDate(selectedDate: Date): OrganisationUnit? {
        val dateDBFormat = DateUtils.databaseDateFormat().format(selectedDate)
        val orgUnits = repository.getFilteredOrgUnits(dateDBFormat, null)

        getCurrentOrgUnit()?.let { currentOrgUnitUid ->
            return orgUnits.find { it.uid().equals(currentOrgUnitUid) }
        }

        if (orgUnits.size == 1) {
            return when (creationType) {
                ADDNEW,
                DEFAULT,
                -> orgUnits.firstOrNull()
                else -> null
            }
        }
        return null
    }

    private fun getStoredOrgUnit(selectedOrgUnit: String?): OrganisationUnit? {
        if (!selectedOrgUnit.isNullOrEmpty()) {
            return repository.getOrganisationUnit(selectedOrgUnit)
        }

        repository.getEvent()?.let { event ->
            if (event.organisationUnit() != null) {
                repository.getOrganisationUnit(event.organisationUnit()!!)
                    .let { orgUnit ->
                        return orgUnit
                    }
            }
        }

        val currentOrgUnit = getCurrentOrgUnit() ?: initialOrgUnitUid
        return currentOrgUnit?.let {
            repository.getOrganisationUnit(it)
        }
    }

    private fun getOrgUnitsByProgramId(): List<OrganisationUnit> {
        return repository.getOrganisationUnits()
    }

    private fun getOrgUnitIfOnlyOne() =
        getOrgUnitsByProgramId().takeIf { it.size == 1 }?.firstOrNull()

    private fun getCurrentOrgUnit() = if (preferencesProvider.contains(CURRENT_ORG_UNIT)) {
        preferencesProvider.getString(
            CURRENT_ORG_UNIT,
            null,
        )
    } else {
        null
    }

    private fun setCurrentOrgUnit(organisationUnitUid: String) {
        preferencesProvider.setValue(CURRENT_ORG_UNIT, organisationUnitUid)
    }
}
