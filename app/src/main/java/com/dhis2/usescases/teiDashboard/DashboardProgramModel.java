package com.dhis2.usescases.teiDashboard;

import android.databinding.BaseObservable;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

/**
 * Created by ppajuelo on 04/12/2017.
 *
 */

public class DashboardProgramModel extends BaseObservable {

    private TrackedEntityInstanceModel tei;
    private List<ProgramTrackedEntityAttributeModel> trackedEntityAttributesModel;
    private List<TrackedEntityAttributeValueModel> trackedEntityAttributeValues;
    private List<EventModel> eventModels;
    private List<RelationshipModel> relationships;
    private EnrollmentModel currentEnrollment;
    private List<ProgramStageModel> programStages;
    private List<ProgramModel> enrollmentProgramModels;
    private OrganisationUnitModel orgnUnit;


    public DashboardProgramModel(
            TrackedEntityInstanceModel tei,
            EnrollmentModel currentEnrollment,
            List<ProgramStageModel> programStages,
            List<EventModel> events,
            List<ProgramTrackedEntityAttributeModel> trackedEntityAttributeModels,
            List<TrackedEntityAttributeValueModel> trackedEntityAttributeValues,
            OrganisationUnitModel orgnUnit,
            List<ProgramModel> enrollmentProgramModels,
            List<RelationshipModel> relationships) {

        this.currentEnrollment = currentEnrollment;
        this.programStages = programStages;
        this.orgnUnit = orgnUnit;
        this.enrollmentProgramModels = enrollmentProgramModels;
        this.tei = tei;
        this.eventModels = events;
        this.trackedEntityAttributesModel = trackedEntityAttributeModels;
        this.trackedEntityAttributeValues = trackedEntityAttributeValues;
        this.relationships = relationships;
    }

    public DashboardProgramModel(TrackedEntityInstanceModel tei,
                                 List<ProgramTrackedEntityAttributeModel> trackedEntityAttributeModels,
                                 List<TrackedEntityAttributeValueModel> trackedEntityAttributeValues,
                                 OrganisationUnitModel orgnUnit,
                                 List<ProgramModel> enrollmentProgramModels) {
        this.tei = tei;
        this.trackedEntityAttributesModel = trackedEntityAttributeModels;
        this.trackedEntityAttributeValues = trackedEntityAttributeValues;
        this.orgnUnit = orgnUnit;
        this.enrollmentProgramModels = enrollmentProgramModels;
    }

    public TrackedEntityInstanceModel getTei() {
        return tei;
    }

    public EnrollmentModel getCurrentEnrollment() {
        return currentEnrollment;
    }

    public List<ProgramStageModel> getProgramStages() {
        return programStages;
    }

    public OrganisationUnitModel getOrgUnit() {
        return orgnUnit;
    }


    public String getAttributeBySortOrder(int sortOrder) {
        TrackedEntityAttributeValueModel attributeValue = null;
        sortOrder--;
        if (sortOrder < trackedEntityAttributesModel.size())
            for (TrackedEntityAttributeValueModel attribute : trackedEntityAttributeValues)
                if (trackedEntityAttributesModel != null && attribute.trackedEntityAttribute().equals(trackedEntityAttributesModel.get(sortOrder).trackedEntityAttribute()))
                    attributeValue = attribute;


        return attributeValue != null ? attributeValue.value() : "ERROR";
    }

    public List<ProgramModel> getEnrollmentProgramModels() {
        return enrollmentProgramModels;
    }

    public List<EventModel> getEvents() {
        return eventModels;
    }

    public List<RelationshipModel> getRelationships() {
        return relationships;
    }

    public ProgramModel getCurrentProgram() {
        ProgramModel selectedProgram = null;
        for (ProgramModel programModel : enrollmentProgramModels)
            if (programModel.uid().equals(currentEnrollment.program()))
                selectedProgram = programModel;
        return selectedProgram;
    }

    public List<TrackedEntityAttributeValueModel> getTrackedEntityAttributeValues() {
        return trackedEntityAttributeValues;
    }

    public List<ProgramTrackedEntityAttributeModel> getTrackedEntityAttributesModel() {
        return trackedEntityAttributesModel;
    }
}
