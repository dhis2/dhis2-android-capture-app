package com.dhis2.usescases.programDetail;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemProgramTrackedEntityBinding;
import com.dhis2.usescases.main.program.HomeViewModel;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by frodriguez on 11/2/2017.
 */

public class ProgramDetailAdapter extends RecyclerView.Adapter<ProgramDetailViewHolder>{

    private ProgramDetailPresenter presenter;
    private List<TrackedEntityInstance> trackedEntityInstances;
    private HomeViewModel program;

    @Inject
    public ProgramDetailAdapter(ProgramDetailPresenter presenter) {
        this.presenter = presenter;
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
        holder.bind(presenter, program, trackedEntityInstances.get(position));
    }

    @Override
    public int getItemCount() {
        return trackedEntityInstances != null ? trackedEntityInstances.size() : 0;
    }

    public void addItems(List<TrackedEntityInstance> trackedEntityInstances){
        if(trackedEntityInstances.size() > 0){
            this.trackedEntityInstances.addAll(trackedEntityInstances);
        }
    }

    public void setProgram(HomeViewModel program){
        this.program = program;
    }
}
