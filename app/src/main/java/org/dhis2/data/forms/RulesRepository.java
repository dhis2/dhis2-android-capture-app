package org.dhis2.data.forms;

import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Quartet;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramRuleVariableModel;
import org.hisp.dhis.android.core.program.ProgramRuleVariableSourceType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.rules.models.Rule;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleActionCreateEvent;
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair;
import org.hisp.dhis.rules.models.RuleActionDisplayText;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionHideProgramStage;
import org.hisp.dhis.rules.models.RuleActionHideSection;
import org.hisp.dhis.rules.models.RuleActionSetMandatoryField;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionShowWarning;
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion;
import org.hisp.dhis.rules.models.RuleAttributeValue;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEnrollment;
import org.hisp.dhis.rules.models.RuleEvent;
import org.hisp.dhis.rules.models.RuleValueType;
import org.hisp.dhis.rules.models.RuleVariable;
import org.hisp.dhis.rules.models.RuleVariableAttribute;
import org.hisp.dhis.rules.models.RuleVariableCalculatedValue;
import org.hisp.dhis.rules.models.RuleVariableCurrentEvent;
import org.hisp.dhis.rules.models.RuleVariableNewestEvent;
import org.hisp.dhis.rules.models.RuleVariableNewestStageEvent;
import org.hisp.dhis.rules.models.RuleVariablePreviousEvent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.data.database.SqlConstants.AND;
import static org.dhis2.data.database.SqlConstants.COMMA;
import static org.dhis2.data.database.SqlConstants.DESC;
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.INNER_JOIN;
import static org.dhis2.data.database.SqlConstants.IS_NOT_NULL;
import static org.dhis2.data.database.SqlConstants.JOIN;
import static org.dhis2.data.database.SqlConstants.LESS_OR_EQUAL;
import static org.dhis2.data.database.SqlConstants.LIMIT_1;
import static org.dhis2.data.database.SqlConstants.LIMIT_10;
import static org.dhis2.data.database.SqlConstants.NOT_EQUAL;
import static org.dhis2.data.database.SqlConstants.ON;
import static org.dhis2.data.database.SqlConstants.ORDER_BY;
import static org.dhis2.data.database.SqlConstants.POINT;
import static org.dhis2.data.database.SqlConstants.PROGRAM_RULE_ACTION_TABLE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_RULE_CONDITION;
import static org.dhis2.data.database.SqlConstants.PROGRAM_RULE_PRIORITY;
import static org.dhis2.data.database.SqlConstants.PROGRAM_RULE_PROGRAM;
import static org.dhis2.data.database.SqlConstants.PROGRAM_RULE_PROGRAM_STAGE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_RULE_TABLE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_RULE_UID;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_DISPLAY_NAME;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_TABLE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_UID;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.QUOTE;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.WHERE;


@SuppressWarnings("PMD")
public final class RulesRepository {
    private static final String QUERY_RULES =
            SELECT + PROGRAM_RULE_TABLE + POINT + PROGRAM_RULE_UID + COMMA +
                    PROGRAM_RULE_TABLE + POINT + PROGRAM_RULE_PROGRAM_STAGE + COMMA +
                    PROGRAM_RULE_TABLE + POINT + PROGRAM_RULE_PRIORITY + COMMA +
                    PROGRAM_RULE_TABLE + POINT + PROGRAM_RULE_CONDITION +
                    FROM + PROGRAM_RULE_TABLE +
                    WHERE + PROGRAM_RULE_TABLE + POINT + PROGRAM_RULE_PROGRAM +
                    EQUAL + QUESTION_MARK;

