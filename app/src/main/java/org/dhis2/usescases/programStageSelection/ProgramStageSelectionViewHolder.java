package org.dhis2.usescases.programStageSelection;

import android.support.v7.widget.RecyclerView;

import com.android.databinding.library.baseAdapters.BR;
import org.dhis2.Bindings.Bindings;
import org.dhis2.databinding.ItemProgramStageBinding;

import org.hisp.dhis.android.core.program.ProgramStageModel;

/**
 * Created by Cristian on 13/02/2018.
 */

public class ProgramStageSelectionViewHolder extends RecyclerView.ViewHolder {

    private ItemProgramStageBinding binding;

    ProgramStageSelectionViewHolder(ItemProgramStageBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramStageSelectionContract.Presenter presenter, ProgramStageModel programStage) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.programStage, programStage);
        binding.executePendingBindings();
        Bindings.setObjectStyleAndTint(binding.programStageIcon, binding.programStageIcon, programStage.uid());
        itemView.setOnClickListener(view -> {
            if (programStage.accessDataWrite())
                presenter.onProgramStageClick(programStage);
            else
                presenter.displayMessage(null);
        });
    }
}
