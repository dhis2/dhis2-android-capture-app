package org.dhis2.usescases.teiDashboard.teiProgramList.ui

import androidx.compose.ui.platform.ComposeView
import androidx.databinding.BindingAdapter
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.usescases.main.program.ProgramViewModel
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListContract

@BindingAdapter(value = ["setProgramModel", "setPresenter"])
fun ComposeView.setProgramModel(
    program: ProgramViewModel,
    presenter: TeiProgramListContract.Presenter
) {
    setContent {
        MdcTheme {
            EnrollToProgram(program) {
                presenter.onEnrollClick(program)
            }
        }
    }
}
