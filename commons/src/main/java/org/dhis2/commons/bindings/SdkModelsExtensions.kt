package org.dhis2.commons.bindings

import org.hisp.dhis.android.core.usecase.stock.StockUseCase
import org.hisp.dhis.android.core.usecase.stock.StockUseCaseTransaction

fun StockUseCase.distributedTo() = (
    transactions[0] as StockUseCaseTransaction.Distributed
    ).distributedTo

fun StockUseCase.stockDistribution() = (
    transactions.find {
        it.transactionType == StockUseCaseTransaction.Companion.TransactionType.DISTRIBUTED
    } as StockUseCaseTransaction.Distributed
    ).stockDistributed

fun StockUseCase.stockCount() = (
    transactions.find {
        it.transactionType == StockUseCaseTransaction.Companion.TransactionType.CORRECTED
    } as StockUseCaseTransaction.Correction
    ).stockCount

fun StockUseCase.stockDiscarded() = (
    transactions.find {
        it.transactionType == StockUseCaseTransaction.Companion.TransactionType.DISCARDED
    } as StockUseCaseTransaction.Discarded
    ).stockDiscarded
