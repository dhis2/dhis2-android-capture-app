package org.dhis2.usescases.main.program

import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.usescases.general.AbstractActivityContracts

interface ProgramView : AbstractActivityContracts.View {

    fun swapProgramModelData(programs: List<ProgramViewModel>)

    fun showFilterProgress()

    fun openOrgUnitTreeSelector()

    fun showHideFilter()

    fun clearFilters()

    fun navigateTo(program: ProgramViewModel)

    fun navigateToStockManagement(config: AppConfig)

    fun showSyncDialog(program: ProgramViewModel)
}
