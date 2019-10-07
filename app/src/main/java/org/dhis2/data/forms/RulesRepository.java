package org.dhis2.data.forms;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.constant.Constant;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.android.core.program.ProgramRule;
import org.hisp.dhis.android.core.program.ProgramRuleAction;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramRuleVariable;
import org.hisp.dhis.android.core.program.ProgramRuleVariableSourceType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Single;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;


public final class RulesRepository {

    private final D2 d2;

    public RulesRepository(@NonNull BriteDatabase briteDatabase, @NonNull D2 d2) {
        this.d2 = d2;
    }

    @NonNull
    public Single<List<Rule>> rulesNew(@NonNull String programUid) {
        Timber.tag("PROGRAMRULEREPOSITORY").d("INIT RULES NEW %s", Thread.currentThread().getName());
        return queryRules(programUid)
                .map(this::translateToRules);
    }

    @NonNull
    public Single<List<RuleVariable>> ruleVariables(@NonNull String programUid) {
        Timber.tag("PROGRAMRULEREPOSITORY").d("INIT RULES VARIABLES %s", Thread.currentThread().getName());
        return d2.programModule().programRuleVariables.byProgramUid().eq(programUid).get()
                .map(this::translateToRuleVariable);
    }

    private List<RuleVariable> translateToRuleVariable(List<ProgramRuleVariable> programRuleVariables) {
        List<RuleVariable> ruleVariables = new ArrayList<>();
        for (ProgramRuleVariable programRuleVariable : programRuleVariables) {
            String attribute = programRuleVariable.trackedEntityAttribute() != null ? programRuleVariable.trackedEntityAttribute().uid() : null;
            String de = programRuleVariable.dataElement() != null ? programRuleVariable.dataElement().uid() : null;
            if ((de != null && d2.dataElementModule().dataElements.uid(de).blockingExists()) ||
                    (attribute != null && d2.trackedEntityModule().trackedEntityAttributes.uid(attribute).blockingExists()))
                ruleVariables.add(
                        translateToRuleVariable(programRuleVariable)
                );
        }
        Timber.tag("PROGRAMRULEREPOSITORY").d("FINISHED RULES VARIABLES");

        return ruleVariables;
    }

    @NonNull
    public Single<List<RuleVariable>> ruleVariablesProgramStages(@NonNull String programUid) {
        return d2.programModule().programRuleVariables.byProgramUid().eq(programUid).get()
                .toFlowable().flatMapIterable(list -> list)
                .map(this::mapToRuleVariableProgramStages)
                .toList();
    }

