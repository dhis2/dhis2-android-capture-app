package org.dhis2.utils.category

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import io.reactivex.disposables.CompositeDisposable
import java.util.Date
import java.util.concurrent.TimeUnit
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import timber.log.Timber

class CategoryDialogPresenter(
    val view: CategoryDialogView,
    val d2: D2,
    val type: CategoryDialog.Type,
    val uid: String,
    private val withAccessControl: Boolean,
    private val date: Date?,
    private val catOptMapper: CategoryOptionCategoryDialogItemMapper,
    private val catOptCombMapper: CategoryOptionComboCategoryDialogItemMapper,
    val schedulerProvider: SchedulerProvider
) {

    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        when (type) {
            CategoryDialog.Type.CATEGORY_OPTIONS -> getOptions()
            CategoryDialog.Type.CATEGORY_OPTION_COMBO -> getCategoryOptionCombos()
        }
    }

    private fun getCategoryOptionCombos() {
        disposable.add(
            d2.categoryModule().categoryCombos().uid(uid).get()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setTitle(it.displayName() ?: "-") },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            view.searchSource()
                .debounce(500, TimeUnit.MILLISECONDS, schedulerProvider.io())
                .map { it.toString() }
                .map { textToSearch ->
                    mapCatOptComboToLivePageList(textToSearch)
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setLiveData(it) },
                    { Timber.e(it) }
                )
        )
    }

    private fun createDataSource(
        dataSource: DataSource<CategoryOptionCombo, CategoryDialogItem>
    ): LiveData<PagedList<CategoryDialogItem>> {
        return LivePagedListBuilder(
            object : DataSource.Factory<CategoryOptionCombo, CategoryDialogItem>() {
                override fun create(): DataSource<CategoryOptionCombo, CategoryDialogItem> {
                    return dataSource
                }
            },
            20
        ).build()
    }

    private fun getOptions() {
        disposable.add(
            d2.categoryModule().categories().uid(uid).get()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setTitle(it.displayName() ?: "-") },
                    { Timber.e(it) }
                )
        )
        disposable.add(
            view.searchSource()
                .debounce(500, TimeUnit.MILLISECONDS, schedulerProvider.io())
                .map { textToSearch ->
                    mapCategoryOptionToLivePageList(textToSearch)
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setLiveData(it) },
                    { Timber.e(it) }
                )
        )
    }

    private fun mapCategoryOptionToLivePageList(
        textToSearch: String
    ): LiveData<PagedList<CategoryDialogItem>> {
        var catOptionRepository = d2.categoryModule().categoryOptions()
            .byCategoryUid(uid)

        if (textToSearch.isNotEmpty()) {
            catOptionRepository =
                catOptionRepository.byDisplayName().like("%$textToSearch%")
        }

        if (withAccessControl) {
            catOptionRepository =
                catOptionRepository.byAccessDataWrite().isTrue
        }

        val dataSource =
            catOptionRepository.orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .dataSource
                .mapByPage { options -> filterByDate(options) }
                .map { catOption -> catOptMapper.map(catOption) }

        return LivePagedListBuilder(
            object : DataSource.Factory<CategoryOption, CategoryDialogItem>() {
                override fun create(): DataSource<CategoryOption, CategoryDialogItem> {
                    return dataSource
                }
            },
            20
        ).build()
    }

    private fun mapCatOptComboToLivePageList(
        textToSearch: String
    ): LiveData<PagedList<CategoryDialogItem>> {
        var catOptComboRepository = d2.categoryModule().categoryOptionCombos()
            .byCategoryComboUid().eq(uid)

        if (textToSearch.isNotEmpty()) {
            catOptComboRepository =
                catOptComboRepository.byDisplayName().like("%$textToSearch%")
        }

        val dataSource =
            catOptComboRepository
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .dataSource
                .map { catOption -> catOptCombMapper.map(catOption) }

        return createDataSource(dataSource)
    }

    private fun filterByDate(options: MutableList<CategoryOption>): MutableList<CategoryOption>? {
        val iterator = options.iterator()
        while (iterator.hasNext()) {
            val option = iterator.next()
            if (date != null &&
                (isBeforeDate(option) || isAfterDate(option))
            ) {
                iterator.remove()
            }
        }
        return options
    }

    private fun isBeforeDate(option: CategoryOption): Boolean {
        return option.startDate() != null && date!!.before(option.startDate())
    }

    private fun isAfterDate(option: CategoryOption): Boolean {
        return option.endDate() != null && date!!.after(option.endDate())
    }

    fun getCount(): Int {
        return when (type) {
            CategoryDialog.Type.CATEGORY_OPTIONS ->
                d2.categoryModule().categoryOptions()
                    .byCategoryUid(uid)
                    .blockingCount()
            CategoryDialog.Type.CATEGORY_OPTION_COMBO ->
                d2.categoryModule().categoryOptionCombos()
                    .byCategoryComboUid().eq(uid)
                    .blockingCount()
        }
    }

    fun onDetach() {
        disposable.clear()
    }
}
