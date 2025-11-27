package org.dhis2.android.rtsm.utils

import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.commons.bindings.stockCount
import org.dhis2.commons.bindings.stockDiscarded
import org.dhis2.commons.bindings.stockDistribution
import org.hisp.dhis.android.core.usecase.stock.StockUseCase

object ConfigUtils {
    @JvmStatic
    fun getTransactionDataElement(
        transactionType: TransactionType,
        stockUseCase: StockUseCase,
    ): String {
        val dataElementUid =
            when (transactionType) {
                TransactionType.DISTRIBUTION -> stockUseCase.stockDistribution()
                TransactionType.CORRECTION -> stockUseCase.stockCount()
                TransactionType.DISCARD -> stockUseCase.stockDiscarded()
            }

        return dataElementUid
    }
}
