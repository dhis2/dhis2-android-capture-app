package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.R;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.forms.dataentry.fields.image.ImageViewModel;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.ObjectWithUid;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRenderingModel;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramRule;
import org.hisp.dhis.android.core.program.ProgramRuleAction;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramRuleVariable;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.program.ProgramStageSectionDeviceRendering;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionRendering;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.program.ProgramType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.rules.models.Rule;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureRepositoryImpl implements EventCaptureContract.EventCaptureRepository {

    private final FieldViewModelFactory fieldFactory;

    private static final List<String> SECTION_TABLES = Arrays.asList(
            EventModel.TABLE, ProgramModel.TABLE, ProgramStageModel.TABLE, ProgramStageSectionModel.TABLE);
    private static final String SELECT_SECTIONS = "SELECT\n" +
            "  Program.uid AS programUid,\n" +
            "  ProgramStage.uid AS programStageUid,\n" +
            "  ProgramStageSection.uid AS programStageSectionUid,\n" +
            "  ProgramStageSection.displayName AS programStageSectionDisplayName,\n" +
            "  ProgramStage.displayName AS programStageDisplayName,\n" +
            "  ProgramStageSection.mobileRenderType AS renderType\n" +
            "FROM Event\n" +
            "  JOIN Program ON Event.program = Program.uid\n" +
            "  JOIN ProgramStage ON Event.programStage = ProgramStage.uid\n" +
            "  LEFT OUTER JOIN ProgramStageSection ON ProgramStageSection.programStage = Event.programStage\n" +
            "WHERE Event.uid = ?\n" +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' ORDER BY ProgramStageSection.sortOrder";

    private static final String QUERY = "SELECT\n" +
            "  Field.id,\n" +
            "  Field.label,\n" +
            "  Field.type,\n" +
            "  Field.mandatory,\n" +
            "  Field.optionSet,\n" +
            "  Value.value,\n" +
            "  Option.displayName,\n" +
            "  Field.section,\n" +
            "  Field.allowFutureDate,\n" +
            "  Event.status,\n" +
            "  Field.formLabel,\n" +
            "  Field.displayDescription,\n" +
            "  Field.formOrder,\n" +
            "  Field.sectionOrder\n" +
            "FROM Event\n" +
            "  LEFT OUTER JOIN (\n" +
            "      SELECT\n" +
            "        DataElement.displayName AS label,\n" +
            "        DataElement.valueType AS type,\n" +
            "        DataElement.uid AS id,\n" +
            "        DataElement.optionSet AS optionSet,\n" +
            "        DataElement.displayFormName AS formLabel,\n" +
            "        ProgramStageDataElement.sortOrder AS formOrder,\n" +
            "        ProgramStageDataElement.programStage AS stage,\n" +
            "        ProgramStageDataElement.compulsory AS mandatory,\n" +
            "        ProgramStageSectionDataElementLink.programStageSection AS section,\n" +
            "        ProgramStageDataElement.allowFutureDate AS allowFutureDate,\n" +
            "        DataElement.displayDescription AS displayDescription,\n" +
            "        ProgramStageSectionDataElementLink.sortOrder AS sectionOrder\n" + //This should override dataElement formOrder
            "      FROM ProgramStageDataElement\n" +
            "        INNER JOIN DataElement ON DataElement.uid = ProgramStageDataElement.dataElement\n" +
            "        LEFT JOIN ProgramStageSection ON ProgramStageSection.programStage = ProgramStageDataElement.programStage\n" +
            "        LEFT JOIN ProgramStageSectionDataElementLink ON ProgramStageSectionDataElementLink.programStageSection = ProgramStageSection.uid AND ProgramStageSectionDataElementLink.dataElement = DataElement.uid\n" +
            "    ) AS Field ON (Field.stage = Event.programStage)\n" +
            "  LEFT OUTER JOIN TrackedEntityDataValue AS Value ON (\n" +
            "    Value.event = Event.uid AND Value.dataElement = Field.id\n" +
            "  )\n" +
            "  LEFT OUTER JOIN Option ON (\n" +
            "    Field.optionSet = Option.optionSet AND Value.value = Option.code\n" +
            "  )\n" +
            " %s  " +
            "ORDER BY CASE" +
            " WHEN Field.sectionOrder IS NULL THEN Field.formOrder" +
            " WHEN Field.sectionOrder IS NOT NULL THEN Field.sectionOrder" +
            " END ASC;";

    private static final String OPTIONS = "SELECT Option.uid, Option.displayName, Option.code FROM Option WHERE Option.optionSet = ?";

    private final BriteDatabase briteDatabase;
    private final String eventUid;
    private final Event currentEvent;
    private final FormRepository formRepository;
    private final D2 d2;
    private final boolean isEventEditable;
    private final HashMap<String, ProgramStageSection> sectionMap;
    private final HashMap<String, ProgramStageDataElement> stageDataElementsMap;
    private String lastUpdatedUid;
    private RuleEvent.Builder eventBuilder;
    private Map<String, List<Rule>> dataElementRules = new HashMap<>();
    private List<ProgramRule> mandatoryRules;
    private List<ProgramRule> rules;
    private Map<String, List<FieldViewModel>> sectionFields;

    public EventCaptureRepositoryImpl(Context context, BriteDatabase briteDatabase, FormRepository formRepository, String eventUid, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;
        this.formRepository = formRepository;
        this.d2 = d2;

        currentEvent = d2.eventModule().events.uid(eventUid).withAllChildren().get();
        ProgramStage programStage = d2.programModule().programStages.uid(currentEvent.programStage()).withAllChildren().get();
        OrganisationUnit ou = d2.organisationUnitModule().organisationUnits.uid(currentEvent.organisationUnit()).withAllChildren().get();

        eventBuilder = RuleEvent.builder()
                .event(currentEvent.uid())
                .programStage(currentEvent.programStage())
                .programStageName(programStage.displayName())
                .status(RuleEvent.Status.valueOf(currentEvent.status().name()))
                .eventDate(currentEvent.eventDate())
                .dueDate(currentEvent.dueDate() != null ? currentEvent.dueDate() : currentEvent.eventDate())
                .organisationUnit(currentEvent.organisationUnit())
                .organisationUnitCode(ou.code());

        fieldFactory = new FieldViewModelFactoryImpl(
                context.getString(R.string.enter_text),
                context.getString(R.string.enter_long_text),
                context.getString(R.string.enter_number),
                context.getString(R.string.enter_integer),
                context.getString(R.string.enter_positive_integer),
                context.getString(R.string.enter_negative_integer),
                context.getString(R.string.enter_positive_integer_or_zero),
                context.getString(R.string.filter_options),
                context.getString(R.string.choose_date));

        isEventEditable = isEventExpired(eventUid);

        loadDataElementRules(currentEvent);

        List<ProgramStageSection> sections = d2.programModule().programStageSections.byProgramStageUid().eq(programStage.uid())
                .withAllChildren().get();
        sectionMap = new HashMap<>();
        if (sections != null && !sections.isEmpty()) {
            for (ProgramStageSection section : sections)
                sectionMap.put(section.uid(), section);
        }

        stageDataElementsMap = new HashMap<>();
        for (ProgramStageDataElement psDe : programStage.programStageDataElements()) {
            stageDataElementsMap.put(psDe.dataElement().uid(), psDe);
        }

        sectionFields = new HashMap<>();
    }

    private void loadDataElementRules(Event event) {
        float init = System.currentTimeMillis();
        rules = d2.programModule().programRules.byProgramUid().eq(event.program()).withAllChildren().get();
        Timber.d("LOAD ALL RULES  AT %s", System.currentTimeMillis() - init);
        mandatoryRules = new ArrayList<>();
        Iterator<ProgramRule> ruleIterator = rules.iterator();
        while (ruleIterator.hasNext()) {
            ProgramRule rule = ruleIterator.next();
            if (rule.programStage() != null && !rule.programStage().uid().equals(event.programStage()))
                ruleIterator.remove();
            else if (rule.condition() == null)
                ruleIterator.remove();
            else
                for (ProgramRuleAction action : rule.programRuleActions())
                    if (action.programRuleActionType() == ProgramRuleActionType.HIDEFIELD ||
                            action.programRuleActionType() == ProgramRuleActionType.HIDESECTION ||
                            action.programRuleActionType() == ProgramRuleActionType.ASSIGN ||
                            action.programRuleActionType() == ProgramRuleActionType.SHOWWARNING ||
                            action.programRuleActionType() == ProgramRuleActionType.SHOWERROR ||
                            action.programRuleActionType() == ProgramRuleActionType.HIDEOPTIONGROUP ||
                            action.programRuleActionType() == ProgramRuleActionType.HIDEOPTION ||
                            action.programRuleActionType() == ProgramRuleActionType.DISPLAYKEYVALUEPAIR ||
                            action.programRuleActionType() == ProgramRuleActionType.DISPLAYTEXT ||
                            action.programRuleActionType() == ProgramRuleActionType.SETMANDATORYFIELD)
                        if (!mandatoryRules.contains(rule))
                            mandatoryRules.add(rule);
        }

        List<ProgramRuleVariable> variables = d2.programModule().programRuleVariables
                .byProgramUid().eq(event.program())
                .withAllChildren().get();

        Iterator<ProgramRuleVariable> variableIterator = variables.iterator();
        while (variableIterator.hasNext()) {
            ProgramRuleVariable variable = variableIterator.next();
            if (variable.programStage() != null && variable.programStage().uid().equals(event.programStage()))
                variableIterator.remove();
            else if (variable.dataElement() == null)
                variableIterator.remove();
        }

        List<Rule> finalMandatoryRules = trasformToRule(mandatoryRules);
        for (ProgramRuleVariable variable : variables) {
            if (variable.dataElement() != null && !dataElementRules.containsKey(variable.dataElement().uid()))
                dataElementRules.put(variable.dataElement().uid(), finalMandatoryRules);
            for (ProgramRule rule : rules) {
                if (rule.condition().contains(variable.displayName()) || actionsContainsDE(rule.programRuleActions(), variable.displayName())) {
                    if (dataElementRules.get(variable.dataElement().uid()) == null)
                        dataElementRules.put(variable.dataElement().uid(), trasformToRule(mandatoryRules));
                    Rule fRule = trasformToRule(rule);
                    if (!dataElementRules.get(variable.dataElement().uid()).contains(fRule))
                        dataElementRules.get(variable.dataElement().uid()).add(fRule);
                }
            }
        }
        Timber.d("FINISHED DE RULES  AT %s", System.currentTimeMillis() - init);
    }

    private Rule trasformToRule(ProgramRule rule) {
        return Rule.create(
                rule.programStage() != null ? rule.programStage().uid() : null,
                rule.priority(),
                rule.condition(),
                transformToRuleAction(rule.programRuleActions()),
                rule.displayName());
    }


    private List<ProgramRule> filterRules(List<ProgramRule> rules) {
        Program program = d2.programModule().programs.uid(currentEvent.program()).withAllChildren().get();
        if (program.programType().equals(ProgramType.WITH_REGISTRATION)) {
            Iterator<ProgramRule> iterator = rules.iterator();
            while (iterator.hasNext()) {
                if (haveDisplayActionIndicator(iterator.next()))
                    iterator.remove();
            }
        }
        return rules;
    }

    private boolean haveDisplayActionIndicator(ProgramRule programRule) {
        for (ProgramRuleAction programRuleAction : programRule.programRuleActions()) {
            if ((programRuleAction.programRuleActionType() == ProgramRuleActionType.DISPLAYTEXT ||
                    programRuleAction.programRuleActionType() == ProgramRuleActionType.DISPLAYKEYVALUEPAIR)
                    && programRule.programStage() == null)
                return true;
        }
        return false;
    }

    private List<Rule> trasformToRule(List<ProgramRule> rules) {
        filterRules(rules);
        List<Rule> finalRules = new ArrayList<>();
        for (ProgramRule rule : rules) {
            finalRules.add(Rule.create(
                    rule.programStage() != null ? rule.programStage().uid() : null,
                    rule.priority(),
                    rule.condition(),
                    transformToRuleAction(rule.programRuleActions()),
                    rule.displayName()));
        }
        return finalRules;
    }

    private List<RuleAction> transformToRuleAction(List<ProgramRuleAction> programRuleActions) {
        List<RuleAction> ruleActions = new ArrayList<>();
        if (programRuleActions != null)
            for (ProgramRuleAction programRuleAction : programRuleActions)
                ruleActions.add(
                        RulesRepository.create(
                                programRuleAction.programRuleActionType(),
                                programRuleAction.programStage() != null ? programRuleAction.programStage().uid() : null,
                                programRuleAction.programStageSection() != null ? programRuleAction.programStageSection().uid() : null,
                                programRuleAction.trackedEntityAttribute() != null ? programRuleAction.trackedEntityAttribute().uid() : null,
                                programRuleAction.dataElement() != null ? programRuleAction.dataElement().uid() : null,
                                programRuleAction.location(),
                                programRuleAction.content(),
                                programRuleAction.data(),
                                programRuleAction.option() != null ? programRuleAction.option().uid() : null,
                                programRuleAction.optionGroup() != null ? programRuleAction.optionGroup().uid() : null)
                );
        return ruleActions;
    }

    private boolean actionsContainsDE(List<ProgramRuleAction> programRuleActions, String variableName) {
        boolean actionContainsDe = false;
        for (ProgramRuleAction ruleAction : programRuleActions) {
            if (ruleAction.data() != null && ruleAction.data().contains(variableName))
                actionContainsDe = true;

        }
        return actionContainsDe;
    }


    @Override
    public boolean isEnrollmentOpen() {
        Enrollment enrollment = d2.enrollmentModule().enrollments.uid(d2.eventModule().events.uid(eventUid).get().enrollment()).get();
        return enrollment == null || enrollment.status() == EnrollmentStatus.ACTIVE;
    }

    private boolean inOrgUnitRange(String eventUid) {
        Event event = d2.eventModule().events.uid(eventUid).get();
        String orgUnitUid = event.organisationUnit();
        Date eventDate = event.eventDate();
        boolean inRange = true;
        OrganisationUnit orgUnit = d2.organisationUnitModule().organisationUnits.uid(orgUnitUid).get();
        if (eventDate != null && orgUnit.openingDate() != null && eventDate.before(orgUnit.openingDate()))
            inRange = false;
        if (eventDate != null && orgUnit.closedDate() != null && eventDate.after(orgUnit.closedDate()))
            inRange = false;

        return inRange;
    }

    @Override
    public boolean isEnrollmentCancelled() {
        Enrollment enrollment = d2.enrollmentModule().enrollments.uid(d2.eventModule().events.uid(eventUid).get().enrollment()).get();
        if (enrollment == null)
            return false;
        else
            return d2.enrollmentModule().enrollments.uid(d2.eventModule().events.uid(eventUid).get().enrollment()).get().status() == EnrollmentStatus.CANCELLED;
    }

    @Override
    public boolean isEventExpired(String eventUid) {
        Event event = d2.eventModule().events.uid(eventUid).withAllChildren().get();
        Program program = d2.programModule().programs.uid(event.program()).withAllChildren().get();
        ProgramStage stage = d2.programModule().programStages.uid(event.programStage()).get();
        boolean isExpired = DateUtils.getInstance().isEventExpired(event.eventDate(), event.completedDate(), event.status(), program.completeEventsExpiryDays(), stage.periodType() != null ? stage.periodType() : program.expiryPeriodType(), program.expiryDays());
        boolean blockAfterComplete = event.status() == EventStatus.COMPLETED && stage.blockEntryForm();
        boolean isInCaptureOrgUnit = d2.organisationUnitModule().organisationUnits
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(event.organisationUnit()).one().exists();

        boolean editable = isEnrollmentOpen() && !blockAfterComplete && !isExpired &&
                getAccessDataWrite() && inOrgUnitRange(eventUid) && isInCaptureOrgUnit;

        return !editable;
    }

    @Override
    public Flowable<String> programStageName() {
        return Flowable.just(d2.eventModule().events.uid(eventUid).get())
                .map(event -> d2.programModule().programStages.uid(event.programStage()).get().displayName());
    }

    @Override
    public Flowable<String> eventDate() {
        return Flowable.just(d2.eventModule().events.uid(eventUid).get())
                .map(event -> DateUtils.uiDateFormat().format(event.eventDate()));
    }

    @Override
    public Flowable<String> orgUnit() {
        return Flowable.just(d2.eventModule().events.uid(eventUid).get())
                .map(event -> d2.organisationUnitModule().organisationUnits.uid(event.organisationUnit()).get().displayName());
    }


    @Override
    public Flowable<String> catOption() {
        return Flowable.just(d2.eventModule().events.uid(eventUid).get())
                .map(event -> d2.categoryModule().categoryOptionCombos.uid(event.attributeOptionCombo()))
                .map(categoryOptionComboRepo -> {
                    if (categoryOptionComboRepo.get() == null)
                        return "";
                    else
                        return categoryOptionComboRepo.get().displayName();
                })
                .map(displayName -> displayName.equals("default") ? "" : displayName);
    }

    @Override
    public Flowable<List<FormSectionViewModel>> eventSections() {
        return briteDatabase
                .createQuery(SECTION_TABLES, SELECT_SECTIONS, eventUid)
                .mapToList(this::mapToFormSectionViewModels)
                .distinctUntilChanged().toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list(String sectionUid) {

        if (sectionFields.containsKey(sectionUid))
            return Flowable.just(sectionFields.get(sectionUid))
                    .flatMapIterable(fieldViewModels -> fieldViewModels)
                    .map(fieldViewModel -> {
                        String uid = fieldViewModel.uid();
                        TrackedEntityDataValueObjectRepository valueRepository = d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, uid);

                        String value = null;
                        if (valueRepository.exists()) {
                            value = valueRepository.get().value();

                            if (fieldViewModel instanceof OrgUnitViewModel && !isEmpty(value)) {
                                value = value + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(value).get().displayName();
                            }

                            if ((fieldViewModel instanceof SpinnerViewModel || fieldViewModel instanceof ImageViewModel) && !isEmpty(value)) {
                                value = d2.optionModule().options.byOptionSetUid().eq(fieldViewModel.optionSet()).byCode().eq(value).one().get().displayName();
                            }
                        }
                        boolean editable = fieldViewModel.editable() != null ? fieldViewModel.editable() : true;
                        fieldViewModel = fieldViewModel.withValue(value).withEditMode(editable);

                        return fieldViewModel;
                    }).toList()
                    .map(fieldViewModels -> {
                        sectionFields.put(sectionUid, fieldViewModels);
                        return fieldViewModels;
                    }).toFlowable()
                    .map(this::checkRenderType);
        else
            return d2.programModule().programStageSections.uid(sectionUid)
                    .withAllChildren().getAsync().toFlowable()
                    .map(section -> section.dataElements())
                    .flatMapIterable(UidsHelper::getUidsList)
                    .map(deUid -> {
                        DataElement de = d2.dataElementModule().dataElements.uid(deUid).withAllChildren().get();
                        ProgramStageDataElement stageDataElement = stageDataElementsMap.get(deUid);
                        TrackedEntityDataValueObjectRepository valueRepository = d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, deUid);
                        ProgramStageSection section = sectionMap.get(sectionUid);


                        String uid = de.uid();
                        String displayName = de.displayName();
                        ValueType valueType = de.valueType();
                        boolean mandatory = stageDataElement.compulsory() != null ? stageDataElement.compulsory() : false;
                        String optionSet = de.optionSetUid();
                        String dataValue = valueRepository.exists() ? valueRepository.get().value() : null;
                        boolean allowFurureDates = stageDataElement.allowFutureDate() != null ? stageDataElement.allowFutureDate() : false;
                        String formName = de.displayFormName();
                        String description = de.displayDescription();

                        int optionCount = 0;
                        if (!isEmpty(optionSet)) {
                            if (!isEmpty(dataValue)) {
                                dataValue = d2.optionModule().options.byOptionSetUid().eq(optionSet).byCode().eq(dataValue).one().get().displayName();
                            }
                            optionCount = d2.optionModule().options.byOptionSetUid().eq(optionSet).count();
                        }

                        ValueTypeDeviceRenderingModel fieldRendering = null;
                        //TODO: CHANGE

                        ObjectStyleModel objectStyle = ObjectStyleModel.builder().build();
                        if (de.style() != null) {
                            objectStyle = ObjectStyleModel.builder()
                                    .objectTable(de.style().objectTable())
                                    .color(de.style().color())
                                    .icon(de.style().icon())
                                    .uid(de.style().uid())
                                    .build();
                        }

                        if (valueType == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
                            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(dataValue).get().displayName();
                        }

                        ProgramStageSectionRenderingType renderingType = getSectionRenderingType(section);

                        return fieldFactory.create(uid, formName == null ? displayName : formName,
                                valueType, mandatory, optionSet, dataValue,
                                section.uid(), allowFurureDates,
                                !isEventEditable,
                                renderingType, description, fieldRendering, optionCount, objectStyle);
                    }).toList().toFlowable()
                    .map(data -> {
                        sectionFields.put(sectionUid, data);
                        return data;
                    })
                    .map(this::checkRenderType);
    }

    private ProgramStageSectionRenderingType renderingType(String sectionUid) {
        ProgramStageSectionRenderingType renderingType = ProgramStageSectionRenderingType.LISTING;
        if (sectionUid != null) {
            ProgramStageSectionRendering sectionRendering = d2.programModule().programStageSections.uid(sectionUid).get().renderType();
            ProgramStageSectionDeviceRendering stageSectionRendering = sectionRendering != null ? sectionRendering.mobile() : null;
            if (stageSectionRendering != null)
                renderingType = stageSectionRendering.type();
        }

        return renderingType;
    }

    private List<FieldViewModel> checkRenderType(List<FieldViewModel> fieldViewModels) {
        ArrayList<FieldViewModel> renderList = new ArrayList<>();

        for (FieldViewModel fieldViewModel : fieldViewModels) {

            ProgramStageSectionRenderingType renderingType = renderingType(fieldViewModel.programStageSection());
            if (!isEmpty(fieldViewModel.optionSet()) && renderingType != ProgramStageSectionRenderingType.LISTING) {
                try (Cursor cursor = briteDatabase.query(OPTIONS, fieldViewModel.optionSet() == null ? "" : fieldViewModel.optionSet())) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int optionCount = cursor.getCount();
                        for (int i = 0; i < optionCount; i++) {
                            String uid = cursor.getString(0);
                            String displayName = cursor.getString(1);
                            String optionCode = cursor.getString(2);

                            ValueTypeDeviceRenderingModel fieldRendering = null;
                            try (Cursor rendering = briteDatabase.query("SELECT ValueTypeDeviceRendering.* FROM ValueTypeDeviceRendering" +
                                    " JOIN ProgramStageDataElement ON ProgramStageDataElement.uid = ValueTypeDeviceRendering.uid" +
                                    " WHERE ProgramStageDataElement.uid = ?", uid)) {
                                if (rendering != null && rendering.moveToFirst())
                                    fieldRendering = ValueTypeDeviceRenderingModel.create(rendering);
                            }

                            ObjectStyleModel objectStyle = ObjectStyleModel.builder().build();
                            try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", uid)) {
                                if (objStyleCursor != null && objStyleCursor.moveToFirst())
                                    objectStyle = ObjectStyleModel.create(objStyleCursor);
                            }

                            renderList.add(fieldFactory.create(
                                    fieldViewModel.uid() + "." + uid, //fist
                                    displayName + "-" + optionCode, ValueType.TEXT, false,
                                    fieldViewModel.optionSet(), fieldViewModel.value(), fieldViewModel.programStageSection(),
                                    fieldViewModel.allowFutureDate(), fieldViewModel.editable() == null ? false : fieldViewModel.editable(), renderingType, fieldViewModel.description(), fieldRendering, optionCount, objectStyle));

                            cursor.moveToNext();
                        }
                    }
                }
            } else
                renderList.add(fieldViewModel);
        }

        return renderList;

    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list() {

        if (sectionFields.containsKey("ALL"))
            return Flowable.just(sectionFields.get("ALL"))
                    .flatMapIterable(fieldViewModels -> fieldViewModels)
                    .map(fieldViewModel -> {
                        String uid = fieldViewModel.uid();
                        TrackedEntityDataValueObjectRepository valueRepository = d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, uid);

                        String value = null;
                        if (valueRepository.exists()) {
                            value = valueRepository.get().value();

                            if (fieldViewModel instanceof OrgUnitViewModel && !isEmpty(value)) {
                                value = value + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(value).get().displayName();
                            }

                            if ((fieldViewModel instanceof SpinnerViewModel || fieldViewModel instanceof ImageViewModel) && !isEmpty(value)) {
                                value = d2.optionModule().options.byOptionSetUid().eq(fieldViewModel.optionSet()).byCode().eq(value).one().get().displayName();
                            }
                        }
                        boolean editable = fieldViewModel.editable() != null ? fieldViewModel.editable() : true;
                        fieldViewModel = fieldViewModel.withValue(value).withEditMode(editable);

                        return fieldViewModel;
                    }).toList()
                    .map(fieldViewModels -> {
                        sectionFields.put("ALL", fieldViewModels);
                        return fieldViewModels;
                    }).toFlowable()
                    .map(this::checkRenderType);
        else return d2.eventModule().events.uid(eventUid).getAsync()
                .flatMap(event ->
                        d2.programModule().programStages.uid(event.programStage()).withAllChildren().getAsync()
                                .map(ProgramStage::programStageDataElements).toFlowable()
                                .flatMapIterable(list -> list)
                                .map(programStageDataElement -> {
                                    DataElement de = d2.dataElementModule().dataElements.uid(programStageDataElement.dataElement().uid()).withAllChildren().get();
                                    TrackedEntityDataValueObjectRepository valueRepository = d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, de.uid());

                                    ProgramStageSection programStageSection = null;
                                    List<ProgramStageSection> sections = d2.programModule().programStageSections.withDataElements().byProgramStageUid().eq(event.programStage()).get();
                                    for (ProgramStageSection section : sections)
                                        if (UidsHelper.getUidsList(section.dataElements()).contains(de.uid())) {
                                            programStageSection = section;
                                            break;
                                        }


                                    String uid = de.uid();
                                    String displayName = de.displayName();
                                    ValueType valueType = de.valueType();
                                    boolean mandatory = programStageDataElement.compulsory() != null ? programStageDataElement.compulsory() : false;
                                    String optionSet = de.optionSetUid();
                                    String dataValue = valueRepository.exists() ? valueRepository.get().value() : null;
                                    boolean allowFurureDates = programStageDataElement.allowFutureDate() != null ? programStageDataElement.allowFutureDate() : false;
                                    String formName = de.displayFormName();
                                    String description = de.displayDescription();

                                    int optionCount = 0;
                                    if (!isEmpty(optionSet)) {
                                        if (!isEmpty(dataValue)) {
                                            dataValue = d2.optionModule().options.byOptionSetUid().eq(optionSet).byCode().eq(dataValue).one().get().displayName();
                                        }
                                        optionCount = d2.optionModule().options.byOptionSetUid().eq(optionSet).count();
                                    }

                                    ValueTypeDeviceRenderingModel fieldRendering = null;
                                    //TODO: CHANGE

                                    ObjectStyleModel objectStyle = ObjectStyleModel.builder().build();
                                    if (de.style() != null) {
                                        objectStyle = ObjectStyleModel.builder()
                                                .objectTable(de.style().objectTable())
                                                .color(de.style().color())
                                                .icon(de.style().icon())
                                                .uid(de.style().uid())
                                                .build();
                                    }

                                    if (valueType == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
                                        dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(dataValue).get().displayName();
                                    }

                                    ProgramStageSectionRenderingType renderingType = getSectionRenderingType(programStageSection);

                                    return fieldFactory.create(uid, formName == null ? displayName : formName,
                                            valueType, mandatory, optionSet, dataValue,
                                            programStageSection != null ? programStageSection.uid() : null, allowFurureDates,
                                            !isEventEditable,
                                            renderingType, description, fieldRendering, optionCount, objectStyle);
                                }).toList()).toFlowable()
                .map(data -> {
                    sectionFields.put("ALL", data);
                    return data;
                })
                .map(this::checkRenderType);
    }

    private ProgramStageSectionRenderingType getSectionRenderingType(ProgramStageSection stageSection) {
        ProgramStageSectionRenderingType renderingType = ProgramStageSectionRenderingType.LISTING;
        if (stageSection != null && stageSection.renderType() != null & stageSection.renderType().mobile() != null)
            renderingType = stageSection.renderType().mobile().type();

        return renderingType;
    }

    @NonNull
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    private String prepareStatement(String sectionUid, String eventUid) {
        String where;
        if (isEmpty(sectionUid)) {
            where = String.format(Locale.US, "WHERE Event.uid = '%s'", eventUid == null ? "" : eventUid);
        } else {
            where = String.format(Locale.US, "WHERE Event.uid = '%s' AND " +
                    "Field.section = '%s'", eventUid == null ? "" : eventUid, sectionUid == null ? "" : sectionUid);
        }

        return String.format(Locale.US, QUERY, where);
    }

    @NonNull
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    private String prepareStatement(String eventUid) {

        StringBuilder sectionUids = new StringBuilder();

        try (Cursor sectionsCursor = briteDatabase.query(SELECT_SECTIONS, eventUid)) {
            if (sectionsCursor != null && sectionsCursor.moveToFirst()) {
                for (int i = 0; i < sectionsCursor.getCount(); i++) {
                    if (sectionsCursor.getString(2) != null)
                        sectionUids.append(String.format("'%s'", sectionsCursor.getString(2)));
                    if (i < sectionsCursor.getCount() - 1)
                        sectionUids.append(",");
                    sectionsCursor.moveToNext();
                }
            }
        }

        String where;
        if (isEmpty(sectionUids)) {
            where = String.format(Locale.US, "WHERE Event.uid = '%s'", eventUid == null ? "" : eventUid);
        } else {
            where = String.format(Locale.US, "WHERE Event.uid = '%s' AND " +
                    "Field.section IN (%s)", eventUid == null ? "" : eventUid, sectionUids);
        }

        return String.format(Locale.US, QUERY, where);
    }

    @NonNull
    private FieldViewModel transform(@NonNull Cursor cursor) {
        String uid = cursor.getString(0);
        String displayName = cursor.getString(1);
        String valueTypeName = cursor.getString(2);
        boolean mandatory = cursor.getInt(3) == 1;
        String optionSet = cursor.getString(4);
        String dataValue = cursor.getString(5);
        String optionCodeName = cursor.getString(6);
        String programStageSection = cursor.getString(7);
        boolean allowFurureDates = cursor.getInt(8) == 1;
        EventStatus eventStatus = EventStatus.valueOf(cursor.getString(9));
        String formName = cursor.getString(10);
        String description = cursor.getString(11);

        if (!isEmpty(optionCodeName)) {
            dataValue = optionCodeName;
        }

        int optionCount = 0;
        if (!isEmpty(optionSet)) {
            try (Cursor countCursor = briteDatabase.query("SELECT COUNT (uid) FROM Option WHERE optionSet = ?", optionSet)) {
                if (countCursor != null && countCursor.moveToFirst())
                    optionCount = countCursor.getInt(0);
            }
        }

        ValueTypeDeviceRenderingModel fieldRendering = null;
        try (Cursor rendering = briteDatabase.query("SELECT ValueTypeDeviceRendering.* FROM ValueTypeDeviceRendering" +
                " JOIN ProgramStageDataElement ON ProgramStageDataElement.uid = ValueTypeDeviceRendering.uid" +
                " WHERE ProgramStageDataElement.dataElement = ? LIMIT 1", uid)) {
            if (rendering != null && rendering.moveToFirst())
                fieldRendering = ValueTypeDeviceRenderingModel.create(rendering);
        }

        ObjectStyleModel objectStyle = ObjectStyleModel.builder().build();
        try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", uid)) {
            if (objStyleCursor != null && objStyleCursor.moveToFirst())
                objectStyle = ObjectStyleModel.create(objStyleCursor);
        }

        if (ValueType.valueOf(valueTypeName) == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(dataValue).get().displayName();
        }

        ProgramStageSectionRenderingType renderingType = renderingType(programStageSection);

        return fieldFactory.create(uid, formName == null ? displayName : formName,
                ValueType.valueOf(valueTypeName), mandatory, optionSet, dataValue,
                programStageSection, allowFurureDates,
                !isEventEditable,
                renderingType, description, fieldRendering, optionCount, objectStyle);
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return queryDataValues(eventUid)
                .switchMap(dataValues ->
                        formRepository.ruleEngine()
                                .flatMap(ruleEngine -> {
                                    if (isEmpty(lastUpdatedUid))
                                        return Flowable.fromCallable(ruleEngine.evaluate(eventBuilder.dataValues(dataValues).build()));
                                    else if (dataElementRules.containsKey(lastUpdatedUid))
                                        return Flowable.fromCallable(ruleEngine.evaluate(eventBuilder.dataValues(dataValues).build(), dataElementRules.get(lastUpdatedUid)));
                                    else
                                        return Flowable.fromCallable(ruleEngine.evaluate(eventBuilder.dataValues(dataValues).build(), trasformToRule(mandatoryRules)));
                                })
                                .map(Result::success)
                )
                .doOnNext(data -> Timber.tag("PROGRAMRULES").d("NEW EFFECTS"))
                .doOnError(error -> Result.failure(new Exception(error)));
    }

    @Override
    public Observable<Boolean> completeEvent() {
        ContentValues contentValues = currentEvent.toContentValues();
        contentValues.put(EventModel.Columns.STATUS, EventStatus.COMPLETED.name());
        String completeDate = DateUtils.databaseDateFormat().format(DateUtils.getInstance().getToday());
        contentValues.put(EventModel.Columns.COMPLETE_DATE, completeDate);
        contentValues.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        contentValues.put(EventModel.Columns.STATE, currentEvent.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        boolean updated = briteDatabase.update(EventModel.TABLE, contentValues, "uid = ?", eventUid) > 0;
        if (updated && currentEvent.enrollment() != null)
            updateEnrollment(currentEvent.enrollment());
        return Observable.just(updated);
    }

    @Override
    public boolean reopenEvent() {
        ContentValues contentValues = currentEvent.toContentValues();
        contentValues.put(EventModel.Columns.STATUS, EventStatus.ACTIVE.name());
        contentValues.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        contentValues.put(EventModel.Columns.STATE, currentEvent.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        boolean updated = briteDatabase.update(EventModel.TABLE, contentValues, "uid = ?", eventUid) > 0;
        if (updated && currentEvent.enrollment() != null)
            updateEnrollment(currentEvent.enrollment());
        if (updated && currentEvent.enrollment() != null)
            updateEnrollment(currentEvent.enrollment());
        return updated;
    }

    @Override
    public Observable<Boolean> deleteEvent() {
        Event event = currentEvent;
        long status;
        if (event.state() == State.TO_POST) {
            String DELETE_WHERE = String.format(
                    "%s.%s = ?",
                    EventModel.TABLE, EventModel.Columns.UID
            );
            status = briteDatabase.delete(EventModel.TABLE, DELETE_WHERE, eventUid);
        } else {
            ContentValues contentValues = event.toContentValues();
            contentValues.put(EventModel.Columns.STATE, State.TO_DELETE.name());
            status = briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + " = ?", eventUid);
        }
        if (status == 1 && event.enrollment() != null)
            updateEnrollment(event.enrollment());

        return Observable.just(status == 1);
    }

    private void updateEnrollment(String enrollmentUid) {
        Enrollment enrollment = d2.enrollmentModule().enrollments.uid(enrollmentUid).get();
        ContentValues cv = enrollment.toContentValues();
        cv.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        cv.put("state", enrollment.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        briteDatabase.update(EnrollmentModel.TABLE, cv, "uid = ?", enrollmentUid);
        updateTei(enrollment.trackedEntityInstance());
    }

    private void updateTei(String teiUid) {
        TrackedEntityInstance tei = d2.trackedEntityModule().trackedEntityInstances.uid(teiUid).get();
        ContentValues cv = tei.toContentValues();
        cv.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        cv.put("state", tei.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        briteDatabase.update(TrackedEntityInstanceModel.TABLE, cv, "uid = ?", teiUid);
    }

    @Override
    public Observable<Boolean> updateEventStatus(EventStatus status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.STATUS, status.name());
        String updateDate = DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime());
        contentValues.put(EventModel.Columns.LAST_UPDATED, updateDate);
        return Observable.just(briteDatabase.update(EventModel.TABLE, contentValues, "uid = ?", eventUid) > 0);
    }

    @Override
    public Observable<Boolean> rescheduleEvent(Date newDate) {
        ContentValues cv = currentEvent.toContentValues();
        cv.put(EventModel.Columns.DUE_DATE, DateUtils.databaseDateFormat().format(newDate));
        cv.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        cv.put("state", currentEvent.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        cv.put("status", EventStatus.SCHEDULE.name());
        boolean updated = briteDatabase.update(EventModel.TABLE, cv, "uid = ?", eventUid) > 0;
        if (updated && currentEvent.enrollment() != null)
            updateEnrollment(currentEvent.enrollment());
        return Observable.just(updated);
    }

    @Override
    public Observable<String> programStage() {
        return Observable.defer(() -> Observable.just(d2.eventModule().events.uid(eventUid).get().programStage()));
    }

    @Override
    public boolean getAccessDataWrite() {
        boolean canWrite;
        canWrite =
                d2.programModule().programs.uid(
                        d2.eventModule().events.uid(eventUid).get().program()
                ).get().access().data().write();
        if (canWrite)
            canWrite =
                    d2.programModule().programStages.uid(
                            d2.eventModule().events.uid(eventUid).get().programStage()
                    ).get().access().data().write();
        return canWrite;
    }

    @Override
    public void setLastUpdated(String lastUpdatedUid) {
        this.lastUpdatedUid = lastUpdatedUid;
    }

    @Override
    public Flowable<EventStatus> eventStatus() {
        return Flowable.just(d2.eventModule().events.uid(eventUid).get())
                .map(Event::status);
    }

    private static final String QUERY_VALUES = "SELECT DISTINCT" +
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

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues(String eventUid) {

        return d2.eventModule().events.uid(eventUid).getAsync()
                .flatMap(event -> d2.trackedEntityModule().trackedEntityDataValues.byEvent().eq(eventUid).byValue().isNotNull().getAsync()
                        .toFlowable()
                        .flatMapIterable(values -> values)
                        .map(trackedEntityDataValue -> {
                            DataElement de = d2.dataElementModule().dataElements.uid(trackedEntityDataValue.dataElement()).get();
                            String value = trackedEntityDataValue.value();
                            if (de.optionSetUid() != null) {
                                ProgramRuleVariable variable = d2.programModule().programRuleVariables
                                        .byDataElementUid().eq(de.uid())
                                        .byProgramUid().eq(event.program())
                                        .one().get();
                                Option option = d2.optionModule().options.byOptionSetUid().eq(de.optionSetUid())
                                        .byCode().eq(value).one().get();
                                if (variable == null || variable.useCodeForOptionSet() != null && variable.useCodeForOptionSet())
                                    value = option.code();
                                else
                                    value = option.name();

                                if (de.valueType() == ValueType.AGE)
                                    value = value.split("T")[0];
                            }

                            return RuleDataValue.create(event.eventDate(), event.programStage(), de.uid(), value);
                        }).toList()).toFlowable();
/*
        return briteDatabase.createQuery(Arrays.asList(EventModel.TABLE,
                TrackedEntityDataValueModel.TABLE), QUERY_VALUES, eventUid == null ? "" : eventUid)
                .mapToList(cursor -> {
                    Date eventDate = parseDate(cursor.getString(0));
                    String programStage = cursor.getString(1);
                    String dataElement = cursor.getString(2);
                    String value = cursor.getString(3);
                    Boolean useCode = cursor.getInt(4) == 1;
                    String optionCode = cursor.getString(5);
                    String optionName = cursor.getString(6);
                    if (!isEmpty(optionCode) && !isEmpty(optionName))
                        value = useCode ? optionCode : optionName; //If de has optionSet then check if value should be code or name for program rules
                    if (d2.dataElementModule().dataElements.uid(dataElement).get().valueType() == ValueType.AGE)
                        value = value.split("T")[0];
                    return RuleDataValue.create(eventDate, programStage,
                            dataElement, value);
                }).toFlowable(BackpressureStrategy.LATEST);*/
    }

    @NonNull
    private static Date parseDate(@NonNull String date) {
        try {
            return BaseIdentifiableObject.DATE_FORMAT.parse(date);
        } catch (ParseException parseException) {
            throw new RuntimeException(parseException);
        }
    }

    @NonNull
    private FormSectionViewModel mapToFormSectionViewModels
            (@NonNull Cursor cursor) {
        // GET PROGRAMSTAGE DISPLAYNAME IN CASE THERE ARE NO SECTIONS
        if (cursor.getString(2) == null) {
            // This programstage has no sections
            return FormSectionViewModel.createForProgramStageWithLabel(eventUid, cursor.getString(4), cursor.getString(1));
        } else {
            // This programstage has sections
            return FormSectionViewModel.createForSection(eventUid, cursor.getString(2), cursor.getString(3), cursor.getString(5));
        }
    }

    @Override
    public Observable<List<OrganisationUnitLevel>> getOrgUnitLevels() {
        return Observable.just(d2.organisationUnitModule().organisationUnitLevels.get());
    }

    @Override
    public boolean optionIsInOptionGroup(String optionUid, String optionGroupToHide) {
        List<ObjectWithUid> optionGroupOptions = d2.optionModule().optionGroups.uid(optionGroupToHide).withAllChildren().get().options();
        boolean isInGroup = false;
        if (optionGroupOptions != null)
            for (ObjectWithUid uidObject : optionGroupOptions)
                if (uidObject.uid().equals(optionUid))
                    isInGroup = true;

        return isInGroup;
    }

    @Override
    public String getSectionFor(String field) {
        String sectionToReturn = "NO_SECTION";
        List<ProgramStageSection> programStages = d2.programModule().programStageSections.byProgramStageUid().eq(currentEvent.programStage()).withDataElements().get();
        for (ProgramStageSection section : programStages) {
            if (UidsHelper.getUidsList(section.dataElements()).contains(field)) {
                sectionToReturn = section.uid();
                break;
            }
        }
        return sectionToReturn;
    }

    @Override
    public Single<Boolean> canReOpenEvent() {
        return Single.defer(() -> Single.fromCallable(() -> {
                    boolean hasAuthority = d2.userModule().authorities
                            .byName().eq("F_UNCOMPLETE_EVENT").one().exists();
                    return hasAuthority;
                }
        ));
    }

    @Override
    public void assign(String uid, String value) {
        try {
            if (d2.dataElementModule().dataElements.uid(uid).exists()) {
                if (!isEmpty(value))
                    d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, uid).set(value); //TODO: Should be assigned in all events?
                else if( d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, uid).exists())
                    d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, uid).delete();
            } else if (d2.trackedEntityModule().trackedEntityAttributes.uid(uid).exists()) {
                String tei = d2.enrollmentModule().enrollments.uid(currentEvent.enrollment()).get().trackedEntityInstance();
                if (!isEmpty(value))
                    d2.trackedEntityModule().trackedEntityAttributeValues.value(uid, tei).set(value);
                else if( d2.trackedEntityModule().trackedEntityAttributeValues.value(eventUid, tei).exists())
                    d2.trackedEntityModule().trackedEntityAttributeValues.value(eventUid, tei).delete();
            }
        } catch (D2Error d2Error) {
            Timber.e(d2Error.originalException());
        }
    }
}
