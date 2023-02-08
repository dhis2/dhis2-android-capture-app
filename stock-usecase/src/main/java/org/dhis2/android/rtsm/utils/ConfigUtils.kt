package org.dhis2.android.rtsm.utils

import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.TransactionType

object ConfigUtils {

    @JvmStatic
    fun getTransactionDataElement(transactionType: TransactionType, config: AppConfig): String {
        val dataElementUid = when (transactionType) {
            TransactionType.DISTRIBUTION -> config.stockDistribution
            TransactionType.CORRECTION -> config.stockCount
            TransactionType.DISCARD -> config.stockDiscarded
        }

        return dataElementUid
    }
}
