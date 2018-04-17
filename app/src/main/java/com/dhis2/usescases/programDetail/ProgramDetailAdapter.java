package com.dhis2.usescases.programDetail;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemProgramTrackedEntityBinding;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by frodriguez on 11/2/2017.
 */

public class ProgramDetailAdapter extends RecyclerView.Adapter<ProgramDetailViewHolder> {
    private final ProgramRepository programRepository;
    private ProgramDetailContractModule.Presenter presenter;
    private List<TrackedEntityInstanceModel> trackedEntityInstances;
    private String program;

    ProgramDetailAdapter(ProgramDetailContractModule.Presenter presenter, ProgramRepository programRepository) {
        this.presenter = presenter;
        this.programRepository = programRepository;
        this.trackedEntityInstances = new ArrayList<>();
    }

    @Override
    public ProgramDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProgramTrackedEntityBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_program_tracked_entity, parent, false);
        return new ProgramDetailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ProgramDetailViewHolder holder, int position) {

        holder.bind(presenter, program, trackedEntityInstances.get(position), programRepository);
    }

    @Override
    public int getItemCount() {
        return trackedEntityInstances != null ? trackedEntityInstances.size() : 0;
    }

    public void addItems(List<TrackedEntityInstanceModel> trackedEntityInstances) {
        this.trackedEntityInstances.clear();
        this.trackedEntityInstances.addAll(trackedEntityInstances);
        notifyDataSetChanged();
    }

    public void setProgram(String program) {
        this.program = program;
    }


}