    @NonNull
    private RuleVariable mapToRuleVariableProgramStages(@NonNull ProgramRuleVariable programRuleVariable) {
        String name = programRuleVariable.name();
        String stage = programRuleVariable.programStage() != null ? programRuleVariable.programStage().uid() : null;
        ProgramRuleVariableSourceType sourceType = programRuleVariable.programRuleVariableSourceType();
        String dataElement = programRuleVariable.dataElement() != null ? programRuleVariable.dataElement().uid() : null;
        String attribute = programRuleVariable.trackedEntityAttribute() != null ? programRuleVariable.trackedEntityAttribute().uid() : null;

        // Mime types of the attribute and data element.
        ValueType valueType = null;
        if (attribute != null)
            valueType = d2.trackedEntityModule().trackedEntityAttributes.uid(attribute).blockingGet().valueType();
        else if (dataElement != null)
            valueType = d2.dataElementModule().dataElements.uid(dataElement).blockingGet().valueType();

        // String representation of value type.
        RuleValueType mimeType = convertType(valueType != null ? valueType : ValueType.TEXT);

        if (sourceType != null)
            switch (sourceType) {
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
        else
            throw new NullPointerException("SouceType can't be null");
    }

    @NonNull
    public Single<Map<String, String>> queryConstants() {
        Timber.tag("PROGRAMRULEREPOSITORY").d("INIT CONSTANTS at %s", Thread.currentThread().getName());
        return d2.constantModule().constants.get()
                .map(constants -> {
                    Map<String, String> constantsMap = new HashMap<>();
                    for (Constant constant : constants) {
                        constantsMap.put(constant.uid(), Objects.requireNonNull(constant.value()).toString());
                    }
                    Timber.tag("PROGRAMRULEREPOSITORY").d("FINISHED CONSTANTS at %s", Thread.currentThread().getName());
                    return constantsMap;
                });
    }

    @NonNull
    private Single<List<ProgramRule>> queryRules(
            @NonNull String programUid) {
        return d2.programModule().programRules
                .byProgramUid().eq(programUid)
                .withProgramRuleActions()
                .get();

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
        Timber.tag("PROGRAMRULEREPOSITORY").d("FINISH RULES NEW");
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
        String attributeType = attribute != null ? d2.trackedEntityModule().trackedEntityAttributes.uid(attribute).blockingGet().valueType().name() : null;
        String elementType = dataElement != null ? d2.dataElementModule().dataElements.uid(dataElement).blockingGet().valueType().name() : null;

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
    private static RuleValueType convertType(@NonNull ValueType valueType) {
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


    public Single<List<RuleEvent>> otherEvents(String eventUidToEvaluate) {
        return d2.eventModule().events.uid(eventUidToEvaluate).get()
                .flatMap(eventToEvaluate ->
                        getOtherEventList(eventToEvaluate).toFlowable()
                                .flatMapIterable(eventList -> eventList)
                                /*.filter(event ->
                                        event.eventDate().before(eventToEvaluate.eventDate()) ||
                                                event.eventDate() == eventToEvaluate.eventDate() && event.lastUpdated().before(eventToEvaluate.lastUpdated())
                                )*/
                                .map(event -> RuleEvent.builder()
                                        .event(event.uid())
                                        .programStage(event.programStage())
                                        .programStageName(d2.programModule().programStages.uid(event.programStage()).blockingGet().name())
                                        .status(event.status() == EventStatus.VISITED ? RuleEvent.Status.ACTIVE : RuleEvent.Status.valueOf(event.status().name()))
                                        .eventDate(event.eventDate())
                                        .dueDate(event.dueDate() != null ? event.dueDate() : event.eventDate())
                                        .organisationUnit(event.organisationUnit())
                                        .organisationUnitCode(d2.organisationUnitModule().organisationUnits.uid(event.organisationUnit()).blockingGet().code())
                                        .dataValues(translateToRuleDataValue(event))
                                        .build())
                                .toList()
                );
    }

    private List<RuleDataValue> translateToRuleDataValue(Event event) {
        List<RuleDataValue> ruleDataValues = new ArrayList<>();
        if (event.trackedEntityDataValues() != null)
            for (TrackedEntityDataValue dataValue : event.trackedEntityDataValues()) {
                DataElement dataElement = d2.dataElementModule().dataElements.uid(dataValue.dataElement()).blockingGet();
                String value = dataValue.value();
                if (!isEmpty(dataElement.optionSetUid())) {
                    boolean useOptionCode = d2.programModule().programRuleVariables.byProgramUid().eq(event.program()).byDataElementUid().eq(dataValue.dataElement())
                            .byUseCodeForOptionSet().isTrue().blockingIsEmpty();
                    if (!useOptionCode)
                        value = d2.optionModule().options.byOptionSetUid().eq(dataElement.optionSetUid()).byCode().eq(value).one().blockingGet().name();
                }
                ruleDataValues.add(
                        RuleDataValue.create(event.eventDate(), event.programStage(), dataValue.dataElement(), value)
                );
            }
        return ruleDataValues;
    }

    private Single<List<Event>> getOtherEventList(Event eventToEvaluate) {
        if (!isEmpty(eventToEvaluate.enrollment()))
            return d2.eventModule().events.byProgramUid().eq(eventToEvaluate.program())
                    .byEnrollmentUid().eq(eventToEvaluate.enrollment())
                    .byUid().notIn(eventToEvaluate.uid())
                    .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
                    .withTrackedEntityDataValues()
                    .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                    .get();
        else
            return d2.eventModule().events
                    .byUid().notIn(eventToEvaluate.uid())
                    .byProgramUid().eq(eventToEvaluate.program())
                    .byProgramStageUid().eq(eventToEvaluate.programStage())
                    .byOrganisationUnitUid().eq(eventToEvaluate.organisationUnit())
                    .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
                    .withTrackedEntityDataValues()
                    .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                    .get().map(list -> {
                        if (list.size() > 10)
                            return list.subList(0, 10);
                        else return list;
                    });
    }


    public Single<List<RuleEvent>> enrollmentEvents(String enrollmentUid) {
        return d2.eventModule().events.byEnrollmentUid().eq(enrollmentUid)
                .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
                .withTrackedEntityDataValues()
                .get()
                .toFlowable().flatMapIterable(events -> events)
                .map(event -> RuleEvent.builder()
                        .event(event.uid())
                        .programStage(event.programStage())
                        .programStageName(d2.programModule().programStages.uid(event.programStage()).blockingGet().name())
                        .status(event.status() == EventStatus.VISITED ? RuleEvent.Status.ACTIVE : RuleEvent.Status.valueOf(event.status().name()))
                        .eventDate(event.eventDate())
                        .dueDate(event.dueDate() != null ? event.dueDate() : event.eventDate())
                        .organisationUnit(event.organisationUnit())
                        .organisationUnitCode(d2.organisationUnitModule().organisationUnits.uid(event.organisationUnit()).blockingGet().code())
                        .dataValues(translateToRuleDataValue(event))
                        .build()).toList();
    }

    public Single<RuleEnrollment> enrollment(String eventUid) {
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> {
                    Timber.tag("PROGRAMRULEREPOSITORY").d("INIT ENROLLMENT in %s", Thread.currentThread().getName());
                    String ouCode = d2.organisationUnitModule().organisationUnits.uid(event.organisationUnit()).blockingGet().code();
                    String programName = d2.programModule().programs.uid(event.program()).blockingGet().name();
                    if (event.enrollment() == null)
                        return Single.just(
                                RuleEnrollment.create("",
                                        Calendar.getInstance().getTime(),
                                        Calendar.getInstance().getTime(),
                                        RuleEnrollment.Status.CANCELLED,
                                        event.organisationUnit(),
                                        ouCode,
                                        new ArrayList<>(),
                                        programName));
                    else
                        return d2.enrollmentModule().enrollments
                                .uid(event.enrollment()).get()
                                .map(enrollment -> RuleEnrollment.create(enrollment.uid(),
                                        enrollment.enrollmentDate(),
                                        enrollment.incidentDate() != null ? enrollment.incidentDate() : new Date(),
                                        RuleEnrollment.Status.valueOf(enrollment.status().name()),
                                        event.organisationUnit(),
                                        ouCode,
                                        getAttributesValues(enrollment),
                                        programName));
                }).doOnSuccess(ruleEnrollment ->
                        Timber.tag("PROGRAMRULEREPOSITORY").d("FINISHED ENROLLMENT in %s", Thread.currentThread().getName())
                );
    }

    private List<RuleAttributeValue> getAttributesValues(Enrollment enrollment) {
        List<TrackedEntityAttributeValue> attributeValues = d2.trackedEntityModule().trackedEntityAttributeValues
                .byTrackedEntityInstance().eq(enrollment.trackedEntityInstance()).blockingGet();
        List<RuleAttributeValue> ruleAttributeValues = new ArrayList<>();
        for (TrackedEntityAttributeValue attributeValue : attributeValues) {
            TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes.uid(attributeValue.trackedEntityAttribute()).blockingGet();
            String value = attributeValue.value();
            if (attribute.optionSet() != null && !isEmpty(attribute.optionSet().uid())) {
                boolean useOptionCode = d2.programModule().programRuleVariables.byProgramUid().eq(enrollment.program()).byTrackedEntityAttributeUid().eq(attribute.uid())
                        .byUseCodeForOptionSet().isTrue().blockingIsEmpty();
                if (!useOptionCode)
                    value = d2.optionModule().options.byOptionSetUid().eq(attribute.optionSet().uid()).byCode().eq(value).one().blockingGet().name();
            }
            RuleAttributeValue.create(attributeValue.trackedEntityAttribute(), value);
        }
        return ruleAttributeValues;
    }

    @NonNull
    public Single<Map<String, List<String>>> getSuplementaryData() {
        Timber.tag("PROGRAMRULEREPOSITORY").d("INIT SUPPLEM %s", Thread.currentThread().getName());

        return Single.fromCallable(() -> {
            Map<String, List<String>> supData = new HashMap<>();

            //ORG UNIT GROUPS
            for (OrganisationUnitGroup ouGroup : d2.organisationUnitModule().organisationUnitGroups.blockingGet())
                if (ouGroup.code() != null)
                    supData.put(ouGroup.code(), new ArrayList<>());

            for (OrganisationUnit ou : d2.organisationUnitModule().organisationUnits.withOrganisationUnitGroups().blockingGet()) {
                if (ou.organisationUnitGroups() != null) {
                    for (OrganisationUnitGroup ouGroup : ou.organisationUnitGroups()) {
                        List<String> groupOUs = supData.get(ouGroup.code());
                        if (groupOUs != null && !groupOUs.contains(ou.uid()))
                            groupOUs.add(ou.uid());
                    }
                }
            }

            //USER ROLES
            List<String> userRoleUids = UidsHelper.getUidsList(d2.userModule().userRoles.blockingGet());
            supData.put("USER", userRoleUids);
            Timber.tag("PROGRAMRULEREPOSITORY").d("FINISHED SUPPLEM");

            return supData;
        });
    }
}