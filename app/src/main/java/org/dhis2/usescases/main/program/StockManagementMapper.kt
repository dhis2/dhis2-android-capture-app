package org.dhis2.usescases.main.program

import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.exceptions.InitializationException
import org.hisp.dhis.android.core.programtheme.stock.StockThemeTransaction
import org.hisp.dhis.android.core.programtheme.stock.StockThemeTransaction.Companion.TransactionType.CORRECTED
import org.hisp.dhis.android.core.programtheme.stock.StockThemeTransaction.Companion.TransactionType.DISCARDED
import org.hisp.dhis.android.core.programtheme.stock.StockThemeTransaction.Companion.TransactionType.DISTRIBUTED

internal class StockManagementMapper(
    val repository: ProgramThemeRepository
) {

    fun map(program: ProgramViewModel): AppConfig {
        val stockTheme = repository.getStockTheme(program.uid)
            ?: throw InitializationException(
                "Not possible to retrieve the Stock info from the server for uid:${program.uid}"
            )

        return AppConfig(
            stockTheme.programUid,
            stockTheme.itemCode,
            stockTheme.itemDescription,
            stockTheme.stockOnHand,
            (stockTheme.transactions.get(0) as StockThemeTransaction.Distributed).distributedTo,
            (
                stockTheme.transactions.find {
                    it.transactionType == DISTRIBUTED
                } as StockThemeTransaction.Distributed
                ).stockDistributed,
            (
                stockTheme.transactions.find {
                    it.transactionType == CORRECTED
                } as StockThemeTransaction.Correction
                ).stockCorrected,
            (
                stockTheme.transactions.find {
                    it.transactionType == DISCARDED
                } as StockThemeTransaction.Discarded
                ).stockDiscarded
        )
    }
}
