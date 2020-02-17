package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.hisp.dhis.android.core.D2
import timber.log.Timber

class EventCaptureFormPresenter(
    val view: EventCaptureFormView,
    private val activityPresenter: EventCaptureContract.Presenter,
    val d2: D2,
    val valueStore: ValueStore,
    val schedulerProvider: SchedulerProvider
) {
    private var lastFocusItem: String = ""
    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        disposable.add(
            view.dataEntryFlowable()
                .onBackpressureBuffer()
                .distinctUntilChanged()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .switchMap { action ->
                    if (action.lastFocusPosition() != null && action.lastFocusPosition() >= 0) {
                        this.lastFocusItem = action.id()
                    }
                    //TODO: REMOVE FROM EMPTY MANDATORY FIELDS IF EXIST
                    valueStore.save(action.id(), action.value())
                }
                .subscribe(
                    {
                        if(it.valueStoreResult == ValueStoreImpl.ValueStoreResult.VALUE_CHANGED){
                            activityPresenter.setLastUpdatedUid(lastFocusItem)
                            activityPresenter.nextCalculation(true)
                        }
                    },
                    Timber::e
                )
        )

        disposable.add(
            view.sectionSelectorFlowable()
                .distinctUntilChanged()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        activityPresenter.goToSection(it)
                    },
                    Timber::e
                )
        )

        disposable.add(
            activityPresenter.formFieldsFlowable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.showFields(it, lastFocusItem) },
                    { Timber.e(it) }
                )
        )
    }

    fun onDetach() {
        disposable.clear()
    }

    fun onActionButtonClick() {
        activityPresenter.attempFinish()
    }

}