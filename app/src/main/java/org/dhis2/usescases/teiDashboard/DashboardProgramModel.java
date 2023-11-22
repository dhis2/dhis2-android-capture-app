package org.dhis2.usescases.teiDashboard;

import androidx.databinding.BaseObservable;

import org.dhis2.commons.data.tuples.Pair;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */
public class DashboardProgramModel extends BaseObservable {

    private TrackedEntityInstance tei;
    private List<Pair<TrackedEntityAttribute, TrackedEntityAttributeValue>> trackedEntityAttributes;
    private List<TrackedEntityAttributeValue> trackedEntityAttributeValues;
    private List<Event> eventModels;
    private Enrollment currentEnrollment;
    private List<ProgramStage> programStages;
    private List<Program> enrollmentPrograms;
    private List<OrganisationUnit> orgsUnits;
    private List<Enrollment> teiEnrollments;

    private String teiHeader;
    private String avatarPath;

    private EnrollmentStatus currentEnrollmentStatus;
    private State enrollmentState;


    public DashboardProgramModel(
            TrackedEntityInstance tei,
            Enrollment currentEnrollment,
            List<ProgramStage> programStages,
            List<Event> events,
            List<Pair<TrackedEntityAttribute, TrackedEntityAttributeValue>> trackedEntityAttributes,
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
        this.currentEnrollmentStatus = currentEnrollment.status();
        this.enrollmentState = currentEnrollment.aggregatedSyncState();
    }

    public DashboardProgramModel(TrackedEntityInstance tei,
                                 List<TrackedEntityAttributeValue> trackedEntityAttributeValues,
                                 List<OrganisationUnit> orgsUnits,
                                 List<Program> enrollmentPrograms,
                                 List<Enrollment> teiEnrollments) {
        this.tei = tei;
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

    public List<Pair<TrackedEntityAttribute, TrackedEntityAttributeValue>> getAttributes() {
        return trackedEntityAttributes;
    }

    public List<Program> getProgramsWithActiveEnrollment() {
        Collections.sort(enrollmentPrograms, (program1, program2) -> program1.displayName().compareToIgnoreCase(program2.displayName()));
        List<Program> listWithActiveEnrollments = new ArrayList<>();
        for (Program program : enrollmentPrograms) {
            if (getEnrollmentForProgram(program.uid()) != null) {
                listWithActiveEnrollments.add(program);
            }
        }
        return listWithActiveEnrollments;
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

    public List<TrackedEntityAttributeValue>  getTrackedEntityAttributeValues() {
        return trackedEntityAttributeValues;
    }

    public Enrollment getEnrollmentForProgram(String uid) {
        for (Enrollment enrollment : teiEnrollments)
            if (Objects.equals(enrollment.program(), uid) && enrollment.status() == EnrollmentStatus.ACTIVE)
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

    public void setTeiHeader(@Nullable String header) {
        teiHeader = header;
    }
    public String getTeiHeader() {
        return teiHeader;
    }

    public void setAvatarPath(String path) {
        avatarPath = path;
    }

    public String getAvatarPath() { return avatarPath; }

    public List<Program> getEnrollmentActivePrograms(){
        Collections.sort(enrollmentPrograms, (program1, program2) -> program1.displayName().compareToIgnoreCase(program2.displayName()));
        List<Program> programs = new ArrayList<>();
        for(Program program: enrollmentPrograms) {
            if (!Objects.equals(currentEnrollment.program(), program.uid())) {
                programs.add(program);
            }
        }
        return programs;
    }

    public void setCurrentEnrollmentStatus(EnrollmentStatus status){
        currentEnrollmentStatus = status;
    }

    public EnrollmentStatus getCurrentEnrollmentStatus() {
        return currentEnrollmentStatus;
    }

    public void setEnrollmentState(State state){
        enrollmentState = state;
    }

    public State getEnrollmentState() {
        return enrollmentState;
    }
}