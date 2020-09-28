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
    private var selectedSection: String? = null
    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        disposable.add(
            view.dataEntryFlowable()
                .onBackpressureBuffer()
                .distinctUntilChanged()
                .observeOn(schedulerProvider.io())
                .switchMap { action ->
                    if (action.lastFocusPosition() != null && action.lastFocusPosition() >= 0) {
                        this.lastFocusItem = action.id()
                    }
                    valueStore.save(action.id(), action.value())
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        if (it.valueStoreResult == ValueStoreImpl.ValueStoreResult.VALUE_CHANGED) {
                            activityPresenter.setLastUpdatedUid(lastFocusItem)
                            activityPresenter.nextCalculation(true)
                        }
                    },
                    Timber::e
                )
        )

        disposable.add(
            view.sectionSelectorFlowable()
                .map { setCurrentSection(it) }
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
                    { fields ->
                        view.showFields(fields, lastFocusItem)
                        selectedSection ?: fields
                            .mapNotNull { it.programStageSection() }
                            .firstOrNull()
                            ?.let { selectedSection = it }
                    },
                    { Timber.e(it) }
                )
        )
    }

    private fun setCurrentSection(sectionUid: String): String? {
        if (sectionUid == selectedSection) {
            this.selectedSection = ""
        } else {
            this.selectedSection = sectionUid
        }
        return selectedSection
    }

    fun onDetach() {
        disposable.clear()
    }

    fun onActionButtonClick() {
        activityPresenter.attempFinish()
    }
}
