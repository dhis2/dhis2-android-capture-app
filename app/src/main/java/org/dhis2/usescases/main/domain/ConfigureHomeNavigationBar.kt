package org.dhis2.usescases.main.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository
import org.dhis2.usescases.main.domain.model.BottomNavigationItem

class ConfigureHomeNavigationBar(
    private val homeRepository: HomeRepository,
) : UseCase<Unit, List<BottomNavigationItem>> {
    override suspend operator fun invoke(input: Unit) =
        try {
            val list =
                buildList {
                    add(BottomNavigationItem.Program)
                    if (homeRepository.hasHomeAnalytics()) {
                        add(BottomNavigationItem.Analytics)
                    }
                }
            Result.success(list)
        } catch (_: DomainError) {
            Result.success(listOf(BottomNavigationItem.Program))
        }
}
