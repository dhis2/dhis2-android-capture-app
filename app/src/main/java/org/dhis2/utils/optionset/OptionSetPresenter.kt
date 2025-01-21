package org.dhis2.utils.optionset

import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.option.Option
import timber.log.Timber
import java.util.concurrent.TimeUnit
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel as TableSpinnerViewModel

class OptionSetPresenter(
    val view: OptionSetView,
    val d2: D2,
    val schedulerProvider: SchedulerProvider,
) {
    var disposable: CompositeDisposable = CompositeDisposable()
    private var optionSetUid: String? = null

    fun init(optionSet: FieldUiModel) {
        this.optionSetUid = optionSet.optionSet
        getOptions()
    }

    fun init(optionSetTable: TableSpinnerViewModel) {
        this.optionSetUid = optionSetTable.optionSet()
        getOptions()
    }

    private fun getOptions() {
        disposable.add(
            view.searchSource()
                .debounce(500, TimeUnit.MILLISECONDS, schedulerProvider.io())
                .map { it.toString() }
                .map { textToSearch ->
                    var optionRepository = d2.optionModule().options()
                        .byOptionSetUid().eq(optionSetUid)

                    if (textToSearch.isNotEmpty()) {
                        optionRepository = optionRepository.byDisplayName().like("%$textToSearch%")
                    }

                    val dataSource = optionRepository
                        .dataSource
                        .mapByPage { it.sortedWith(compareBy { option -> option.sortOrder() }) }

                    LivePagedListBuilder(
                        object : DataSource.Factory<Option, Option>() {
                            override fun create(): DataSource<Option, Option> {
                                return dataSource
                            }
                        },
                        20,
                    ).build()
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setLiveData(it) },
                    { Timber.e(it) },
                ),
        )
    }

    fun getCount(optionSetUid: String): Int? {
        return d2.optionModule().options().byOptionSetUid().eq(optionSetUid).blockingCount()
    }

    fun onDetach() {
        disposable.clear()
    }
}