    private static final String QUERY_VARIABLES = SELECT +
            "  name,\n" +
            "  programStage,\n" +
            "  programRuleVariableSourceType,\n" +
            "  dataElement,\n" +
            "  trackedEntityAttribute,\n" +
            "  Element.type,\n" +
            "  Attribute.type\n" +
            "FROM ProgramRuleVariable\n" +
            "  LEFT OUTER JOIN (\n" +
            "    SELECT\n" +
            "      uid as elementUid,\n" +
            "      valueType AS type\n" +
            "    FROM DataElement\n" +
            "  ) AS Element ON ProgramRuleVariable.dataElement = Element.elementUid\n" +
            "  LEFT OUTER JOIN (\n" +
            "    SELECT\n" +
            "      uid as attributeUid,\n" +
            "      valueType AS type\n" +
            "    FROM TrackedEntityAttribute\n" +
            "  ) AS Attribute ON ProgramRuleVariable.trackedEntityAttribute = Attribute.attributeUid\n" +
            "WHERE program = ? AND programRuleVariableSourceType IN (\n" +
            "  \"DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE\",\n" +
            "  \"DATAELEMENT_NEWEST_EVENT_PROGRAM\",\n" +
            "  \"DATAELEMENT_CURRENT_EVENT\",\n" +
            "  \"DATAELEMENT_PREVIOUS_EVENT\",\n" +
            "  \"CALCULATED_VALUE\",\n" +
            "  \"TEI_ATTRIBUTE\"\n" +
            ");";

    private static final String QUERY_ACTIONS = SELECT +
            "  ProgramRuleAction.programRule,\n" +
            "  ProgramRuleAction.programStage,\n" +
            "  ProgramRuleAction.programStageSection,\n" +
            "  ProgramRuleAction.programRuleActionType,\n" +
            "  ProgramRuleAction.programIndicator,\n" +
            "  ProgramRuleAction.trackedEntityAttribute,\n" +
            "  ProgramRuleAction.dataElement,\n" +
            "  ProgramRuleAction.location,\n" +
            "  ProgramRuleAction.content,\n" +
            "  ProgramRuleAction.data\n" +
            "FROM ProgramRuleAction\n" +
            "  INNER JOIN ProgramRule ON ProgramRuleAction.programRule = ProgramRule.uid\n" +
            "WHERE program = ? AND ProgramRuleAction.programRuleActionType IN (\n" +
            "  \"DISPLAYTEXT\",\n" +
            "  \"DISPLAYKEYVALUEPAIR\",\n" +
            "  \"HIDEFIELD\",\n" +
            "  \"HIDESECTION\",\n" +
            "  \"ASSIGN\",\n" +
            "  \"SHOWWARNING\",\n" +
            "  \"WARNINGONCOMPLETE\",\n" +
            "  \"SHOWERROR\",\n" +
            "  \"ERRORONCOMPLETE\",\n" +
            "  \"CREATEEVENT\",\n" +
            "  \"HIDEPROGRAMSTAGE\",\n" +
            "  \"SETMANDATORYFIELD\"" +
            ");";

    /**
     * Query all events except current one from a program without registration
     */
    private static final String QUERY_OTHER_EVENTS =
            SELECT + EventModel.TABLE + POINT + EventModel.Columns.UID + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.STATUS + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.EVENT_DATE + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.DUE_DATE + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.ORGANISATION_UNIT + COMMA +
                    PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_DISPLAY_NAME +
                    FROM + EventModel.TABLE +
                    JOIN + PROGRAM_STAGE_TABLE +
                    ON + PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_UID +
                    EQUAL + EventModel.TABLE + POINT + EventModel.Columns.PROGRAM_STAGE +
                    WHERE + EventModel.TABLE + POINT + EventModel.Columns.PROGRAM +
                    EQUAL + QUESTION_MARK +
                    AND + EventModel.TABLE + POINT + EventModel.Columns.EVENT_DATE +
                    LESS_OR_EQUAL + QUESTION_MARK +
                    AND + EventModel.TABLE + POINT + EventModel.Columns.STATE +
                    NOT_EQUAL + QUOTE + State.TO_DELETE + QUOTE +
                    ORDER_BY + EventModel.TABLE + POINT + EventModel.Columns.EVENT_DATE + DESC + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.LAST_UPDATED + DESC +
                    LIMIT_10;

