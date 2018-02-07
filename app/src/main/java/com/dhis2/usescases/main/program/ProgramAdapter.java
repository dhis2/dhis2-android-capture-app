package com.dhis2.usescases.main.program;

import android.databinding.DataBindingUtil;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemProgramBinding;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ppajuelo on 18/10/2017.
 */

public class ProgramAdapter extends RecyclerView.Adapter<ProgramViewHolder> {

    private List<ProgramModel> programList;
    private ProgramContract.Presenter presenter;

    public ProgramAdapter(ProgramContract.Presenter presenter) {
        this.presenter = presenter;
        this.programList = new ArrayList<>();
    }

    @Override
    public ProgramViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProgramBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.item_program, parent, false);
        return new ProgramViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(ProgramViewHolder holder, int position) {
        holder.bind(presenter, getItemAt(position));

    }

    public void setData(List<ProgramModel> program) {
//        this.programList.clear();
//        this.programList.addAll(program);

        Collections.sort(this.programList, (ob1, ob2) -> ob2.lastUpdated().compareTo(ob1.lastUpdated()));
        Collections.sort(program, (ob1, ob2) -> ob2.lastUpdated().compareTo(ob1.lastUpdated()));
//        notifyDataSetChanged();

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ProgramDiffCallback(programList, program));
        programList.clear();
        programList.addAll(program);
        diffResult.dispatchUpdatesTo(this);
    }

    private ProgramModel getItemAt(int position) {
        return programList.get(position);
    }

    @Override
    public int getItemCount() {
        return programList != null ? programList.size() : 0;
    }
}
