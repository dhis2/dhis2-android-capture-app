package org.dhis2.usescases.enrollment

import io.reactivex.Flowable
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue

interface EnrollmentView : AbstractActivityContracts.View {

    fun setAccess(access: Boolean?)

    fun renderStatus(status: EnrollmentStatus)
    fun showStatusOptions(currentStatus: EnrollmentStatus)

    fun showFields(fields: List<FieldViewModel>)

    fun setSaveButtonVisible(visible: Boolean)

    fun displayTeiInfo(it: List<TrackedEntityAttributeValue>)
    fun rowActions(): Flowable<RowAction>
    fun openEvent(eventUid: String)
    fun openDashboard(enrollmentUid: String)
    fun goBack()
    fun showMissingMandatoryFieldsMessage(emptyMandatoryFields: List<String>)
    fun showErrorFieldsMessage(errorFields: List<String>)
    fun sectionFlowable(): Flowable<String>
}
