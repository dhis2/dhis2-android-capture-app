package org.dhis2.usescases.enrollment

import io.reactivex.Flowable
import java.util.Date
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.usescases.general.AbstractActivityContracts
import org.dhis2.utils.DatePickerUtils
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType

interface EnrollmentView : AbstractActivityContracts.View {

    fun setAccess(access: Boolean?)

    fun renderStatus(status: EnrollmentStatus)
    fun showStatusOptions(currentStatus: EnrollmentStatus)

    fun displayOrgUnit(ou: OrganisationUnit)

    fun showFields(fields: List<FieldViewModel>)

    fun showSaveButton()
    fun hideSaveButton()
    fun showAdjustingForm()
    fun hideAdjustingForm()

    fun displayEnrollmentCoordinates(enrollmentCoordinatesData: Pair<Program, Enrollment>?)
    fun displayTeiCoordinates(
        teiCoordinatesData: Pair<TrackedEntityType, TrackedEntityInstance>?
    )

    fun setDateLabels(enrollmentDateLabel: String?, indicendDateLabel: String?)
    fun setUpIncidentDate(incidentDate: Date?)
    fun setUpEnrollmentDate(enrollmentDate: Date?)
    fun onReportDateClick()
    fun onIncidentDateClick()
    fun showCalendar(
        date: Date?,
        minDate: Date?,
        maxDate: Date?,
        label: String,
        allowFuture: Boolean,
        listener: DatePickerUtils.OnDatePickerClickListener
    )

    fun blockDates(blockEnrollmentDate: Boolean, blockIncidentDate: Boolean)
    fun displayTeiInfo(it: List<TrackedEntityAttributeValue>)
    fun rowActions(): Flowable<RowAction>
    fun openEvent(eventUid: String)
    fun openDashboard(enrollmentUid: String)
    fun goBack()
    fun showMissingMandatoryFieldsMessage()
    fun showErrorFieldsMessage()

    fun requestFocus()
    fun performSaveClick()
}
