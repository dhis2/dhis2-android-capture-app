package org.dhis2.usescases.teiDashboard.teiProgramList

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.BR
import org.dhis2.usescases.main.program.ProgramViewModel

class TeiProgramListEnrollmentViewHolder(
    private val binding: ViewDataBinding,
    composeView: ComposeView?,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        composeView?.setViewCompositionStrategy(
            ViewCompositionStrategy
                .DisposeOnViewTreeLifecycleDestroyed,
        )
    }

    fun bind(
        presenter: TeiProgramListContract.Presenter,
        enrollment: EnrollmentViewModel?,
        programModel: ProgramViewModel?,
    ) {
        binding.setVariable(BR.enrollment, enrollment)
        binding.setVariable(BR.program, programModel)
        binding.setVariable(BR.presenter, presenter)
        binding.setVariable(BR.metadataIconData, enrollment?.metadataIconData())
        binding.executePendingBindings()
    }
}
