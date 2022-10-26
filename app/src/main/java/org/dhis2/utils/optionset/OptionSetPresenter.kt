package org.dhis2.utils.optionset

import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel as TableSpinnerViewModel
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.option.Option
import timber.log.Timber

class OptionSetPresenter(
    val view: OptionSetView,
    val d2: D2,
    val schedulerProvider: SchedulerProvider
) {

    private lateinit var optionSetOptionHandler: OptionSetOptionsHandler
    var disposable: CompositeDisposable = CompositeDisposable()
    private var optionSetUid: String? = null

    fun init(optionSet: FieldUiModel) {
        this.optionSetUid = optionSet.optionSet
        this.optionSetOptionHandler = OptionSetOptionsHandler(
            optionSet.optionSetConfiguration?.optionsToHide,
            optionSet.optionSetConfiguration?.optionsToShow,
            null
        )
        getOptions()
    }

    fun init(optionSetTable: TableSpinnerViewModel) {
        this.optionSetUid = optionSetTable.optionSet()
        this.optionSetOptionHandler = OptionSetOptionsHandler(
            optionSetTable.optionsToHide,
            null,
            optionSetTable.optionGroupsToHide
        )
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

                    val(finalOptionsToHide, finalOptionsToShow) =
                        optionSetOptionHandler.handleOptions()

                    if (finalOptionsToShow.isNotEmpty()) {
                        optionRepository = optionRepository.byUid().`in`(finalOptionsToShow)
                    }

                    if (finalOptionsToHide.isNotEmpty()) {
                        optionRepository = optionRepository.byUid().notIn(finalOptionsToHide)
                    }

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
                        20
                    ).build()
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setLiveData(it) },
                    { Timber.e(it) }
                )
        )
    }

    fun getCount(optionSetUid: String): Int? {
        return d2.optionModule().options().byOptionSetUid().eq(optionSetUid).blockingCount()
    }

    fun onDetach() {
        disposable.clear()
    }
}
