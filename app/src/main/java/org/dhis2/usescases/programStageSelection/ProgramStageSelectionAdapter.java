package org.dhis2.usescases.programStageSelection;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemProgramStageBinding;

import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.List;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class ProgramStageSelectionAdapter extends RecyclerView.Adapter<ProgramStageSelectionViewHolder> {

    private ProgramStageSelectionContract.Presenter presenter;
    private List<ProgramStageModel> programStageModels;

    ProgramStageSelectionAdapter(@NonNull ProgramStageSelectionContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public void setProgramStageModels(List<ProgramStageModel> programStageModels) {
        this.programStageModels = programStageModels;
    }

    @Override
    public ProgramStageSelectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProgramStageBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_program_stage, parent, false);
        return new ProgramStageSelectionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ProgramStageSelectionViewHolder holder, int position) {
        ProgramStageModel programStageModel = programStageModels.get(position);
        holder.bind(presenter, programStageModel);
    }

    @Override
    public int getItemCount() {
        return programStageModels != null ? programStageModels.size() : 0;
    }
}
