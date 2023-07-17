package org.dhis2.form.data

import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.option.OptionCollectionRepository

class SearchOptionSetOption(
    private val optionRepository: OptionCollectionRepository
) {
    operator fun invoke(
        optionSetUid: String?,
        textToSearch: String,
        optionsToShow: List<String>,
        optionsToHide: List<String>
    ): List<Option> {
        var repository = optionRepository
            .byOptionSetUid().eq(optionSetUid)

        if (optionsToShow.isNotEmpty()) {
            repository = repository.byUid().`in`(optionsToShow)
        }
        if (optionsToHide.isNotEmpty()) {
            repository = repository.byUid().notIn(optionsToHide)
        }
        if (textToSearch.isNotEmpty()) {
            repository = repository.byDisplayName().like("%$textToSearch%")
        }

        return repository.orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet()
    }
}
