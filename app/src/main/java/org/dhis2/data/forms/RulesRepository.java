package org.dhis2.data.forms;

import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Quartet;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.constant.Constant;
import org.hisp.dhis.android.core.constant.ConstantTableInfo;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramRuleActionModel;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramRuleModel;
import org.hisp.dhis.android.core.program.ProgramRuleVariableModel;
import org.hisp.dhis.android.core.program.ProgramRuleVariableSourceType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.rules.models.Rule;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleActionCreateEvent;
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair;
import org.hisp.dhis.rules.models.RuleActionDisplayText;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionHideOption;
import org.hisp.dhis.rules.models.RuleActionHideOptionGroup;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;


@SuppressWarnings("PMD")
public final class RulesRepository {
    private static final String QUERY_CONSTANTS = "SELECT * " +
            "FROM Constant";


    private static final String QUERY_RULES = "SELECT\n" +
            "  ProgramRule.uid, \n" +
            "  ProgramRule.programStage,\n" +
            "  ProgramRule.priority,\n" +
            "  ProgramRule.condition\n" +
            "FROM ProgramRule\n" +
            "WHERE program = ?;";

    private static final String QUERY_VARIABLES = "SELECT\n" +
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

    private static final String QUERY_ACTIONS = "SELECT\n" +
            "  ProgramRuleAction.programRule,\n" +
            "  ProgramRuleAction.programStage,\n" +
            "  ProgramRuleAction.programStageSection,\n" +
            "  ProgramRuleAction.programRuleActionType,\n" +
            "  ProgramRuleAction.programIndicator,\n" +
            "  ProgramRuleAction.trackedEntityAttribute,\n" +
            "  ProgramRuleAction.dataElement,\n" +
            "  ProgramRuleAction.location,\n" +
            "  ProgramRuleAction.content,\n" +
            "  ProgramRuleAction.data,\n" +
            "  ProgramRuleAction.option,\n" +
            "  ProgramRuleAction.optionGroup\n" +
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
            "  \"SETMANDATORYFIELD\",\n" +
            "  \"HIDEOPTION\",\n" +
            "  \"HIDEOPTIONGROUP\"" +
            ");";

    /**
     * Query all events except current one from a program without registration
     */
    private static final String QUERY_OTHER_EVENTS = "SELECT Event.uid,\n" +
            "  Event.programStage,\n" +
            "  Event.status,\n" +
            "  Event.eventDate,\n" +
            "  Event.dueDate,\n" +
            "  Event.organisationUnit,\n" +
            "  ProgramStage.displayName\n" +
            "FROM Event\n" +
            "JOIN ProgramStage ON ProgramStage.uid = Event.programStage\n" +
            "WHERE Event.program = ? AND Event.uid != ? AND (Event.eventDate < ? OR (Event.eventDate = ? AND Event.lastUpdated < ?))\n" +
            " AND Event.Status NOT IN ('SCHEDULE', 'SKIPPED', 'OVERDUE')" +
            " AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' ORDER BY Event.eventDate DESC,Event.lastUpdated DESC LIMIT 10";

    /**
     * Query all events except current one from an enrollment
     */
    private static final String QUERY_OTHER_EVENTS_ENROLLMENTS = "SELECT Event.uid,\n" +
            "  Event.programStage,\n" +
            "  Event.status,\n" +
            "  Event.eventDate,\n" +
            "  Event.dueDate,\n" +
            "  Event.organisationUnit,\n" +
            "  ProgramStage.displayName\n" +
            "FROM Event\n" +
            "JOIN ProgramStage ON ProgramStage.uid = Event.programStage\n" +
            "WHERE Event.enrollment = ? AND Event.uid != ? AND (Event.eventDate < ? OR (Event.eventDate = ? AND Event.lastUpdated < ?))\n" +
            " AND Event.Status NOT IN ('SCHEDULE', 'SKIPPED', 'OVERDUE')" +
            " AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' ORDER BY Event.eventDate DESC,Event.lastUpdated DESC";/*LIMIT 10*/

