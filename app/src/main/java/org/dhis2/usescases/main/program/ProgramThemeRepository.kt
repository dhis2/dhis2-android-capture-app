package org.dhis2.usescases.main.program

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.usecase.stock.StockUseCase

internal class ProgramThemeRepository(
    val d2: D2
) {

    fun isStockTheme(programUid: String): Boolean {
        return try {
            d2.useCaseModule()
                .stockUseCases()
                .uid(programUid)
                .blockingExists()
        } catch (e: NullPointerException) {
            false
        }
    }

    fun getStockTheme(programUid: String): StockUseCase? {
        return try {
            d2.useCaseModule()
                .stockUseCases()
                .withTransactions()
                .uid(programUid)
                .blockingGet()
        } catch (e: Exception) {
            null
        }
    }
}
