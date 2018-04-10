package com.dhis2.usescases.programDetail;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemProgramTrackedEntityBinding;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by frodriguez on 11/2/2017.
 *
 */

public class ProgramDetailAdapter extends RecyclerView.Adapter<ProgramDetailViewHolder> {
    private ProgramDetailContractModule.Presenter presenter;
    private List<MyTrackedEntityInstance> trackedEntityInstances;
    private List<ProgramTrackedEntityAttributeModel> attributesToShow;
    private ProgramModel program;
    private List<OrganisationUnitModel> orgUnits;

    ProgramDetailAdapter(ProgramDetailContractModule.Presenter presenter) {
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

        MyTrackedEntityInstance entityInstance = trackedEntityInstances.get(position);

        ArrayList<String> attributes = new ArrayList<>();
        for (ProgramTrackedEntityAttributeModel programAttributes : attributesToShow) {
            for (TrackedEntityAttributeValueModel value : entityInstance.getTrackedEntityAttributeValues()) {
                if (value.trackedEntityAttribute().equals(programAttributes.trackedEntityAttribute()))
                    attributes.add(value.value());
            }
        }

        String orgUnit = "";
        for (OrganisationUnitModel orgUnitModel : orgUnits) {
            if (orgUnitModel.uid().equals(entityInstance.getTrackedEntityInstance().organisationUnit()))
                orgUnit = orgUnitModel.displayShortName();
        }

        String stage = "";
        boolean followUp = false;
        if (entityInstance.getEnrollments() != null) {
            for (EnrollmentModel enrollment : entityInstance.getEnrollments()) {
                if (enrollment.followUp())
                    followUp = true;
            }
        }
        if (entityInstance.getEventModels() != null) {
            List<EventModel> events = new ArrayList<>(entityInstance.getEventModels());
            Collections.sort(events, (event, t1) -> event.dueDate().compareTo(t1.dueDate()));
            stage = events.get(0).programStage();
        }

        holder.bind(presenter, program, orgUnit, attributes, stage, entityInstance.getTrackedEntityInstance().uid(), followUp);
    }

    @Override
    public int getItemCount() {
        return trackedEntityInstances != null ? trackedEntityInstances.size() : 0;
    }

    public void addItems(List<MyTrackedEntityInstance> trackedEntityInstances) {
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

    public void setProgram(ProgramModel program) {
        this.program = program;
    }

    public void setOrgUnits(List<OrganisationUnitModel> orgUnits) {
        this.orgUnits = orgUnits;
        notifyDataSetChanged();
    }
}
