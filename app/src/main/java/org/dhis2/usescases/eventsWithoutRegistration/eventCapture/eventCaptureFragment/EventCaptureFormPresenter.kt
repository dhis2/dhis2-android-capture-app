package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.form.model.FieldUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import timber.log.Timber

class EventCaptureFormPresenter(
    val view: EventCaptureFormView,
    private val activityPresenter: EventCaptureContract.Presenter,
    val schedulerProvider: SchedulerProvider
) {
    private var finishing: Boolean = false
    private var selectedSection: String? = null
    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        disposable.add(
            activityPresenter.formFieldsFlowable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { fields -> populateList(fields) },
                    { Timber.e(it) }
                )
        )
    }

    private fun populateList(items: List<FieldUiModel>? = null) {
        view.showFields(items)
        checkFinishing()
        activityPresenter.hideProgress()
        if (items != null) {
            selectedSection ?: items
                .mapNotNull { it.programStageSection }
                .firstOrNull()
                .let { selectedSection = it }
        }
    }

    private fun checkFinishing() {
        if (finishing) {
            view.performSaveClick()
        }
        finishing = false
    }

    fun onDetach() {
        disposable.clear()
    }

    fun onActionButtonClick() {
        activityPresenter.attemptFinish()
    }

    fun setFinishing() {
        finishing = true
    }
}
