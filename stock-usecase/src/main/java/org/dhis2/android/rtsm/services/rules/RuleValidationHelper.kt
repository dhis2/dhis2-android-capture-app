package org.dhis2.android.rtsm.services.rules

import io.reactivex.Flowable
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.data.models.Transaction
import org.hisp.dhis.android.core.usecase.stock.StockUseCase
import org.hisp.dhis.rules.models.RuleEffect

interface RuleValidationHelper {
    fun evaluate(
        entry: StockEntry,
        program: String,
        transaction: Transaction,
        eventUid: String? = null,
        stockUseCase: StockUseCase,
    ): Flowable<List<RuleEffect>>
}
