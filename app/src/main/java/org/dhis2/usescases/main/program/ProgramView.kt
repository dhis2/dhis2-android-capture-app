package org.dhis2.usescases.main.program

import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.usescases.general.AbstractActivityContracts

interface ProgramView : AbstractActivityContracts.View {

    fun navigateTo(program: ProgramUiModel)

    fun navigateToStockManagement(config: AppConfig)

    fun showSyncDialog(program: ProgramUiModel)
}
