package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.hisp.dhis.android.core.D2
import timber.log.Timber

class EventCaptureFormPresenter(
    val view: EventCaptureFormView,
    private val activityPresenter: EventCaptureContract.Presenter,
    val d2: D2,
    val valueStore: ValueStore,
    val schedulerProvider: SchedulerProvider,
    private val onFieldActionProcessor: FlowableProcessor<RowAction>,
    private val focusProcessor: FlowableProcessor<HashMap<String, Boolean>>
) {
    private var focusedItem: String? = null
    private var selectedSection: String? = null
    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        disposable.add(
            onFieldActionProcessor
                .onBackpressureBuffer()
                .distinctUntilChanged()
                .doOnNext { activityPresenter.showProgress() }
                .observeOn(schedulerProvider.io())
                .switchMap { action ->
                    valueStore.save(action.id(), action.value())
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        if (it.valueStoreResult == ValueStoreImpl.ValueStoreResult.VALUE_CHANGED) {
                            activityPresenter.nextCalculation(true)
                        }
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
                        view.showFields(setFocusedItem(fields))
                        activityPresenter.hideProgress()
                        selectedSection ?: fields
                            .mapNotNull { it.programStageSection() }
                            .firstOrNull()
                            ?.let { selectedSection = it }
                    },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            focusProcessor
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { map ->
                        val uid = map.keys.first()
                        val isOnNext = map.values.first()

                        focusedItem = if (isOnNext) {
                            getNextItem(uid)
                        } else {
                            uid
                        }
                        val list = activityPresenter.formFieldsFlowable().blockingFirst()
                        view.showFields(setFocusedItem(list))
                    },
                    {
                        Timber.e(it)
                    }
                )
        )
    }

    fun onDetach() {
        disposable.clear()
    }

    fun onActionButtonClick() {
        activityPresenter.attempFinish()
    }

    private fun getNextItem(currentItemUid: String): String? {
        val list = activityPresenter.formFieldsFlowable().blockingFirst()
        val oldItem = list.find { it.uid() == currentItemUid }
        val pos = list.indexOf(oldItem)
        if (pos < list.size - 1) {
            return list[pos + 1].getUid()
        }
        return null
    }

    private fun setFocusedItem(list: MutableList<FieldViewModel>) = focusedItem?.let {
        val oldItem = list.find { it.uid() == focusedItem }
        val pos = list.indexOf(oldItem)
        val newItem = oldItem?.withFocus()
        list.updated(pos, newItem) as MutableList<FieldViewModel>
    } ?: list

    fun <E> Iterable<E>.updated(index: Int, elem: E): List<E> =
        mapIndexed { i, existing -> if (i == index) elem else existing }
}
