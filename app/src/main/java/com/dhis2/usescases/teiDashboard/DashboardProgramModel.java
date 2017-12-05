package com.dhis2.usescases.teiDashboard;

import android.databinding.BaseObservable;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

import java.util.List;

/**
 * Created by ppajuelo on 04/12/2017.
 */

public class DashboardProgramModel extends BaseObservable {

    private final List<ProgramTrackedEntityAttributeModel> trackedEntityAttributesModel;
    private ProgramModel program;
    private List<ProgramStageModel> programStages;


    public DashboardProgramModel(ProgramModel programModel, List<ProgramStageModel> programStages, List<ProgramTrackedEntityAttributeModel> trackedEntityAttributeModels) {
        this.program = programModel;
        this.programStages = programStages;
        this.trackedEntityAttributesModel = trackedEntityAttributeModels;
    }

    public ProgramModel getProgram() {
        return program;
    }

    public List<ProgramStageModel> getProgramStages() {
        return programStages;
    }

    public String getAttributeBySortOrder(List<TrackedEntityAttributeValue> attributeValues, int sortOrder) {
        TrackedEntityAttributeValue attributeValue = null;
        sortOrder--;

        for (TrackedEntityAttributeValue attribute : attributeValues)
            if (attribute.trackedEntityAttribute().equals(trackedEntityAttributesModel.get(sortOrder).trackedEntityAttribute()))
                attributeValue = attribute;


        return attributeValue.value();
    }
}
