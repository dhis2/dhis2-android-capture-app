package org.dhis2.utils.category

import androidx.paging.ItemKeyedDataSource
import org.dhis2.data.dhislogic.inDateRange
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.category.CategoryOptionComboCollectionRepository
import java.util.Date

class CategoyOptionComboSource(
    val d2: D2,
    val catOptComboRepository: CategoryOptionComboCollectionRepository,
    val withAccessControl: Boolean,
    val date: Date?,
) : ItemKeyedDataSource<CategoryOptionCombo, CategoryOptionCombo>() {

    private var isComplete = false

    override fun loadInitial(
        params: LoadInitialParams<CategoryOptionCombo>,
        callback: LoadInitialCallback<CategoryOptionCombo>,
    ) {
        callback.onResult(loadPages(params.requestedLoadSize))
    }

    override fun loadAfter(
        params: LoadParams<CategoryOptionCombo>,
        callback: LoadCallback<CategoryOptionCombo>,
    ) {
//        callback.onResult(loadPages(params.requestedLoadSize))
    }

    override fun loadBefore(
        params: LoadParams<CategoryOptionCombo>,
        callback: LoadCallback<CategoryOptionCombo>,
    ) {
        // do nothing
    }

    override fun getKey(item: CategoryOptionCombo): CategoryOptionCombo {
        return item
    }

    private fun loadPages(requestedLoadSize: Int): List<CategoryOptionCombo> {
        return if (isComplete) {
            emptyList()
        } else {
            isComplete = true
            catOptComboRepository.orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .blockingGet().filter { catOptionCombo ->
                    var hasOption = categoryOptionComboHasOptions(catOptionCombo.uid())
                    var writeAccess = true
                    var openAccess = true
                    if (withAccessControl) {
                        writeAccess = canWriteInCatOptCombo(catOptionCombo.uid())
                    }
                    if (date != null) {
                        openAccess =
                            doesNotHaveFilteredByDate(categoryOptions(catOptionCombo.uid()))
                    }
                    hasOption && writeAccess && openAccess
                }
        }
    }

    private fun categoryOptionComboHasOptions(categoryOptionComboUid: String): Boolean {
        return d2.categoryModule().categoryOptionCombos().withCategoryOptions()
            .uid(categoryOptionComboUid)
            .blockingGet()?.categoryOptions()?.isNotEmpty() ?: false
    }

    private fun categoryOptions(categoryOptionComboUid: String): List<CategoryOption> {
        return d2.categoryModule().categoryOptions()
            .byCategoryOptionComboUid(categoryOptionComboUid)
            .blockingGet()
    }

    private fun canWriteInCatOptCombo(categoryOptionComboUid: String): Boolean {
        return d2.categoryModule().categoryOptions()
            .byCategoryOptionComboUid(categoryOptionComboUid)
            .byAccessDataWrite().isFalse
            .blockingIsEmpty()
    }

    private fun doesNotHaveFilteredByDate(options: List<CategoryOption>): Boolean {
        return options.size == options.filter { it.inDateRange(date) }.size
    }
}
