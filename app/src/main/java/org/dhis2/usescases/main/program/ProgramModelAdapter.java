package org.dhis2.usescases.main.program;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemProgramModelBinding;
import org.dhis2.utils.Period;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

public class ProgramModelAdapter extends RecyclerView.Adapter<ProgramModelHolder> {
    private final ProgramContract.Presenter presenter;
    private final List<ProgramViewModel> programList;

    ProgramModelAdapter(ProgramContract.Presenter presenter) {
        this.presenter = presenter;
        this.programList = new ArrayList<>();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ProgramModelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProgramModelBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_program_model, parent, false);
        return new ProgramModelHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramModelHolder holder, int position) {

        holder.bind(presenter, programList.get(holder.getAdapterPosition()));
    }


    @Override
    public long getItemId(int position) {
        return programList.get(position).id().hashCode();
    }

    @Override
    public int getItemCount() {
        return programList.size();
    }

    public void setData(List<ProgramViewModel> data) {
        this.programList.clear();
        this.programList.addAll(data);
        notifyDataSetChanged();
    }
}
