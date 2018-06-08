package com.dhis2.usescases.main.program;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemProgramBinding;
import com.dhis2.utils.Period;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 18/10/2017.
 */

public class ProgramAdapter extends RecyclerView.Adapter<ProgramViewHolder> {

    private List<ProgramModel> programList;
    private ProgramContract.Presenter presenter;
    private Period currentPeriod;

    ProgramAdapter(ProgramContract.Presenter presenter, Period currentPeriod) {
        this.presenter = presenter;
        this.programList = new ArrayList<>();
        this.currentPeriod = currentPeriod;
        setHasStableIds(true);
    }

    public Period getCurrentPeriod() {
        return currentPeriod;
    }

    public void setCurrentPeriod(Period currentPeriod) {
        this.currentPeriod = currentPeriod;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProgramBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.item_program, parent, false);
        return new ProgramViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        holder.bind(presenter, getItemAt(holder.getAdapterPosition()), currentPeriod);
    }

    public void setData(List<ProgramModel> program) {

        programList = new ArrayList<>();
        programList.addAll(program);

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return programList.get(position).uid().hashCode();
    }

    private ProgramModel getItemAt(int position) {
        return programList.get(position);
    }

    @Override
    public int getItemCount() {
        return programList != null ? programList.size() : 0;
    }
}
