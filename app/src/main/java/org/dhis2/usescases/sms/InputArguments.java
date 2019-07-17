package org.dhis2.usescases.sms;

import android.os.Bundle;

public class InputArguments {
    private static String ARG_TRACKER_EVENT = "tracker_event";
    private static String ARG_SIMPLE_EVENT = "simple_event";
    private static String ARG_ENROLLMENT = "enrollment";
    private static String ARG_ORG_UNIT = "org_unit";
    private static String ARG_PERIOD = "period";
    private static String ARG_ATTRIBUTE = "attribute";
    private static String ARG_DATA_SET = "dataset";
    private static String ARG_RELATIONSHIP = "relationship";
    private static String ARG_DELETION = "deletion";

    private String simpleEventId;
    private String trackerEventId;
    private String enrollmentId;
    private String orgUnit;
    private String period;
    private String attributeOptionCombo;
    private String dataSet;
    private String relationship;
    private String deletion;

    public InputArguments(Bundle extras) {
        if (extras == null) {
            return;
        }
        simpleEventId = extras.getString(ARG_SIMPLE_EVENT);
        trackerEventId = extras.getString(ARG_TRACKER_EVENT);
        enrollmentId = extras.getString(ARG_ENROLLMENT);
        orgUnit = extras.getString(ARG_ORG_UNIT);
        period = extras.getString(ARG_PERIOD);
        attributeOptionCombo = extras.getString(ARG_ATTRIBUTE);
        dataSet = extras.getString(ARG_DATA_SET);
        relationship = extras.getString(ARG_RELATIONSHIP);
        deletion = extras.getString(ARG_DELETION);
    }

    public static void setTrackerEventData(Bundle args, String eventId) {
        args.putString(ARG_TRACKER_EVENT, eventId);
    }

    public static void setSimpleEventData(Bundle args, String eventId) {
        args.putString(ARG_SIMPLE_EVENT, eventId);
    }

    public static void setEnrollmentData(Bundle args, String enrollmentId) {
        args.putString(ARG_ENROLLMENT, enrollmentId);
    }

    public static void setDataSet(Bundle args, String dataSet, String orgUnit, String period, String attributeOptionCombo) {
        args.putString(ARG_ORG_UNIT, orgUnit);
        args.putString(ARG_PERIOD, period);
        args.putString(ARG_ATTRIBUTE, attributeOptionCombo);
        args.putString(ARG_DATA_SET, dataSet);
    }

    public static void setRelationship(Bundle args, String relationship) {
        args.putString(ARG_RELATIONSHIP, relationship);
    }

    public static void setDeletion(Bundle args, String deletion) {
        args.putString(ARG_DELETION, deletion);
    }

    public String getSimpleEventId() {
        return simpleEventId;
    }

    public String getTrackerEventId() {
        return trackerEventId;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public String getOrgUnit() {
        return orgUnit;
    }

    public String getPeriod() {
        return period;
    }

    public String getAttributeOptionCombo() {
        return attributeOptionCombo;
    }

    public String getDataSet() {
        return dataSet;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getDeletion() {
        return deletion;
    }

    public Type getSubmissionType() {
        if (enrollmentId != null && enrollmentId.length() > 0) {
            return Type.ENROLLMENT;
        } else if (simpleEventId != null && simpleEventId.length() > 0) {
            return Type.SIMPLE_EVENT;
        } else if (trackerEventId != null && trackerEventId.length() > 0) {
            return Type.TRACKER_EVENT;
        } else if (orgUnit != null && orgUnit.length() > 0 &&
                period != null && period.length() > 0 &&
                attributeOptionCombo != null && attributeOptionCombo.length() > 0 &&
                dataSet != null && dataSet.length() > 0) {
            return Type.DATA_SET;
        } else if (relationship != null && relationship.length() > 0) {
            return Type.RELATIONSHIP;
        } else if (deletion != null && deletion.length() > 0) {
            return Type.DELETION;
        }
        return Type.WRONG_PARAMS;
    }

    public boolean isSameSubmission(InputArguments second) {
        if (second == null || getSubmissionType() != second.getSubmissionType()) {
            return false;
        }
        switch (getSubmissionType()) {
            case ENROLLMENT:
                return enrollmentId.equals(second.enrollmentId);
            case TRACKER_EVENT:
                return trackerEventId.equals(second.trackerEventId);
            case SIMPLE_EVENT:
                return simpleEventId.equals(second.simpleEventId);
            case DATA_SET:
                return dataSet.equals(second.dataSet) && period.equals(second.period);
            case DELETION:
                return deletion.equals(second.deletion);
            case RELATIONSHIP:
                return relationship.equals(second.relationship);
            case WRONG_PARAMS:
                return true;
        }
        return false;
    }

    public enum Type {
        ENROLLMENT, TRACKER_EVENT, SIMPLE_EVENT, DATA_SET, DELETION, RELATIONSHIP, WRONG_PARAMS
    }
}
