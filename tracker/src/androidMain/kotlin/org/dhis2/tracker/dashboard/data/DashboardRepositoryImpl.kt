package org.dhis2.tracker.dashboard.data

import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.error.withDomainErrors
import org.hisp.dhis.android.core.D2

class DashboardRepositoryImpl(
    private val d2: D2,
    private val dispatcher: Dispatcher,
    private val domainErrorMapper: DomainErrorMapper,
) : DashboardRepository {
    override suspend fun enrollmentCategoryComboLabel(programUid: String): String? =
        execute {
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet()
                ?.enrollmentCategoryCombo
                ?.let { categoryCombo ->
                    d2
                        .categoryModule()
                        .categoryCombos()
                        .uid(categoryCombo.uid)
                        .blockingGet()
                        ?.takeIf { it.isDefault == false }
                        ?.displayName
                }
        }

    override suspend fun enrollmentCategoryOptionComboLabel(enrollmentAttributeOptionCombo: String): String =
        execute {
            d2
                .categoryModule()
                .categoryOptionCombos()
                .uid(enrollmentAttributeOptionCombo)
                .blockingGet()
                ?.displayName
                ?: ""
        }

    private suspend fun <T> execute(block: suspend () -> T): T =
        withContext(dispatcher.io) {
            domainErrorMapper.withDomainErrors(block)
        }
}
