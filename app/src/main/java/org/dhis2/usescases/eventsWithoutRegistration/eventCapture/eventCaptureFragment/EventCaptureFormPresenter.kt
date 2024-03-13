package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.AUTH_ALL
import org.dhis2.data.dhislogic.AUTH_UNCOMPLETE_EVENT
import org.dhis2.form.data.DataIntegrityCheckResult
import org.dhis2.form.data.FieldsWithErrorResult
import org.dhis2.form.data.FieldsWithWarningResult
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.data.NotSavedResult
import org.dhis2.form.data.SuccessfulResult
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ReOpenEventUseCase
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.event.EventNonEditableReason
import org.hisp.dhis.android.core.event.EventStatus

class EventCaptureFormPresenter(
    private val view: EventCaptureFormView,
    private val activityPresenter: EventCaptureContract.Presenter,
    private val d2: D2,
    private val eventUid: String,
    private val resourceManager: ResourceManager,
    private val reOpenEventUseCase: ReOpenEventUseCase,
    private val dispatcherProvider: DispatcherProvider,
) {

    fun handleDataIntegrityResult(result: DataIntegrityCheckResult) {
        when (result) {
            is FieldsWithErrorResult -> activityPresenter.attemptFinish(
                result.canComplete,
                result.onCompleteMessage,
                result.fieldUidErrorList,
                result.mandatoryFields,
                result.warningFields,
            )

            is FieldsWithWarningResult -> activityPresenter.attemptFinish(
                result.canComplete,
                result.onCompleteMessage,
                emptyList(),
                emptyMap(),
                result.fieldUidWarningList,
            )

            is MissingMandatoryResult -> activityPresenter.attemptFinish(
                result.canComplete,
                result.onCompleteMessage,
                result.errorFields,
                result.mandatoryFields,
                result.warningFields,
            )

            is SuccessfulResult -> activityPresenter.attemptFinish(
                result.canComplete,
                result.onCompleteMessage,
                emptyList(),
                emptyMap(),
                emptyList(),
            )

            NotSavedResult -> {
                // Nothing to do in this case
            }
        }
    }

    fun showOrHideSaveButton() {
        val isEditable =
            d2.eventModule().eventService().getEditableStatus(eventUid = eventUid).blockingGet()

        when (isEditable) {
            is EventEditableStatus.Editable -> {
                view.showSaveButton()
            }

            is EventEditableStatus.NonEditable -> {
                view.hideSaveButton()
                configureNonEditableMessage(isEditable.reason)
            }
        }
    }

    private fun configureNonEditableMessage(eventNonEditableReason: EventNonEditableReason) {
        val (reason, canBeReOpened) = when (eventNonEditableReason) {
            EventNonEditableReason.BLOCKED_BY_COMPLETION -> resourceManager.getString(R.string.blocked_by_completion) to canReopen()
            EventNonEditableReason.EXPIRED -> resourceManager.getString(R.string.edition_expired) to false
            EventNonEditableReason.NO_DATA_WRITE_ACCESS -> resourceManager.getString(R.string.edition_no_write_access) to false
            EventNonEditableReason.EVENT_DATE_IS_NOT_IN_ORGUNIT_RANGE -> resourceManager.getString(R.string.event_date_not_in_orgunit_range) to false
            EventNonEditableReason.NO_CATEGORY_COMBO_ACCESS -> resourceManager.getString(R.string.edition_no_catcombo_access) to false
            EventNonEditableReason.ENROLLMENT_IS_NOT_OPEN -> resourceManager.formatWithEnrollmentLabel(
                null,
                R.string.edition_enrollment_is_no_open_V2,
                1,
            ) to false

            EventNonEditableReason.ORGUNIT_IS_NOT_IN_CAPTURE_SCOPE -> resourceManager.getString(R.string.edition_orgunit_capture_scope) to false
        }
        view.showNonEditableMessage(reason, canBeReOpened)
    }

    fun reOpenEvent() {
        CoroutineScope(dispatcherProvider.ui()).launch {
            reOpenEventUseCase(eventUid).fold(
                onSuccess = {
                    view.onReopen()
                    view.showSaveButton()
                    view.hideNonEditableMessage()
                },
                onFailure = { error ->
                    resourceManager.parseD2Error(error)
                },
            )
        }
    }

    private fun canReopen(): Boolean = getEvent()?.let {
        it.status() == EventStatus.COMPLETED && hasReopenAuthority()
    } ?: false

    private fun getEvent(): Event? {
        return d2.eventModule().events().uid(eventUid).blockingGet()
    }

    private fun hasReopenAuthority(): Boolean = d2.userModule().authorities()
        .byName().`in`(AUTH_UNCOMPLETE_EVENT, AUTH_ALL)
        .one()
        .blockingExists()
}
