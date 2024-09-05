package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import androidx.lifecycle.LiveData
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.form.model.EventMode
import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model.EventCompletionDialog
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.common.ValidationStrategy
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import java.util.Date

class EventCaptureContract {
    interface View : AbstractActivityContracts.View {
        fun renderInitialInfo(stageName: String)
        val presenter: Presenter
        fun updatePercentage(primaryValue: Float)
        fun showCompleteActions(eventCompletionDialog: EventCompletionDialog)
        fun restartDataEntry()
        fun finishDataEntry()
        fun saveAndFinish()
        fun showSnackBar(messageId: Int, programStage: String)
        fun attemptToSkip()
        fun attemptToReschedule()
        fun showEventIntegrityAlert()
        fun updateNoteBadge(numberOfNotes: Int)
        fun goBack()
        fun showProgress()
        fun hideProgress()
        fun showNavigationBar()
        fun hideNavigationBar()

        //EyeSeeTea customization
        fun updateProgramStageName(stageName: String)
    }

    interface Presenter : AbstractActivityContracts.Presenter {
        fun observeActions(): LiveData<EventCaptureAction>
        fun init()
        fun onBackClick()
        fun attemptFinish(
            canComplete: Boolean,
            onCompleteMessage: String?,
            errorFields: List<FieldWithIssue>,
            emptyMandatoryFields: Map<String, String>,
            warningFields: List<FieldWithIssue>,
            eventMode: EventMode? = null,
        )

        fun isEnrollmentOpen(): Boolean
        fun completeEvent(addNew: Boolean)
        fun deleteEvent()
        fun skipEvent()
        fun rescheduleEvent(time: Date)
        fun canWrite(): Boolean
        fun hasExpired(): Boolean
        fun initNoteCounter()
        fun refreshTabCounters()
        fun hideProgress()
        fun showProgress()
        fun getCompletionPercentageVisibility(): Boolean
        fun emitAction(onBack: EventCaptureAction)
        fun programStage(): String

        //EyeSeeTea customization
        fun refreshProgramStage()
    }

    interface EventCaptureRepository {
        fun eventIntegrityCheck(): Flowable<Boolean>
        fun programStageName(): Flowable<String>
        fun orgUnit(): Flowable<OrganisationUnit>
        fun completeEvent(): Observable<Boolean>
        fun eventStatus(): Flowable<EventStatus>
        val isEnrollmentOpen: Boolean
        fun deleteEvent(): Observable<Boolean>
        fun updateEventStatus(skipped: EventStatus): Observable<Boolean>
        fun rescheduleEvent(time: Date): Observable<Boolean>
        fun programStage(): Observable<String>
        val accessDataWrite: Boolean
        val isEnrollmentCancelled: Boolean
        fun isEventEditable(eventUid: String): Boolean
        fun canReOpenEvent(): Single<Boolean>
        fun isCompletedEventExpired(eventUid: String): Observable<Boolean>
        val noteCount: Single<Int>
        fun showCompletionPercentage(): Boolean
        fun hasAnalytics(): Boolean
        fun hasRelationships(): Boolean
        fun validationStrategy(): ValidationStrategy
    }
}
