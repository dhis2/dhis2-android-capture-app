package org.dhis2.usescases.teiDashboard.teiProgramList.ui

import androidx.compose.ui.platform.ComposeView
import androidx.databinding.BindingAdapter
import org.dhis2.usescases.main.program.ProgramUiModel
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListContract
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

@BindingAdapter(value = ["setProgramModel", "setPresenter"])
fun ComposeView.setProgramModel(
    program: ProgramUiModel,
    presenter: TeiProgramListContract.Presenter,
) {
    setContent {
        DHIS2Theme {
            EnrollToProgram(program) {
                presenter.onEnrollClick(program)
            }
        }
    }
}
