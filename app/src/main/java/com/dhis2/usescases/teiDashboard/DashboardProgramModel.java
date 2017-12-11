package com.dhis2.usescases.teiDashboard;

import android.databinding.BaseObservable;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

import java.util.List;

/**
 * Created by ppajuelo on 04/12/2017.
 */

public class DashboardProgramModel extends BaseObservable {

    private List<ProgramTrackedEntityAttributeModel> trackedEntityAttributesModel;
    private ProgramModel program;
    private RelationshipTypeModel relationshipTypeModel;
    private List<ProgramStageModel> programStages;
    private List<ProgramModel> enrollmentProgramModels;
    private OrganisationUnitModel orgnUnit;


    public DashboardProgramModel(ProgramModel programModel, List<ProgramStageModel> programStages,
                                 List<ProgramTrackedEntityAttributeModel> trackedEntityAttributeModels,
                                 OrganisationUnitModel orgnUnit,
                                 List<ProgramModel> enrollmentProgramModels,
                                 RelationshipTypeModel relationshipTypeModel) {
        this.program = programModel;
        this.programStages = programStages;
        this.trackedEntityAttributesModel = trackedEntityAttributeModels;
        this.orgnUnit = orgnUnit;
        this.relationshipTypeModel = relationshipTypeModel;
        this.enrollmentProgramModels = enrollmentProgramModels;
    }

    public DashboardProgramModel(ProgramModel programModel, List<ProgramStageModel> programStages,
                                 List<ProgramTrackedEntityAttributeModel> trackedEntityAttributeModels,
                                 List<ProgramModel> enrollmentProgramModels,
                                 RelationshipTypeModel relationshipTypeModel) {
        this.program = programModel;
        this.programStages = programStages;
        this.trackedEntityAttributesModel = trackedEntityAttributeModels;
        this.relationshipTypeModel = relationshipTypeModel;
        this.enrollmentProgramModels = enrollmentProgramModels;
    }

    public DashboardProgramModel(OrganisationUnitModel orgnUnit, List<ProgramModel> enrollmentProgramModels) {
        this.orgnUnit = orgnUnit;
        this.enrollmentProgramModels = enrollmentProgramModels;
    }

    public ProgramModel getProgram() {
        return program;
    }

    public List<ProgramStageModel> getProgramStages() {
        return programStages;
    }

    public OrganisationUnitModel getOrgUnit() {
        return orgnUnit;
    }

    public RelationshipTypeModel getRelationshipTypeModel() {
        return relationshipTypeModel;
    }

    public String getAttributeBySortOrder(List<TrackedEntityAttributeValue> attributeValues, int sortOrder) {
        TrackedEntityAttributeValue attributeValue = null;
        sortOrder--;

        for (TrackedEntityAttributeValue attribute : attributeValues)
            if (trackedEntityAttributesModel!=null && attribute.trackedEntityAttribute().equals(trackedEntityAttributesModel.get(sortOrder).trackedEntityAttribute()))
                attributeValue = attribute;


        return attributeValue != null ? attributeValue.value() : "ERROR";
    }

    public List<ProgramModel> getEnrollmentProgramModels() {
        return enrollmentProgramModels;
    }
}
