package org.dhis2.usescases.main.program

import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.exceptions.InitializationException
import org.dhis2.commons.bindings.distributedTo
import org.dhis2.commons.bindings.stockCount
import org.dhis2.commons.bindings.stockDiscarded
import org.dhis2.commons.bindings.stockDistribution

internal class StockManagementMapper(
    val repository: ProgramThemeRepository
) {

    fun map(program: ProgramViewModel): AppConfig {
        val stockTheme = repository.getStockTheme(program.uid)
            ?: throw InitializationException(
                "Not possible to retrieve the Stock info from the server for uid:${program.uid}"
            )

        return AppConfig(
            program = stockTheme.programUid,
            itemCode = stockTheme.itemCode,
            itemName = stockTheme.itemDescription,
            stockOnHand = stockTheme.stockOnHand,
            distributedTo = stockTheme.distributedTo(),
            stockDistribution = stockTheme.stockDistribution(),
            stockCount = stockTheme.stockCount(),
            stockDiscarded = stockTheme.stockDiscarded()
        )
    }
}
