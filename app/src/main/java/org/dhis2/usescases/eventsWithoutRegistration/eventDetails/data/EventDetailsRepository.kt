package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data

import java.util.Date
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage

interface EventDetailsRepository {

    fun getProgramStage(): ProgramStage

    fun getObjectStyle(): ObjectStyle?

    fun getEditableStatus(): EventEditableStatus?

    fun getEvent(): Event?

    fun getProgram(): Program?

    fun getMinDaysFromStartByProgramStage(): Int

    fun getStageLastDate(enrollmentUid: String?): Date

    fun hasAccessDataWrite(): Boolean

    fun isEnrollmentOpen(): Boolean

    fun getFilteredOrgUnits(
        date: String?,
        parentUid: String?
    ): List<OrganisationUnit>

    fun getOrganisationUnit(orgUnitUid: String): OrganisationUnit?

    fun getOrganisationUnits(): List<OrganisationUnit>

    fun getGeometryModel(): FieldUiModel

    fun getCatOptionCombos(categoryComboUid: String): List<CategoryOptionCombo>

    fun getCategoryOptionCombo(
        categoryComboUid: String?,
        categoryOptionsUid: List<String?>?
    ): String?

    fun getCatOption(selectedOption: String?): CategoryOption?

    fun getCatOptionSize(uid: String?): Int

    fun getCategoryOptions(categoryUid: String): List<CategoryOption>

    fun getOptionsFromCatOptionCombo(): Map<String, CategoryOption>?

    fun catCombo(): CategoryCombo

    fun updateEvent(
        selectedDate: Date,
        selectedOrgUnit: String?,
        catOptionComboUid: String?,
        coordinates: String?
    ): Event
}
