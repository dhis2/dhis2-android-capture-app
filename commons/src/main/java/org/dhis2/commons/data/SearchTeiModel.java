package org.dhis2.commons.data;

import androidx.compose.ui.graphics.Color;

import org.dhis2.commons.data.tuples.Trio;
import org.dhis2.ui.MetadataIconData;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.maintenance.D2ErrorCode;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.mobile.ui.designsystem.component.internal.ImageCardData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class SearchTeiModel implements CarouselItemModel {

    private LinkedHashMap<String, TrackedEntityAttributeValue> attributeValues;
    private LinkedHashMap<String, TrackedEntityAttributeValue> textAttributeValues;

    private List<Trio<String, String, String>> enrollmentsInfo;
    private List<Program> programInfo;
    private HashMap<String, MetadataIconData> metadataIconDataMap;
    private boolean hasOverdue;
    private boolean isOnline;

    private TrackedEntityInstance tei;
    private String profilePicturePath;
    private String defaultTypeIcon;
    private String header;

    private Enrollment selectedEnrollment;
    private List<Enrollment> enrollments;
    private Date overdueDate;
    private List<RelationshipViewModel> relationships;
    private boolean openedAttributeList = false;
    private String sortingKey;
    private String sortingValue;
    private String teTypeName;
    private String enrolledOrgUnit;

    private Boolean displayOrgUnit;
    private boolean showNavigationButton = false;
    @Nullable
    public String onlineErrorMessage;
    @Nullable
    public D2ErrorCode onlineErrorCode;

    public SearchTeiModel() {
        this.tei = null;
        this.selectedEnrollment = null;
        this.attributeValues = new LinkedHashMap<>();
        this.textAttributeValues = new LinkedHashMap<>();
        this.enrollmentsInfo = new ArrayList<>();
        this.programInfo = new ArrayList<>();
        this.isOnline = true;
        this.enrollments = new ArrayList<>();
        this.relationships = new ArrayList<>();
        this.sortingKey = null;
        this.sortingValue = null;
        this.enrolledOrgUnit = null;
        this.onlineErrorMessage = null;
        this.metadataIconDataMap = new HashMap<>();
    }

    public void addEnrollmentInfo(Trio<String, String, String> enrollmentInfo) {
        enrollmentsInfo.add(enrollmentInfo);
    }

    public void addProgramInfo(Program program, MetadataIconData metadataIconData) {
        if (!programInfo.contains(program)) {
            programInfo.add(program);
            metadataIconDataMap.put(program.uid(), metadataIconData);
        }
    }

    public boolean isHasOverdue() {
        return hasOverdue;
    }

    public void setHasOverdue(boolean hasOverdue) {
        this.hasOverdue = hasOverdue;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
        this.attributeValues.clear();
    }

    public LinkedHashMap<String, TrackedEntityAttributeValue> getAttributeValues() {
        return attributeValues;
    }

    public LinkedHashMap<String, TrackedEntityAttributeValue> getTextAttributeValues() {
        return textAttributeValues;
    }

    public void addAttributeValue(String attributeName, TrackedEntityAttributeValue attributeValues) {
        this.attributeValues.put(attributeName, attributeValues);
    }


    public void addTextAttribute(String attributeName, TrackedEntityAttributeValue attributeValue) {
        this.textAttributeValues.put(attributeName, attributeValue);
    }

    public void resetEnrollments() {
        this.enrollments.clear();
        this.enrollmentsInfo.clear();
    }

    public void setAttributeValues(LinkedHashMap<String, TrackedEntityAttributeValue> attributeValues) {
        this.attributeValues = attributeValues;
    }


    public void setTei(TrackedEntityInstance tei) {
        this.tei = tei;
    }

    public TrackedEntityInstance getTei() {
        return tei;
    }

    public void setProfilePicture(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getProfilePicturePath() {
        return profilePicturePath != null ? profilePicturePath : "";
    }

    public void setDefaultTypeIcon(String defaultTypeIcon) {
        this.defaultTypeIcon = defaultTypeIcon;
    }

    @Nullable
    public String getDefaultTypeIcon() {
        return defaultTypeIcon;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @Nullable
    public String getHeader() {
        return header;
    }

    public void setCurrentEnrollment(Enrollment enrollment) {
        this.selectedEnrollment = enrollment;
    }

    public Enrollment getSelectedEnrollment() {
        return this.selectedEnrollment;
    }

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public List<Program> getProgramInfo() {
        Collections.sort(programInfo, (program1, program2) -> program1.displayName().compareToIgnoreCase(program2.displayName()));
        if (selectedEnrollment != null) {
            List<Program> programs = new ArrayList<>();
            for (Program program : programInfo) {
                if (!Objects.equals(selectedEnrollment.program(), program.uid())) {
                    programs.add(program);
                }
            }
            return programs;
        } else {
            return programInfo;
        }
    }

    public MetadataIconData getMetadataIconData(String programUid) {
        MetadataIconData iconData = metadataIconDataMap.get(programUid);
        if (iconData != null) {
            return iconData;
        } else {
            return MetadataIconData.Companion.defaultIcon();
        }
    }

    public void setOverdueDate(Date dateToShow) {
        this.overdueDate = dateToShow;
    }

    public Date getOverdueDate() {
        return overdueDate;
    }

    public List<RelationshipViewModel> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<RelationshipViewModel> relationships) {
        this.relationships = relationships;
    }

    public void toggleAttributeList() {
        this.openedAttributeList = !this.openedAttributeList;
    }

    public boolean isAttributeListOpen() {
        return this.openedAttributeList;
    }

    public void setSortingValue(kotlin.Pair<String, String> sortingKeyValue) {
        if (sortingKeyValue != null) {
            this.sortingKey = sortingKeyValue.getFirst();
            this.sortingValue = sortingKeyValue.getSecond();
        }
    }

    public String getSortingKey() {
        return sortingKey;
    }

    public String getSortingValue() {
        return sortingValue;
    }

    public void setTEType(String teTypeName) {
        this.teTypeName = teTypeName;
    }

    public String getTeTypeName() {
        return teTypeName;
    }

    @NotNull
    @Override
    public String uid() {
        return tei.uid();
    }

    public void setEnrolledOrgUnit(String orgUnit) {
        enrolledOrgUnit = orgUnit;
    }

    public String getEnrolledOrgUnit() {
        return enrolledOrgUnit;
    }

    public void setDisplayOrgUnit(Boolean display) {
        displayOrgUnit = display;
    }

    public Boolean getDisplayOrgUnit() {
        return displayOrgUnit;
    }

    public void setShowNavigationButton(boolean showNavigationButton) {
        this.showNavigationButton = showNavigationButton;
    }

    public boolean shouldShowNavigationButton() {
        return showNavigationButton;
    }
}
