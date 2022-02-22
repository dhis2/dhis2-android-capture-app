package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import android.text.TextUtils.isEmpty
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.ADDNEW
import org.dhis2.commons.data.EventCreationType.DEFAULT
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.prefs.Preference.Companion.CURRENT_ORG_UNIT
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialRepository
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import java.util.Date

class ConfigureOrgUnit(
    private val creationType: EventCreationType,
    private val eventInitialRepository: EventInitialRepository,
    private val preferencesProvider: PreferenceProvider,
    private val programUid: String,
    private val eventUid: String?,
    private val initialOrgUnitUid: String?
) {

    operator fun invoke(
        selectedDate: Date? = null,
        selectedOrgUnit: String? = null
    ): EventOrgUnit {
        return EventOrgUnit(
            visible = isVisible(),
            enable = isEnable(),
            fixed = isFixed(),
            selectedOrgUnit = getSelectedOrgUnit(selectedDate, selectedOrgUnit),
            orgUnits = getOrgUnitsByProgramId(),
            programUid = programUid
        )
    }

    private fun isEnable(): Boolean {

        if (getStoredEvent() != null) {
            return false
        }

        val canWrite = eventInitialRepository.accessDataWrite(programUid).blockingFirst()
        val isEnrollmentOpen = eventInitialRepository.isEnrollmentOpen
        if (!canWrite || !isEnrollmentOpen) {
            return false
        }

        return true
    }

    private fun isVisible(): Boolean {
        if (creationType == SCHEDULE) {
            return false
        }

        getStoredEvent()?.let { event ->
            if (!isEmpty(event.enrollment()) && creationType != ADDNEW) {
                return false
            }
        }

        return true
    }

    private fun isFixed(): Boolean {
        return creationType == SCHEDULE || getStoredEvent() != null
    }

    private fun getSelectedOrgUnit(
        selectedDate: Date?,
        selectedOrgUnit: String?
    ): OrganisationUnit? {
        val orgUnit: OrganisationUnit? = selectedDate?.let { date ->
            getOrgUnitBySelectedDate(date)
        } ?: getStoredOrgUnit(selectedOrgUnit)

        orgUnit?.let {
            setCurrentOrgUnit(it.uid())
        }
        return orgUnit
    }

    private fun getOrgUnitBySelectedDate(selectedDate: Date): OrganisationUnit? {
        val dateDBFormat = DateUtils.databaseDateFormat().format(selectedDate)
        val orgUnits = eventInitialRepository.filteredOrgUnits(
            dateDBFormat,
            programUid,
            null
        ).blockingFirst()

        getCurrentOrgUnit()?.let { currentOrgUnitUid ->
            return orgUnits.find { it.uid().equals(currentOrgUnitUid) }
        }

        if (orgUnits.size == 1) {
            return when (creationType) {
                ADDNEW,
                DEFAULT -> orgUnits.firstOrNull()
                else -> null
            }
        }
        return null
    }

    private fun getStoredOrgUnit(selectedOrgUnit: String?): OrganisationUnit? {
        selectedOrgUnit?.let {
            eventInitialRepository.getOrganisationUnit(it).blockingFirst()
                .let { orgUnit ->
                    return orgUnit
                }
        }

        eventUid?.let { eventId ->
            eventInitialRepository.event(eventId).blockingFirst()?.let { event ->
                eventInitialRepository.getOrganisationUnit(event.organisationUnit()).blockingFirst()
                    .let { orgUnit ->
                        return orgUnit
                    }
            }
        }

        val currentOrgUnit = getCurrentOrgUnit() ?: initialOrgUnitUid
        return currentOrgUnit?.let {
            eventInitialRepository.getOrganisationUnit(it).blockingFirst()
        }
    }

    private fun getOrgUnitsByProgramId(): List<OrganisationUnit> {
        return eventInitialRepository.orgUnits(programUid).blockingFirst()
    }

    private fun getStoredEvent(): Event? =
        eventUid?.let { eventInitialRepository.event(eventUid).blockingFirst() }

    private fun getCurrentOrgUnit() =
        if (preferencesProvider.contains(CURRENT_ORG_UNIT)) preferencesProvider.getString(
            CURRENT_ORG_UNIT, null
        ) else null

    private fun setCurrentOrgUnit(organisationUnitUid: String) {
        preferencesProvider.setValue(CURRENT_ORG_UNIT, organisationUnitUid)
    }
}