    /**
     * Query all events from an enrollment
     */
    private static final String QUERY_EVENTS_ENROLLMENTS = "SELECT Event.uid,\n" +
            "  Event.programStage,\n" +
            "  Event.status,\n" +
            "  Event.eventDate,\n" +
            "  Event.dueDate,\n" +
            "  Event.organisationUnit,\n" +
            "  ProgramStage.displayName\n" +
            "FROM Event\n" +
            "JOIN ProgramStage ON ProgramStage.uid = Event.programStage\n" +
            "WHERE Event.enrollment = ?\n" +
            " AND Event.Status NOT IN ('SCHEDULE', 'SKIPPED', 'OVERDUE')" +
            " AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' ORDER BY Event.eventDate,Event.lastUpdated DESC ";/*LIMIT 10*/

    private static final String QUERY_VALUES = "SELECT " +
            "  Event.eventDate," +
            "  Event.programStage," +
            "  TrackedEntityDataValue.dataElement," +
            "  TrackedEntityDataValue.value," +
            "  ProgramRuleVariable.useCodeForOptionSet," +
            "  Option.code," +
            "  Option.name" +
            " FROM TrackedEntityDataValue " +
            "  INNER JOIN Event ON TrackedEntityDataValue.event = Event.uid " +
            "  INNER JOIN DataElement ON DataElement.uid = TrackedEntityDataValue.dataElement " +
            "  LEFT JOIN ProgramRuleVariable ON ProgramRuleVariable.dataElement = DataElement.uid " +
            "  LEFT JOIN Option ON (Option.optionSet = DataElement.optionSet AND Option.code = TrackedEntityDataValue.value) " +
            " WHERE Event.uid = ? AND value IS NOT NULL AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "';";

    private static final String QUERY_ENROLLMENT = "SELECT\n" +
            "  Enrollment.uid,\n" +
            "  Enrollment.incidentDate,\n" +
            "  Enrollment.enrollmentDate,\n" +
            "  Enrollment.status,\n" +
            "  Enrollment.organisationUnit,\n" +
            "  Program.displayName\n" +
            "FROM Enrollment\n" +
            "JOIN Program ON Program.uid = Enrollment.program\n" +
            "WHERE Enrollment.uid = ? \n" +
            "LIMIT 1;";

    private static final String QUERY_ATTRIBUTE_VALUES = "SELECT\n" +
            "  Field.id,\n" +
            "  Value.value,\n" +
            "  ProgramRuleVariable.useCodeForOptionSet,\n" +
            "  Option.code,\n" +
            "  Option.name\n" +
            "FROM (Enrollment INNER JOIN Program ON Program.uid = Enrollment.program)\n" +
            "  INNER JOIN (\n" +
            "      SELECT\n" +
            "        TrackedEntityAttribute.uid AS id,\n" +
            "        TrackedEntityAttribute.optionSet AS optionSet,\n" +
            "        ProgramTrackedEntityAttribute.program AS program\n" +
            "      FROM ProgramTrackedEntityAttribute INNER JOIN TrackedEntityAttribute\n" +
            "          ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n" +
            "    ) AS Field ON Field.program = Program.uid\n" +
            "  INNER JOIN TrackedEntityAttributeValue AS Value ON (\n" +
            "    Value.trackedEntityAttribute = Field.id\n" +
            "        AND Value.trackedEntityInstance = Enrollment.trackedEntityInstance)\n" +
            "  LEFT JOIN ProgramRuleVariable ON ProgramRuleVariable.trackedEntityAttribute = Field.id " +
            "  LEFT JOIN Option ON (Option.optionSet = Field.optionSet AND Option.code = Value.value) " +
            "WHERE Enrollment.uid = ? AND Value.value IS NOT NULL;";

    @NonNull
    private final BriteDatabase briteDatabase;
    private int count;

