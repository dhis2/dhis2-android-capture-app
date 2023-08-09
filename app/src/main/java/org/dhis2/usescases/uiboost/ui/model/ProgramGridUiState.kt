package org.dhis2.usescases.uiboost.ui.model

import org.dhis2.usescases.main.program.ProgramViewModel

data class ProgramGridUiState(
    var programsListGridUiState: List<ProgramViewModel> = emptyList()
)
