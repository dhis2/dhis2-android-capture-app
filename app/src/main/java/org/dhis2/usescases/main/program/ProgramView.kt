package org.dhis2.usescases.main.program

import org.dhis2.usescases.general.AbstractActivityContracts

interface ProgramView : AbstractActivityContracts.View {
    fun navigateTo(program: ProgramUiModel)

    fun showSyncDialog(program: ProgramUiModel)
}
