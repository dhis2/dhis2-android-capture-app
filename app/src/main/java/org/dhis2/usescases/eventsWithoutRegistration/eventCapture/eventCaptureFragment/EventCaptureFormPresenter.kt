package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import timber.log.Timber

class EventCaptureFormPresenter(
    val view: EventCaptureFormView,
    private val activityPresenter: EventCaptureContract.Presenter,
    val schedulerProvider: SchedulerProvider,
    private val onFieldActionProcessor: FlowableProcessor<RowAction>,
    private val formRepository: FormRepository
) {
    private var finishing: Boolean = false
    private var selectedSection: String? = null
    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        disposable.add(
            onFieldActionProcessor.onBackpressureBuffer().distinctUntilChanged()
                .doOnNext { activityPresenter.showProgress() }
                .observeOn(schedulerProvider.io())
                .switchMap { rowAction ->
                    Flowable.just(formRepository.processUserAction(rowAction))
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { result ->
                        result.valueStoreResult?.let {
                            if (result.valueStoreResult == ValueStoreResult.VALUE_CHANGED
                            ) {
                                activityPresenter.setValueChanged(result.uid)
                                activityPresenter.nextCalculation(true)
                            } else {
                                populateList()
                            }
                        } ?: activityPresenter.hideProgress()
                    },
                    Timber::e
                )
        )

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
        view.showFields(formRepository.composeList(items).toMutableList())
        checkFinishing(true)
        activityPresenter.hideProgress()
        if (items != null) {
            selectedSection ?: items
                .mapNotNull { it.programStageSection }
                .firstOrNull()
                .let { selectedSection = it }
        }
    }

    private fun checkFinishing(canFinish: Boolean) {
        if (finishing && canFinish) {
            view.performSaveClick()
        }
        finishing = false
    }

    fun onDetach() {
        disposable.clear()
    }

    fun onActionButtonClick() {
        activityPresenter.attempFinish()
    }

    fun <E> Iterable<E>.updated(index: Int, elem: E): List<E> =
        mapIndexed { i, existing -> if (i == index) elem else existing }

    fun setFinishing() {
        finishing = true
    }

    fun saveValue(uid: String, value: String?) {
        onFieldActionProcessor.onNext(
            RowAction(
                id = uid,
                value = value,
                type = ActionType.ON_SAVE
            )
        )
    }
}