    /**
     * Query all events except current one from an enrollment
     */
    private static final String QUERY_OTHER_EVENTS_ENROLLMENTS =
            SELECT + EventModel.TABLE + POINT + EventModel.Columns.UID + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.PROGRAM_STAGE + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.STATUS + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.EVENT_DATE + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.DUE_DATE + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.ORGANISATION_UNIT + COMMA +
                    PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_DISPLAY_NAME +
                    FROM + EventModel.TABLE +
                    JOIN + PROGRAM_STAGE_TABLE +
                    ON + PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_UID +
                    EQUAL + EventModel.TABLE + POINT + EventModel.Columns.PROGRAM_STAGE +
                    WHERE + EventModel.TABLE + POINT + EventModel.Columns.ENROLLMENT +
                    EQUAL + QUESTION_MARK +
                    AND + EventModel.TABLE + POINT + EventModel.Columns.EVENT_DATE +
                    LESS_OR_EQUAL + QUESTION_MARK +
                    AND + EventModel.TABLE + POINT + EventModel.Columns.STATE +
                    NOT_EQUAL + QUOTE + State.TO_DELETE + QUOTE +
                    ORDER_BY + EventModel.TABLE + POINT + EventModel.Columns.EVENT_DATE + DESC + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.LAST_UPDATED + DESC +
                    LIMIT_10;

    /**
     * Query all events from an enrollment
     */
    private static final String QUERY_EVENTS_ENROLLMENTS =
            SELECT + EventModel.TABLE + POINT + EventModel.Columns.UID + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.PROGRAM_STAGE + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.STATUS + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.EVENT_DATE + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.DUE_DATE + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.ORGANISATION_UNIT + COMMA +
                    PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_DISPLAY_NAME +
                    FROM + EventModel.TABLE +
                    JOIN + PROGRAM_STAGE_TABLE +
                    ON + PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_UID +
                    EQUAL + EventModel.TABLE + POINT + EventModel.Columns.PROGRAM_STAGE +
                    WHERE + EventModel.TABLE + POINT + EventModel.Columns.ENROLLMENT +
                    EQUAL + QUESTION_MARK +
                    AND + EventModel.TABLE + POINT + EventModel.Columns.STATE +
                    NOT_EQUAL + QUOTE + State.TO_DELETE + QUOTE +
                    ORDER_BY + EventModel.TABLE + POINT + EventModel.Columns.LAST_UPDATED + DESC +
                    LIMIT_10;

    private static final String QUERY_VALUES = SELECT +
            EventModel.Columns.EVENT_DATE + COMMA +
            EventModel.Columns.PROGRAM_STAGE + COMMA +
            TrackedEntityDataValueModel.Columns.DATA_ELEMENT + COMMA +
            TrackedEntityDataValueModel.Columns.VALUE +
            FROM + TrackedEntityDataValueModel.TABLE +
            INNER_JOIN + EventModel.TABLE +
            ON + TrackedEntityDataValueModel.TABLE + POINT + TrackedEntityDataValueModel.Columns.EVENT +
            EQUAL + EventModel.TABLE + POINT + EventModel.Columns.UID +
            WHERE + TrackedEntityDataValueModel.Columns.EVENT +
            EQUAL + QUESTION_MARK +
            AND + TrackedEntityDataValueModel.Columns.VALUE + IS_NOT_NULL +
            AND + EventModel.Columns.STATE +
            NOT_EQUAL + QUOTE + State.TO_DELETE + QUOTE;

    private static final String QUERY_ENROLLMENT =
            SELECT + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID + COMMA +
                    EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.INCIDENT_DATE + COMMA +
                    EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.ENROLLMENT_DATE + COMMA +
                    EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.ENROLLMENT_STATUS + COMMA +
                    EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.ORGANISATION_UNIT + COMMA +
                    ProgramModel.TABLE + POINT + ProgramModel.Columns.DISPLAY_NAME +
                    FROM + EnrollmentModel.TABLE +
                    JOIN + ProgramModel.TABLE +
                    ON + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID +
                    EQUAL + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.PROGRAM +
                    WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                    EQUAL + QUESTION_MARK +
                    LIMIT_1;

