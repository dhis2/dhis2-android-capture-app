package org.dhis2.data.forms;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.constant.Constant;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramRule;
import org.hisp.dhis.android.core.program.ProgramRuleAction;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramRuleVariable;
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
import org.hisp.dhis.rules.models.RuleActionShowOptionGroup;
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

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;


@SuppressWarnings("PMD")
public final class RulesRepository {
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
    private final D2 d2;
    private int count;

    public RulesRepository(@NonNull BriteDatabase briteDatabase, @NonNull D2 d2) {
        this.briteDatabase = briteDatabase;
        this.d2 = d2;
    }

    @NonNull
    public Flowable<List<Rule>> rulesNew(@NonNull String programUid) {
        return queryRules(programUid)
                .map(this::translateToRules);
    }

    @NonNull
    public Flowable<List<RuleVariable>> ruleVariables(@NonNull String programUid) {
        return Flowable.fromCallable(() -> d2.programModule().programRuleVariables.byProgramUid().eq(programUid).get()).map(programRuleVariables -> this.translateToRuleVariable(programRuleVariables));
        /*return briteDatabase.createQuery(ProgramRuleVariableModel.TABLE, QUERY_VARIABLES, programUid)
                .mapToList(RulesRepository::mapToRuleVariable).toFlowable(BackpressureStrategy.LATEST);*/
    }

    private List<RuleVariable> translateToRuleVariable(List<ProgramRuleVariable> programRuleVariables) {
        List<RuleVariable> ruleVariables = new ArrayList<>();
        for (ProgramRuleVariable programRuleVariable : programRuleVariables) {
            ruleVariables.add(
                    translateToRuleVariable(programRuleVariable)
            );
        }
        return ruleVariables;
    }

