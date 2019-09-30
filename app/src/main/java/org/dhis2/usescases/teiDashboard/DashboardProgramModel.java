package org.dhis2.usescases.teiDashboard;

import androidx.databinding.BaseObservable;

import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */
public class DashboardProgramModel extends BaseObservable {

    private TrackedEntityInstance tei;
    private List<ProgramTrackedEntityAttribute> trackedEntityAttributes;
    private List<TrackedEntityAttributeValue> trackedEntityAttributeValues;
    private List<Event> eventModels;
    private Enrollment currentEnrollment;
    private List<ProgramStage> programStages;
    private List<Program> enrollmentPrograms;
    private List<OrganisationUnit> orgsUnits;
    private List<Enrollment> teiEnrollments;

    public DashboardProgramModel(
            TrackedEntityInstance tei,
            Enrollment currentEnrollment,
            List<ProgramStage> programStages,
            List<Event> events,
            List<ProgramTrackedEntityAttribute> trackedEntityAttributes,
            List<TrackedEntityAttributeValue> trackedEntityAttributeValues,
            List<OrganisationUnit> orgsUnits,
            List<Program> enrollmentPrograms) {

        this.currentEnrollment = currentEnrollment;
        this.programStages = programStages;
        this.orgsUnits = orgsUnits;
        this.enrollmentPrograms = enrollmentPrograms;
        this.tei = tei;
        this.eventModels = events;
        this.trackedEntityAttributes = trackedEntityAttributes;
        this.trackedEntityAttributeValues = trackedEntityAttributeValues;
    }

    public DashboardProgramModel(TrackedEntityInstance tei,
                                 List<ProgramTrackedEntityAttribute> trackedEntityAttributes,
                                 List<TrackedEntityAttributeValue> trackedEntityAttributeValues,
                                 List<OrganisationUnit> orgsUnits,
                                 List<Program> enrollmentPrograms,
                                 List<Enrollment> teiEnrollments) {
        this.tei = tei;
        this.trackedEntityAttributes = trackedEntityAttributes;
        this.trackedEntityAttributeValues = trackedEntityAttributeValues;
        this.orgsUnits = orgsUnits;
        this.enrollmentPrograms = enrollmentPrograms;
        this.teiEnrollments = teiEnrollments;
    }

    public TrackedEntityInstance getTei() {
        return tei;
    }

    public Enrollment getCurrentEnrollment() {
        return currentEnrollment;
    }

    public List<ProgramStage> getProgramStages() {
        return programStages;
    }

    public List<OrganisationUnit> getOrgUnits() {
        return orgsUnits;
    }

    public OrganisationUnit getCurrentOrgUnit() {
        OrganisationUnit currentOrgUnit = null;
        if (currentEnrollment != null)
            for (OrganisationUnit orgUnit : orgsUnits) {
                if (currentEnrollment.organisationUnit().equals(orgUnit.uid()))
                    currentOrgUnit = orgUnit;
            }
        return currentOrgUnit;
    }

    public String getAttributeBySortOrder(int sortOrder) {
        TrackedEntityAttributeValue attributeValue = null;
        sortOrder--;
        if (sortOrder < trackedEntityAttributes.size())
            for (TrackedEntityAttributeValue attribute : trackedEntityAttributeValues)
                if (trackedEntityAttributes != null &&
                        Objects.equals(attribute.trackedEntityAttribute(), trackedEntityAttributes.get(sortOrder).trackedEntityAttribute().uid()))
                    attributeValue = attribute;


        return attributeValue != null ? attributeValue.value() : "";
    }

    public List<Program> getEnrollmentPrograms() {
        Collections.sort(enrollmentPrograms, (program1, program2) -> program1.displayName().compareToIgnoreCase(program2.displayName()));
        return enrollmentPrograms;
    }

    public List<Event> getEvents() {
        return eventModels;
    }

    public Program getCurrentProgram() {
        Program selectedProgram = null;
        if (currentEnrollment != null)
            for (Program program : enrollmentPrograms)
                if (program.uid().equals(currentEnrollment.program()))
                    selectedProgram = program;
        return selectedProgram;
    }

    public List<TrackedEntityAttributeValue> getTrackedEntityAttributeValues() {
        return trackedEntityAttributeValues;
    }

    public Enrollment getEnrollmentForProgram(String uid) {
        for (Enrollment enrollment : teiEnrollments)
            if (Objects.equals(enrollment.program(), uid))
                return enrollment;
        return null;
    }

    public String getTrackedEntityAttributeValueBySortOrder(int sortOrder) {
        if (sortOrder <= trackedEntityAttributeValues.size()) {
            return trackedEntityAttributeValues.get(sortOrder - 1).value();
        }
        return "";
    }


    public ObjectStyle getObjectStyleForProgram(String programUid) {
        Program foundProgram = null;
        for (Program program : enrollmentPrograms) {
            if (programUid.equals(program.uid()))
                foundProgram = program;
        }
        if (foundProgram != null && foundProgram.style() != null)
            return foundProgram.style();
        else return null;
    }
}