    private static final String QUERY_ATTRIBUTE_VALUES = SELECT +
            "  Field.id,\n" +
            "  Value.VALUE\n" +
            "FROM (Enrollment INNER JOIN Program ON Program.uid = Enrollment.program)\n" +
            "  INNER JOIN (\n" +
            "      SELECT\n" +
            "        TrackedEntityAttribute.uid AS id,\n" +
            "        ProgramTrackedEntityAttribute.program AS program\n" +
            "      FROM ProgramTrackedEntityAttribute INNER JOIN TrackedEntityAttribute\n" +
            "          ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n" +
            "    ) AS Field ON Field.program = Program.uid\n" +
            "  INNER JOIN TrackedEntityAttributeValue AS Value ON (\n" +
            "    Value.trackedEntityAttribute = Field.id\n" +
            "        AND Value.trackedEntityInstance = Enrollment.trackedEntityInstance)\n" +
            "WHERE Enrollment.uid = ? AND Value.VALUE IS NOT NULL;";

    @NonNull
    private final BriteDatabase briteDatabase;

    public RulesRepository(@NonNull BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    public Flowable<List<Rule>> rulesNew(@NonNull String programUid) {
        return Flowable.combineLatest(queryRules(programUid),
                queryRuleActionsList(programUid), RulesRepository::mapActionsToRulesNew);
    }

    @NonNull
    Flowable<List<RuleVariable>> ruleVariables(@NonNull String programUid) {
        return briteDatabase.createQuery(ProgramRuleVariableModel.TABLE, QUERY_VARIABLES, programUid)
                .mapToList(RulesRepository::mapToRuleVariable).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    public Flowable<List<RuleVariable>> ruleVariablesProgramStages(@NonNull String programUid) {
        return briteDatabase.createQuery(ProgramRuleVariableModel.TABLE, QUERY_VARIABLES, programUid)
                .mapToList(RulesRepository::mapToRuleVariableProgramStages).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private Flowable<List<Quartet<String, String, Integer, String>>> queryRules(
            @NonNull String programUid) {
        return briteDatabase.createQuery(PROGRAM_RULE_TABLE, QUERY_RULES, programUid)
                .mapToList(RulesRepository::mapToQuartet).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private Flowable<List<Pair<String, RuleAction>>> queryRuleActionsList(@NonNull String programUid) {
        return briteDatabase.createQuery(PROGRAM_RULE_ACTION_TABLE, QUERY_ACTIONS, programUid)
                .mapToList(RulesRepository::mapToActionPairs).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private static List<Rule> mapActionsToRulesNew(
            @NonNull List<Quartet<String, String, Integer, String>> rawRules, //ProgramRule uid, stage, priority and condition
            @NonNull List<Pair<String, RuleAction>> ruleActions) {
        List<Rule> rules = new ArrayList<>();

        for (Quartet<String, String, Integer, String> rawRule : rawRules) {

            List<RuleAction> pairActions = new ArrayList<>();
            for (Pair<String, RuleAction> pair : ruleActions) {
                if (pair.val0().equals(rawRule.val0()))
                    pairActions.add(pair.val1());
            }

            rules.add(Rule.create(rawRule.val1(), rawRule.val2(),
                    rawRule.val3(), new ArrayList<>(pairActions),
                    rawRule.val0())); //TODO: Change val0 to Rule Name
        }

        return rules;
    }

    @NonNull
    private static Quartet<String, String, Integer, String> mapToQuartet(@NonNull Cursor cursor) {
        String uid = cursor.getString(0);
        String condition = cursor.getString(3) == null ? "" : cursor.getString(3);

        String stage = cursor.isNull(1) ? "" : cursor.getString(1);
        Integer priority = cursor.isNull(2) ? 0 : cursor.getInt(2);

        return Quartet.create(uid, stage, priority, condition);
    }

    @NonNull
    private static Pair<String, RuleAction> mapToActionPairs(@NonNull Cursor cursor) {
        return Pair.create(cursor.getString(0), create(cursor));
    }

    private static RuleValueType getMimeType(ProgramRuleData programRuleData) {
        RuleValueType mimeType = null;
        if (!isEmpty(programRuleData.attributeType)) {
            mimeType = convertType(programRuleData.attributeType);
        } else if (!isEmpty(programRuleData.elementType)) {
            mimeType = convertType(programRuleData.elementType);
        }

        if (mimeType == null)
            mimeType = RuleValueType.TEXT;

        return mimeType;
    }

    private static class ProgramRuleData {
        String name;
        String stage;
        String sourceType;
        String dataElement;
        String attribute;
        String attributeType;
        String elementType;

        ProgramRuleData(@NonNull Cursor cursor) {
            name = cursor.getString(0);
            stage = cursor.getString(1);
            sourceType = cursor.getString(2);
            dataElement = cursor.getString(3);
            attribute = cursor.getString(4);
            // Mime types of the attribute and data element.
            attributeType = cursor.getString(5);
            elementType = cursor.getString(6);
        }
    }

    @NonNull
    private static RuleVariable mapToRuleVariable(@NonNull Cursor cursor) {
        // String representation of VALUE type.
        ProgramRuleData programRuleData = new ProgramRuleData(cursor);
        RuleValueType mimeType = getMimeType(programRuleData);

        switch (ProgramRuleVariableSourceType.valueOf(programRuleData.sourceType)) {
            case TEI_ATTRIBUTE:
                return RuleVariableAttribute.create(programRuleData.name, programRuleData.attribute == null ? "" : programRuleData.attribute, mimeType);
            case DATAELEMENT_CURRENT_EVENT:
                return RuleVariableCurrentEvent.create(programRuleData.name, programRuleData.dataElement, mimeType);
            case DATAELEMENT_NEWEST_EVENT_PROGRAM:
                return RuleVariableNewestEvent.create(programRuleData.name, programRuleData.dataElement, mimeType);
            case DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE:
                if (programRuleData.stage == null)
                    programRuleData.stage = "";
                return RuleVariableNewestStageEvent.create(programRuleData.name, programRuleData.dataElement, programRuleData.stage, mimeType);
            case DATAELEMENT_PREVIOUS_EVENT:
                return RuleVariablePreviousEvent.create(programRuleData.name, programRuleData.dataElement, mimeType);
            case CALCULATED_VALUE:
                String variable = programRuleData.dataElement != null ? programRuleData.dataElement : programRuleData.attribute;
                return RuleVariableCalculatedValue.create(programRuleData.name, variable != null ? variable : "", mimeType);
            default:
                throw new IllegalArgumentException("Unsupported variable " +
                        "source type: " + programRuleData.sourceType);
        }
    }

    @NonNull
    private static RuleVariable mapToRuleVariableProgramStages(@NonNull Cursor cursor) {
        // String representation of VALUE type.
        ProgramRuleData programRuleData = new ProgramRuleData(cursor);
        RuleValueType mimeType = getMimeType(programRuleData);

        switch (ProgramRuleVariableSourceType.valueOf(programRuleData.sourceType)) {
            case TEI_ATTRIBUTE:
                return RuleVariableAttribute.create(programRuleData.name, programRuleData.attribute, mimeType);
            case DATAELEMENT_CURRENT_EVENT:
                return RuleVariableCurrentEvent.create(programRuleData.name, programRuleData.dataElement, mimeType);
            case DATAELEMENT_NEWEST_EVENT_PROGRAM:
                return RuleVariableNewestEvent.create(programRuleData.name, programRuleData.dataElement, mimeType);
            case DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE:
                if (programRuleData.stage == null)
                    programRuleData.stage = "";
                return RuleVariableNewestStageEvent.create(programRuleData.name, programRuleData.dataElement, programRuleData.stage, mimeType);
            case DATAELEMENT_PREVIOUS_EVENT:
                return RuleVariablePreviousEvent.create(programRuleData.name, programRuleData.dataElement, mimeType);
            case CALCULATED_VALUE:
                String variable = programRuleData.dataElement != null ? programRuleData.dataElement : programRuleData.attribute;
                return RuleVariableCalculatedValue.create(programRuleData.name, variable != null ? variable : "", mimeType);
            default:
                throw new IllegalArgumentException("Unsupported variable " +
                        "source type: " + programRuleData.sourceType);
        }
    }

    @NonNull
    private static RuleValueType convertType(@NonNull String type) {
        ValueType valueType = ValueType.valueOf(type);
        if (valueType.isInteger() || valueType.isNumeric()) {
            return RuleValueType.NUMERIC;
        } else if (valueType.isBoolean()) {
            return RuleValueType.BOOLEAN;
        } else {
            return RuleValueType.TEXT;
        }
    }

    @NonNull
    public static RuleAction create(@NonNull Cursor cursor) {
        String programStage = cursor.getString(1);
        String section = cursor.getString(2);
        @NonNull String dataElement = cursor.getString(6) != null ? cursor.getString(6) : "";
        @NonNull String attribute = cursor.getString(5) != null ? cursor.getString(5) : "";
        String location = cursor.getString(7);
        String content = cursor.getString(8);
        String data = cursor.getString(9);

        @NonNull String field = isEmpty(attribute) ? dataElement : attribute;

        switch (ProgramRuleActionType.valueOf(cursor.getString(3))) {
            case DISPLAYTEXT:
                return createDisplayTextAction(content, data, location);
            case DISPLAYKEYVALUEPAIR:
                return createDisplayKeyValuePairAction(content, data, location);
            case HIDEFIELD:
                return RuleActionHideField.create(content, field);
            case HIDESECTION:
                return RuleActionHideSection.create(section);
            case ASSIGN:
                return RuleActionAssign.create(content, data, field);
            case SHOWWARNING:
                return RuleActionShowWarning.create(content, data, field);
            case WARNINGONCOMPLETE:
                return RuleActionWarningOnCompletion.create(content, data, field);
            case SHOWERROR:
                return RuleActionShowError.create(content, data, field);
            case ERRORONCOMPLETE:
                if (content == null)
                    content = "";
                if (data == null)
                    data = "";

                return RuleActionErrorOnCompletion.create(content, data, field);
            case CREATEEVENT:
                return RuleActionCreateEvent.create(content, data, programStage);
            case HIDEPROGRAMSTAGE:
                return RuleActionHideProgramStage.create(programStage);
            case SETMANDATORYFIELD:
                return RuleActionSetMandatoryField.create(field);
            default:
                throw new IllegalArgumentException(
                        "Unsupported RuleActionType: " + cursor.getString(3));
        }
    }

    @NonNull
    private static RuleActionDisplayText createDisplayTextAction(@NonNull String content,
                                                                 @NonNull String data, @NonNull String location) {
        if (location.equals(RuleActionDisplayText.LOCATION_FEEDBACK_WIDGET)) {
            return RuleActionDisplayText.createForFeedback(content, data);
        } else {
            return RuleActionDisplayText.createForIndicators(content, data);
        }
    }

    @NonNull
    private static RuleActionDisplayKeyValuePair createDisplayKeyValuePairAction(
            @NonNull String content, @NonNull String data, @NonNull String location) {
        if (location.equals(RuleActionDisplayKeyValuePair.LOCATION_FEEDBACK_WIDGET)) {
            return RuleActionDisplayKeyValuePair.createForFeedback(content, data);
        } else {
            return RuleActionDisplayKeyValuePair.createForIndicators(content, data);
        }
    }

    public Flowable<List<RuleEvent>> otherEvents(String eventUidToEvaluate) {
        return briteDatabase.createQuery(EventModel.TABLE, "SELECT * FROM Event WHERE Event.uid = ? LIMIT 1", eventUidToEvaluate == null ? "" : eventUidToEvaluate)
                .mapToOne(EventModel::create)
                .flatMap(eventModel ->
                        briteDatabase.createQuery(ProgramModel.TABLE, "SELECT Program.* FROM Program JOIN Event ON Event.program = Program.uid WHERE Event.uid = ? LIMIT 1", eventUidToEvaluate == null ? "" : eventUidToEvaluate)
                                .mapToOne(ProgramModel::create).flatMap(programModel ->
                                briteDatabase.createQuery(EventModel.TABLE, eventModel.enrollment() == null ? QUERY_OTHER_EVENTS : QUERY_OTHER_EVENTS_ENROLLMENTS,
                                        eventModel.enrollment() == null ? programModel.uid() : eventModel.enrollment(),
                                        eventUidToEvaluate == null ? "" : eventUidToEvaluate,
                                        DateUtils.databaseDateFormat().format(eventModel.eventDate()))
                                        .mapToList(this::createRuleEvent))).toFlowable(BackpressureStrategy.LATEST);
    }

    private RuleEvent createRuleEvent(Cursor cursor) throws ParseException {
        String eventUid = cursor.getString(0);
        String programStageUid = cursor.getString(1);
        Date eventDate = DateUtils.databaseDateFormat().parse(cursor.getString(3));
        Date dueDate = cursor.isNull(4) ? eventDate : DateUtils.databaseDateFormat().parse(cursor.getString(4));
        String orgUnit = cursor.getString(5);
        String orgUnitCode = getOrgUnitCode(orgUnit);
        String programStageName = cursor.getString(6);
        RuleEvent.Status status = cursor.getString(2).equals(RuleEvent.Status.VISITED.name()) ?
                RuleEvent.Status.ACTIVE :
                RuleEvent.Status.valueOf(cursor.getString(2));

        return RuleEvent.builder()
                .event(eventUid)
                .programStage(programStageUid)
                .programStageName(programStageName)
                .status(status)
                .eventDate(eventDate)
                .dueDate(dueDate)
                .organisationUnit(orgUnit)
                .organisationUnitCode(orgUnitCode)
                .dataValues(getDataValues(eventUid, cursor))
                .build();
    }

    private List<RuleDataValue> getDataValues(String eventUid, Cursor cursor) throws ParseException {
        List<RuleDataValue> dataValues = new ArrayList<>();
        Cursor dataValueCursor = briteDatabase.query(QUERY_VALUES, eventUid);
        if (dataValueCursor != null && dataValueCursor.moveToFirst()) {
            for (int i = 0; i < dataValueCursor.getCount(); i++) {
                Date eventDateV = DateUtils.databaseDateFormat().parse(dataValueCursor.getString(0));
                String value = cursor.getString(3) != null ? dataValueCursor.getString(3) : "";
                dataValues.add(RuleDataValue.create(eventDateV, dataValueCursor.getString(1),
                        dataValueCursor.getString(2), value));
                dataValueCursor.moveToNext();
            }
            dataValueCursor.close();
        }
        return dataValues;
    }

    public Flowable<List<RuleEvent>> enrollmentEvents(String enrollmentUid) {
        return briteDatabase.createQuery(EventModel.TABLE, QUERY_EVENTS_ENROLLMENTS, enrollmentUid)
                .mapToList(cursor -> {
                    String eventUid = cursor.getString(0);
                    String programStageUid = cursor.getString(1);
                    Date eventDate = cursor.isNull(3) ? null : DateUtils.databaseDateFormat().parse(cursor.getString(3));
                    Date dueDate = cursor.isNull(4) ? eventDate : DateUtils.databaseDateFormat().parse(cursor.getString(4)); //TODO: Should due date always be not null?
                    String orgUnit = cursor.getString(5);
                    String orgUnitCode = getOrgUnitCode(orgUnit);
                    String programStageName = cursor.getString(6);
                    RuleEvent.Status status = cursor.getString(2).equals(RuleEvent.Status.VISITED.name()) ?
                            RuleEvent.Status.ACTIVE :
                            RuleEvent.Status.valueOf(cursor.getString(2));

                    return RuleEvent.builder()
                            .event(eventUid)
                            .programStage(programStageUid)
                            .programStageName(programStageName)
                            .status(status)
                            .eventDate(eventDate)
                            .dueDate(dueDate)
                            .organisationUnit(orgUnit)
                            .organisationUnitCode(orgUnitCode)
                            .dataValues(getDataValues(eventUid, cursor))
                            .build();

                }).toFlowable(BackpressureStrategy.LATEST);

    }

    public Flowable<RuleEnrollment> enrollment(String eventUid) {
        return briteDatabase.createQuery(EventModel.TABLE, "SELECT Event.*, Program.displayName FROM Event JOIN Program ON Program.uid = Event.program WHERE Event.uid = ? LIMIT 1", eventUid == null ? "" : eventUid)
                .mapToOne(cursor -> Pair.create(EventModel.create(cursor), cursor.getString(cursor.getColumnIndex("displayName"))))
                .flatMap(pair -> {
                            EventModel eventModel = pair.val0();
                            String programName = pair.val1();

                            String ouCode = getOrgUnitCode(eventModel.organisationUnit());

                            if (eventModel.enrollment() != null)
                                return queryAttributeValues(eventModel.enrollment())
                                        .switchMap(ruleAttributeValues ->
                                                queryEnrollment(ruleAttributeValues, eventModel.enrollment())
                                        ).toObservable();
                            else
                                return Observable.just(
                                        RuleEnrollment.create("",
                                                Calendar.getInstance().getTime(),
                                                Calendar.getInstance().getTime(),
                                                RuleEnrollment.Status.CANCELLED,
                                                eventModel.organisationUnit(),
                                                ouCode,
                                                new ArrayList<>(),
                                                programName));
                        }
                ).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    public String getOrgUnitCode(String orgUnitUid) {
        String ouCode = "";
        Cursor cursor = briteDatabase.query(SELECT + OrganisationUnitModel.Columns.CODE +
                FROM + OrganisationUnitModel.TABLE + WHERE + OrganisationUnitModel.Columns.UID +
                EQUAL + QUESTION_MARK + LIMIT_1, orgUnitUid);
        if (cursor != null && cursor.moveToFirst()) {
            ouCode = cursor.getString(0);
            cursor.close();
        }
        return ouCode;
    }

    @NonNull
    private Flowable<List<RuleAttributeValue>> queryAttributeValues(String enrollmentUid) {
        return briteDatabase.createQuery(Arrays.asList(EnrollmentModel.TABLE,
                TrackedEntityAttributeValueModel.TABLE), QUERY_ATTRIBUTE_VALUES, enrollmentUid)
                .mapToList(cursor -> RuleAttributeValue.create(
                        cursor.getString(0), cursor.getString(1))
                ).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private Flowable<RuleEnrollment> queryEnrollment(@NonNull List<RuleAttributeValue> attributeValues, @NonNull String enrollmentUid) {
        return briteDatabase.createQuery(EnrollmentModel.TABLE, QUERY_ENROLLMENT, enrollmentUid)
                .mapToOne(cursor -> {
                    Date enrollmentDate = BaseIdentifiableObject.DATE_FORMAT.parse(cursor.getString(2));
                    Date incidentDate = cursor.isNull(1) ?
                            enrollmentDate : BaseIdentifiableObject.DATE_FORMAT.parse(cursor.getString(1));
                    RuleEnrollment.Status status = RuleEnrollment.Status
                            .valueOf(cursor.getString(3));
                    String orgUnit = cursor.getString(4);
                    String programName = cursor.getString(5);

                    String ouCode = getOrgUnitCode(orgUnit);

                    return RuleEnrollment.create(cursor.getString(0),
                            incidentDate, enrollmentDate, status, orgUnit, ouCode, attributeValues, programName);
                }).toFlowable(BackpressureStrategy.LATEST);
    }
}