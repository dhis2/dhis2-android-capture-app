package org.dhis2.usescases.main.program

import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.exceptions.InitializationException
import org.hisp.dhis.android.core.usecase.stock.StockUseCaseTransaction
import org.hisp.dhis.android.core.usecase.stock.StockUseCaseTransaction.Companion.TransactionType.CORRECTED
import org.hisp.dhis.android.core.usecase.stock.StockUseCaseTransaction.Companion.TransactionType.DISCARDED
import org.hisp.dhis.android.core.usecase.stock.StockUseCaseTransaction.Companion.TransactionType.DISTRIBUTED

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
            (stockTheme.transactions[0] as StockUseCaseTransaction.Distributed).distributedTo,
            (
                stockTheme.transactions.find {
                    it.transactionType == DISTRIBUTED
                } as StockUseCaseTransaction.Distributed
                ).stockDistributed,
            (
                stockTheme.transactions.find {
                    it.transactionType == CORRECTED
                } as StockUseCaseTransaction.Correction
                ).stockCorrected,
            (
                stockTheme.transactions.find {
                    it.transactionType == DISCARDED
                } as StockUseCaseTransaction.Discarded
                ).stockDiscarded
        )
    }
}