    @NonNull
    public Flowable<List<RuleVariable>> ruleVariablesProgramStages(@NonNull String programUid) {
        return briteDatabase.createQuery(ProgramRuleVariableModel.TABLE, QUERY_VARIABLES, programUid)
                .mapToList(RulesRepository::mapToRuleVariableProgramStages).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    public Flowable<Map<String, String>> queryConstants() {
        return Flowable.fromCallable(() -> d2.constantModule().constants.get())
                .map(constants -> {
                    Map<String, String> constantsMap = new HashMap<>();
                    for (Constant constant : constants) {
                        constantsMap.put(constant.uid(), Objects.requireNonNull(constant.value()).toString());
                    }
                    return constantsMap;
                });
    }

    @NonNull
    private Flowable<List<ProgramRule>> queryRules(
            @NonNull String programUid) {
        return Flowable.fromCallable(() -> d2.programModule().programRules
                .byProgramUid().eq(programUid)
                .withProgramRuleActions()
                .get());

    }

    private List<Rule> translateToRules(List<ProgramRule> programRules) {
        List<Rule> rules = new ArrayList<>();
        for (ProgramRule programRule : programRules) {
            rules.add(
                    Rule.create(
                            programRule.programStage() != null ? programRule.programStage().uid() : null,
                            programRule.priority(),
                            programRule.condition() != null ? programRule.condition() : "",
                            translateToActions(programRule.programRuleActions()),
                            programRule.name())
            );
        }
        return rules;
    }

    private List<RuleAction> translateToActions(List<ProgramRuleAction> actionList) {
        List<RuleAction> ruleActions = new ArrayList<>();
        for (ProgramRuleAction programRuleAction : actionList) {
            RuleAction ruleAction;
            String dataElement = programRuleAction.dataElement() != null ?
                    programRuleAction.dataElement().uid() : null;
            String attribute = programRuleAction.trackedEntityAttribute() != null ?
                    programRuleAction.trackedEntityAttribute().uid() : null;
            String field = dataElement != null ? dataElement : attribute;
            if (field == null)
                field = "";

            switch (programRuleAction.programRuleActionType()) {
                case HIDEFIELD:
                    ruleAction = RuleActionHideField.create(programRuleAction.content(), field);
                    break;
                case ASSIGN:
                    ruleAction = RuleActionAssign.create(programRuleAction.content(),
                            programRuleAction.data(),
                            field);
                    break;
                case SHOWERROR:
                    ruleAction = RuleActionShowError.create(programRuleAction.content(),
                            programRuleAction.data(), field);
                    break;
                case HIDEOPTION:
                    ruleAction = RuleActionHideOption.create(programRuleAction.content(),
                            programRuleAction.option().uid(), field);
                    break;
                case CREATEEVENT:
                    ruleAction = RuleActionCreateEvent.create(programRuleAction.content(),
                            programRuleAction.data(), programRuleAction.programStage().uid());
                    break;
                case DISPLAYTEXT:
                    ruleAction = RuleActionDisplayText.createForFeedback(programRuleAction.content(),
                            programRuleAction.data());
                    break;
                case HIDESECTION:
                    ruleAction = RuleActionHideSection.create(programRuleAction.programStageSection().uid());
                    break;
                case SHOWWARNING:
                    ruleAction = RuleActionShowWarning.create(programRuleAction.content(),
                            programRuleAction.data(), field);
                    break;
                case ERRORONCOMPLETE:
                    ruleAction = RuleActionErrorOnCompletion.create(programRuleAction.content(),
                            programRuleAction.data(), field);
                    break;
                case HIDEOPTIONGROUP:
                    ruleAction = RuleActionHideOptionGroup.create(programRuleAction.content(),
                            programRuleAction.optionGroup().uid());
                    break;
                case HIDEPROGRAMSTAGE:
                    ruleAction = RuleActionHideProgramStage.create(programRuleAction.programStage().uid());
                    break;
                case SETMANDATORYFIELD:
                    ruleAction = RuleActionSetMandatoryField.create(field);
                    break;
                case WARNINGONCOMPLETE:
                    ruleAction = RuleActionWarningOnCompletion.create(programRuleAction.content(),
                            programRuleAction.data(), field);
                    break;
                case DISPLAYKEYVALUEPAIR:
                    ruleAction = RuleActionDisplayKeyValuePair.createForIndicators(programRuleAction.content(),
                            programRuleAction.data());
                    break;
                case SHOWOPTIONGROUP:
                    ruleAction = RuleActionShowOptionGroup.create(programRuleAction.content(), programRuleAction.optionGroup().uid(), field);
                    break;
                case SENDMESSAGE:
                case SCHEDULEMESSAGE:
                default:
                    String content = programRuleAction.content() != null ? programRuleAction.content() : "unsupported";
                    String ruleType = programRuleAction.programRuleActionType().name();
                    ruleAction = RuleActionUnsupported.create(content, ruleType);
                    break;
            }
            ruleActions.add(ruleAction);
        }
        return ruleActions;
    }

    private RuleVariable translateToRuleVariable(ProgramRuleVariable programRuleVariable) {
        String name = programRuleVariable.name();
        String stage = programRuleVariable.programStage() != null ? programRuleVariable.programStage().uid() : null;
        String sourceType = programRuleVariable.programRuleVariableSourceType().name();
        String dataElement = programRuleVariable.dataElement() != null ? programRuleVariable.dataElement().uid() : null;
        String attribute = programRuleVariable.trackedEntityAttribute() != null ? programRuleVariable.trackedEntityAttribute().uid() : null;

        // Mime types of the attribute and data element.
        String attributeType = attribute != null ? d2.trackedEntityModule().trackedEntityAttributes.uid(attribute).get().valueType().name() : null;
        String elementType = dataElement != null ? d2.dataElementModule().dataElements.uid(dataElement).get().valueType().name() : null;

        // String representation of value type.
        RuleValueType mimeType = null;

        switch (ProgramRuleVariableSourceType.valueOf(sourceType)) {
            case TEI_ATTRIBUTE:
                if (!isEmpty(attributeType))
                    mimeType = convertType(attributeType);
                break;
            case DATAELEMENT_CURRENT_EVENT:
            case DATAELEMENT_PREVIOUS_EVENT:
            case DATAELEMENT_NEWEST_EVENT_PROGRAM:
            case DATAELEMENT_NEWEST_EVENT_PROGRAM_STAGE:
                if (!isEmpty(elementType))
                    mimeType = convertType(elementType);
                break;
            default:
                break;
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
        String attributeType = cursor.getString(6);
        String elementType = cursor.getString(5);

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
                if (location != null)
                    return createDisplayTextAction(content, data, location);
            case DISPLAYKEYVALUEPAIR:
                if (location != null)
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
                if (content == null && data == null)
                    content = "This field has an error.";
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
                return RuleActionHideOption.create(content, option, isEmpty(attribute) ? field : attribute);
            case HIDEOPTIONGROUP:
                return RuleActionHideOptionGroup.create(content, optionGroup);
            case SHOWOPTIONGROUP:
                return RuleActionShowOptionGroup.create(content, optionGroup, isEmpty(attribute) ? field : attribute);
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
                                            DateUtils.databaseDateFormat().format(eventModel.eventDate() != null ? eventModel.eventDate() : eventModel.dueDate()),
                                            DateUtils.databaseDateFormat().format(eventModel.eventDate() != null ? eventModel.eventDate() : eventModel.dueDate()),
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
                                                        RuleEvent.Status.valueOf(cursor.getString(2));

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
                                                calendar.add(Calendar.SECOND, count);
                                                eventDate = calendar.getTime();
                                                calendar.setTime(dueDate);
                                                calendar.add(Calendar.SECOND, count);
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

    @NonNull
    public Flowable<Map<String, List<String>>> getSuplementaryData(D2 d2) {

        return Flowable.fromCallable(() -> {
            Map<String, List<String>> supData = new HashMap<>();

            //ORG UNIT GROUPS
            for (OrganisationUnitGroup ouGroup : d2.organisationUnitModule().organisationUnitGroups.get())
                if (ouGroup.code() != null)
                    supData.put(ouGroup.code(), new ArrayList<>());

            for (OrganisationUnit ou : d2.organisationUnitModule().organisationUnits.withOrganisationUnitGroups().get()) {
                if (ou.organisationUnitGroups() != null) {
                    for (OrganisationUnitGroup ouGroup : ou.organisationUnitGroups()) {
                        List<String> groupOUs = supData.get(ouGroup.code());
                        if (groupOUs != null && !groupOUs.contains(ou.uid()))
                            groupOUs.add(ou.uid());
                    }
                }
            }

            //USER ROLES
            List<String> userRoleUids = UidsHelper.getUidsList(d2.userModule().userRoles.get());
            supData.put("USER", userRoleUids);

            return supData;
        });
    }
}