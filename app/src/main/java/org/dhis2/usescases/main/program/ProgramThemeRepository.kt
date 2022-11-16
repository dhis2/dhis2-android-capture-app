package org.dhis2.usescases.main.program

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.programtheme.stock.StockTheme

internal class ProgramThemeRepository(
    val d2: D2
) {

    fun isStockTheme(programUid: String): Boolean {
        return try {
            d2.programThemeModule()
                .stockThemes()
                .uid(programUid)
                .blockingExists()
        } catch (e: NullPointerException) {
            false
        }
    }

    fun getStockTheme(programUid: String): StockTheme? {
        return try {
            d2.programThemeModule()
                .stockThemes()
                .withTransactions()
                .uid(programUid)
                .blockingGet()
        } catch (e: Exception) {
            null
        }
    }
}
