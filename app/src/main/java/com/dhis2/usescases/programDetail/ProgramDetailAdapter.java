package com.dhis2.usescases.programDetail;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemProgramTrackedEntityBinding;
import com.dhis2.usescases.main.program.HomeViewModel;

import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by frodriguez on 11/2/2017.
 */

public class ProgramDetailAdapter extends RecyclerView.Adapter<ProgramDetailViewHolder> {

    private ProgramDetailPresenter presenter;
    private List<TrackedEntityInstance> trackedEntityInstances;
    private List<ProgramTrackedEntityAttributeModel> attributesToShow;
    private HomeViewModel program;
    private List<OrganisationUnitModel> orgUnits;

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

        TrackedEntityInstance entityInstance = trackedEntityInstances.get(position);

        ArrayList<String> attributes = new ArrayList<>();
        for (ProgramTrackedEntityAttributeModel programAttributes : attributesToShow) {
            for (TrackedEntityAttributeValue value : entityInstance.trackedEntityAttributeValues()) {
                if (value.trackedEntityAttribute().equals(programAttributes.trackedEntityAttribute()))
                    attributes.add(value.value());
            }
        }

        String orgUnit = "";
        for (OrganisationUnitModel orgUnitModel : orgUnits) {
            if (orgUnitModel.uid().equals(entityInstance.organisationUnit()))
                orgUnit = orgUnitModel.displayShortName();
        }

        String stage = "";
        if (entityInstance.enrollments() != null)
            for (Enrollment enrollment : entityInstance.enrollments()) {
                if (enrollment.events() != null) {
                    List<Event> events = new ArrayList<>();
                    events.addAll(enrollment.events());
                    Collections.sort(events, (event, t1) -> event.dueDate().compareTo(t1.dueDate()));
                    stage = events.get(0).programStage();
                }

            }

        holder.bind(presenter, program, orgUnit, attributes, stage);
    }

    @Override
    public int getItemCount() {
        return trackedEntityInstances != null ? trackedEntityInstances.size() : 0;
    }

    public void addItems(List<TrackedEntityInstance> trackedEntityInstances) {
        if (trackedEntityInstances.size() > 0) {
            this.trackedEntityInstances.addAll(trackedEntityInstances);
        }
        if (attributesToShow != null)
            notifyDataSetChanged();
    }

    public void setAttributesToShow(List<ProgramTrackedEntityAttributeModel> attributesToShow) {
        this.attributesToShow = attributesToShow;
        notifyDataSetChanged();
    }

    public void setProgram(HomeViewModel program) {
        this.program = program;
    }

    public void setOrgUnits(List<OrganisationUnitModel> orgUnits) {
        this.orgUnits = orgUnits;
        notifyDataSetChanged();
    }
}
