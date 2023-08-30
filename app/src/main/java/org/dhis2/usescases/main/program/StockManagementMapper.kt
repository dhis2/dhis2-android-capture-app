package org.dhis2.usescases.main.program

import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.commons.bindings.distributedTo
import org.dhis2.commons.bindings.stockCount
import org.dhis2.commons.bindings.stockDiscarded
import org.dhis2.commons.bindings.stockDistribution
import org.hisp.dhis.android.core.usecase.stock.StockUseCase

fun StockUseCase.toAppConfig() = AppConfig(
    program = this.programUid,
    itemCode = this.itemCode,
    itemName = this.itemDescription,
    stockOnHand = this.stockOnHand,
    distributedTo = this.distributedTo(),
    stockDistribution = this.stockDistribution(),
    stockCount = this.stockCount(),
    stockDiscarded = this.stockDiscarded(),
)