    public RulesRepository(@NonNull BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    public Flowable<List<Rule>> rulesNew(@NonNull String programUid) {
        return Flowable.combineLatest(queryRules(programUid),
                queryRuleActionsList(programUid), RulesRepository::mapActionsToRulesNew);
    }

    @NonNull
    public Flowable<List<RuleVariable>> ruleVariables(@NonNull String programUid) {
        return briteDatabase.createQuery(ProgramRuleVariableModel.TABLE, QUERY_VARIABLES, programUid)
                .mapToList(RulesRepository::mapToRuleVariable).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    public Flowable<List<RuleVariable>> ruleVariablesProgramStages(@NonNull String programUid) {
        return briteDatabase.createQuery(ProgramRuleVariableModel.TABLE, QUERY_VARIABLES, programUid)
                .mapToList(RulesRepository::mapToRuleVariableProgramStages).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    public Flowable<Map<String, String>> queryConstants() {
        return briteDatabase.createQuery(ConstantTableInfo.TABLE_INFO.name(), QUERY_CONSTANTS)
                .mapToList(Constant::create)
                .map(constants -> {
                    Map<String, String> constantsMap = new HashMap<>();
                    for (Constant constant : constants) {
                        constantsMap.put(constant.uid(), Objects.requireNonNull(constant.value()).toString());
                    }
                    return constantsMap;
                })
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private Flowable<List<Quartet<String, String, Integer, String>>> queryRules(
            @NonNull String programUid) {
        return briteDatabase.createQuery(ProgramRuleModel.TABLE, QUERY_RULES, programUid)
                .mapToList(RulesRepository::mapToQuartet).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private Flowable<List<Pair<String, RuleAction>>> queryRuleActionsList(@NonNull String programUid) {
        return briteDatabase.createQuery(ProgramRuleActionModel.TABLE, QUERY_ACTIONS, programUid)
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
                    rawRule.val3(), new ArrayList<>(pairActions), rawRule.val0())); //TODO: Change val0 to Rule Name
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

    @NonNull
    private static RuleVariable mapToRuleVariable(@NonNull Cursor cursor) {
        String name = cursor.getString(0);
        String stage = cursor.getString(1);
        String sourceType = cursor.getString(2);
        String dataElement = cursor.getString(3);
        String attribute = cursor.getString(4);

        // Mime types of the attribute and data element.
        String attributeType = cursor.getString(5);
        String elementType = cursor.getString(6);

        // String representation of value type.
        RuleValueType mimeType = null;
        if (!isEmpty(attributeType)) {
            mimeType = convertType(attributeType);
        } else if (!isEmpty(elementType)) {
            mimeType = convertType(elementType);
        }

        if (mimeType == null) {
            mimeType = RuleValueType.TEXT;
        }

        switch (ProgramRuleVariableSourceType.valueOf(sourceType)) {
            case TEI_ATTRIBUTE:
                return RuleVariableAttribute.create(name, attribute == null ? "" : attribute, mimeType);
            case DATAELEMENT_CURRENT_EVENT:
                return RuleVariableCurrentEvent.create(name, dataElement, mimeType);
            case DATAELEMENT_NEWEST_EVENT_PROGRAM:
                return RuleVariableNewestEvent.create(name, dataElement, mimeType);
            case DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE:
                if (stage == null)
                    stage = "";
                return RuleVariableNewestStageEvent.create(name, dataElement, stage, mimeType);
            case DATAELEMENT_PREVIOUS_EVENT:
                return RuleVariablePreviousEvent.create(name, dataElement, mimeType);
            case CALCULATED_VALUE:
                String variable = dataElement != null ? dataElement : attribute;
                return RuleVariableCalculatedValue.create(name, variable != null ? variable : "", mimeType);
            default:
                throw new IllegalArgumentException("Unsupported variable " +
                        "source type: " + sourceType);
        }
    }

    @NonNull
    private static RuleVariable mapToRuleVariableProgramStages(@NonNull Cursor cursor) {
        String name = cursor.getString(0);
        String stage = cursor.getString(1);
        String sourceType = cursor.getString(2);
        String dataElement = cursor.getString(3);
        String attribute = cursor.getString(4);

        // Mime types of the attribute and data element.
        String attributeType = cursor.getString(5);
        String elementType = cursor.getString(6);

        // String representation of value type.
        RuleValueType mimeType = null;
        if (!isEmpty(attributeType)) {
            mimeType = convertType(attributeType);
        } else if (!isEmpty(elementType)) {
            mimeType = convertType(elementType);
        }

        if (mimeType == null) {
            mimeType = RuleValueType.TEXT;
        }

        switch (ProgramRuleVariableSourceType.valueOf(sourceType)) {
            case TEI_ATTRIBUTE:
                return RuleVariableAttribute.create(name, attribute, mimeType);
            case DATAELEMENT_CURRENT_EVENT:
                return RuleVariableCurrentEvent.create(name, dataElement, mimeType);
            case DATAELEMENT_NEWEST_EVENT_PROGRAM:
                return RuleVariableNewestEvent.create(name, dataElement, mimeType);
            case DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE:
                if (stage == null)
                    stage = "";
                return RuleVariableNewestStageEvent.create(name, dataElement, stage, mimeType);
            case DATAELEMENT_PREVIOUS_EVENT:
                return RuleVariablePreviousEvent.create(name, dataElement, mimeType);
            case CALCULATED_VALUE:
                String variable = dataElement != null ? dataElement : attribute;
                return RuleVariableCalculatedValue.create(name, variable != null ? variable : "", mimeType);
            default:
                throw new IllegalArgumentException("Unsupported variable " +
                        "source type: " + sourceType);
        }
    }

    @NonNull
    private static Map<String, String> mapToConstantsMap(@NonNull Cursor cursor) {
        String uid = cursor.getString(0);
        String value = cursor.getString(1);

        Map<String, String> constants = new HashMap<>();
        if (cursor.moveToFirst())
            constants.put(uid, value);
        return constants;
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
        ProgramRuleActionType actionType = ProgramRuleActionType.valueOf(cursor.getString(3));
        String programStage = cursor.getString(1);
        String section = cursor.getString(2);
        String attribute = cursor.getString(5);
        String dataElement = cursor.getString(6);
        String location = cursor.getString(7);
        String content = cursor.getString(8);
        String data = cursor.getString(9);
        String option = cursor.getString(10);
        String optionGroup = cursor.getString(11);

        return create(actionType, programStage, section, attribute, dataElement, location, content, data, option, optionGroup);
    }

    @NonNull
    public static RuleAction create(ProgramRuleActionType actionType, String programStage, String section, String attribute,
                                    String dataElement, String location, String content, String data, String option, String optionGroup) {

        if (dataElement == null && attribute == null) {
            dataElement = "";
            attribute = "";
        }

        String field = dataElement == null ? "" : dataElement;

        switch (actionType) {
            case DISPLAYTEXT:
                if(location!=null)
                    return createDisplayTextAction(content, data, location);
            case DISPLAYKEYVALUEPAIR:
                if(location!=null)
                    return createDisplayKeyValuePairAction(content, data, location);
            case HIDEFIELD:
                return RuleActionHideField.create(content,
                        isEmpty(attribute) ? field : attribute);
            case HIDESECTION:
                return RuleActionHideSection.create(section);
            case ASSIGN:
                return RuleActionAssign.create(content, isEmpty(data) ? "" : data,
                        isEmpty(attribute) ? field : attribute);
            case SHOWWARNING:
                return RuleActionShowWarning.create(content, data,
                        isEmpty(attribute) ? field : attribute);
            case WARNINGONCOMPLETE:
                return RuleActionWarningOnCompletion.create(content, data,
                        isEmpty(attribute) ? field : attribute);
            case SHOWERROR:
                return RuleActionShowError.create(content, data,
                        isEmpty(attribute) ? field : attribute);
            case ERRORONCOMPLETE:
                if (content == null)
                    content = "";
                if (data == null)
                    data = "";

                return RuleActionErrorOnCompletion.create(content, data,
                        isEmpty(attribute) ? field : attribute);
            case CREATEEVENT:
                return RuleActionCreateEvent.create(content, data, programStage);
            case HIDEPROGRAMSTAGE:
                return RuleActionHideProgramStage.create(programStage);
            case SETMANDATORYFIELD:
                return RuleActionSetMandatoryField.create(isEmpty(attribute) ? field : attribute);
            case HIDEOPTION:
                return RuleActionHideOption.create(content, isEmpty(attribute) ? field : attribute, option);
            case HIDEOPTIONGROUP:
                return RuleActionHideOptionGroup.create(content, optionGroup);
            default:
                return RuleActionUnsupported.create("UNSUPPORTED RULE ACTION TYPE", actionType.name());
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
                .flatMap(eventModel -> {
                    count = 0;
                    return briteDatabase.createQuery(ProgramModel.TABLE, "SELECT Program.* FROM Program JOIN Event ON Event.program = Program.uid WHERE Event.uid = ? LIMIT 1", eventUidToEvaluate == null ? "" : eventUidToEvaluate)
                            .mapToOne(ProgramModel::create).flatMap(programModel ->
                                    briteDatabase.createQuery(EventModel.TABLE, eventModel.enrollment() == null ? QUERY_OTHER_EVENTS : QUERY_OTHER_EVENTS_ENROLLMENTS,
                                            eventModel.enrollment() == null ? programModel.uid() : eventModel.enrollment(),
                                            eventUidToEvaluate == null ? "" : eventUidToEvaluate,
                                            DateUtils.databaseDateFormat().format(eventModel.eventDate()),
                                            DateUtils.databaseDateFormat().format(eventModel.eventDate()),
                                            DateUtils.databaseDateFormat().format(eventModel.lastUpdated()))
                                            .mapToList(cursor -> {
                                                List<RuleDataValue> dataValues = new ArrayList<>();
                                                String eventUid = cursor.getString(0);
                                                String programStageUid = cursor.getString(1);
                                                Date eventDate = DateUtils.databaseDateFormat().parse(cursor.getString(3));
                                                Date dueDate = cursor.isNull(4) ? eventDate : DateUtils.databaseDateFormat().parse(cursor.getString(4));
                                                String orgUnit = cursor.getString(5);
                                                String orgUnitCode = getOrgUnitCode(orgUnit);
                                                String programStageName = cursor.getString(6);
                                                RuleEvent.Status status = cursor.getString(2).equals(RuleEvent.Status.VISITED.toString()) ?
                                                        RuleEvent.Status.ACTIVE :
                                                        RuleEvent.Status.valueOf(cursor.getString(2)); //TODO: WHAT?

                                                try (Cursor dataValueCursor = briteDatabase.query(QUERY_VALUES, eventUid)) {
                                                    if (dataValueCursor != null && dataValueCursor.moveToFirst()) {
                                                        for (int i = 0; i < dataValueCursor.getCount(); i++) {
                                                            Date eventDateV = DateUtils.databaseDateFormat().parse(dataValueCursor.getString(0));
                                                            String programStage = dataValueCursor.getString(1);
                                                            String dataElement = dataValueCursor.getString(2);
                                                            String value = dataValueCursor.getString(3) != null ? dataValueCursor.getString(3) : "";
                                                            boolean useCode = dataValueCursor.getInt(4) == 1;
                                                            String optionCode = dataValueCursor.getString(5);
                                                            String optionName = dataValueCursor.getString(6);
                                                            if (!isEmpty(optionCode) && !isEmpty(optionName))
                                                                value = useCode ? optionCode : optionName; //If de has optionSet then check if value should be code or name for program rules
                                                            dataValues.add(RuleDataValue.create(eventDateV, programStage,
                                                                    dataElement, value));
                                                            dataValueCursor.moveToNext();
                                                        }
                                                    }
                                                }

                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTime(eventDate);
                                                calendar.add(Calendar.SECOND,count);
                                                eventDate = calendar.getTime();
                                                calendar.setTime(dueDate);
                                                calendar.add(Calendar.SECOND,count);
                                                dueDate = calendar.getTime();
                                                count--;

                                                return RuleEvent.builder()
                                                        .event(eventUid)
                                                        .programStage(programStageUid)
                                                        .programStageName(programStageName)
                                                        .status(status)
                                                        .eventDate(eventDate)
                                                        .dueDate(dueDate)
                                                        .organisationUnit(orgUnit)
                                                        .organisationUnitCode(orgUnitCode)
                                                        .dataValues(dataValues)
                                                        .build();

                                            }));
                }).toFlowable(BackpressureStrategy.LATEST);
    }


    public Flowable<List<RuleEvent>> enrollmentEvents(String enrollmentUid) {
        return briteDatabase.createQuery(EventModel.TABLE, QUERY_EVENTS_ENROLLMENTS, enrollmentUid)
                .mapToList(cursor -> {
                    List<RuleDataValue> dataValues = new ArrayList<>();
                    String eventUid = cursor.getString(0);
                    String programStageUid = cursor.getString(1);
                    Date eventDate = cursor.isNull(3) ? null : DateUtils.databaseDateFormat().parse(cursor.getString(3));
                    Date dueDate = cursor.isNull(4) ? eventDate : DateUtils.databaseDateFormat().parse(cursor.getString(4)); //TODO: Should due date always be not null?
                    String orgUnit = cursor.getString(5);
                    String orgUnitCode = getOrgUnitCode(orgUnit);
                    String programStageName = cursor.getString(6);
                    RuleEvent.Status status = cursor.getString(2).equals(RuleEvent.Status.VISITED.toString()) ? RuleEvent.Status.ACTIVE : RuleEvent.Status.valueOf(cursor.getString(2)); //TODO: WHAT?

                    try (Cursor dataValueCursor = briteDatabase.query(QUERY_VALUES, eventUid)) {
                        if (dataValueCursor != null && dataValueCursor.moveToFirst()) {
                            for (int i = 0; i < dataValueCursor.getCount(); i++) {
                                Date eventDateV = DateUtils.databaseDateFormat().parse(dataValueCursor.getString(0));
                                String programStage = dataValueCursor.getString(1);
                                String dataElement = dataValueCursor.getString(2);
                                String value = dataValueCursor.getString(3) != null ? dataValueCursor.getString(3) : "";
                                boolean useCode = dataValueCursor.getInt(4) == 1;
                                String optionCode = dataValueCursor.getString(5);
                                String optionName = dataValueCursor.getString(6);
                                if (!isEmpty(optionCode) && !isEmpty(optionName))
                                    value = useCode ? optionCode : optionName; //If de has optionSet then check if value should be code or name for program rules
                                dataValues.add(RuleDataValue.create(eventDateV, programStage,
                                        dataElement, value));
                                dataValueCursor.moveToNext();
                            }
                        }
                    }

                    return RuleEvent.builder()
                            .event(eventUid)
                            .programStage(programStageUid)
                            .programStageName(programStageName)
                            .status(status)
                            .eventDate(eventDate)
                            .dueDate(dueDate)
                            .organisationUnit(orgUnit)
                            .organisationUnitCode(orgUnitCode)
                            .dataValues(dataValues)
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

    @Nonnull
    private String getOrgUnitCode(String orgUnitUid) {
        String ouCode = "";
        try (Cursor cursor = briteDatabase.query("SELECT code FROM OrganisationUnit WHERE uid = ? LIMIT 1", orgUnitUid)) {
            if (cursor.moveToFirst() && cursor.getString(0) != null)
                ouCode = cursor.getString(0);
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