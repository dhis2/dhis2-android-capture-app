package org.dhis2.usescases.main.program

import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.usecase.stock.StockUseCase

interface ProgramView : AbstractActivityContracts.View {

    fun navigateTo(program: ProgramUiModel)

    fun navigateToStockManagement(stockUseCase: StockUseCase)

    fun showSyncDialog(program: ProgramUiModel)
}
