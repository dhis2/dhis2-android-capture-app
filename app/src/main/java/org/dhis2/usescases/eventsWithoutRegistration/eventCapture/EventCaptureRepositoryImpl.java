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
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.utils.CodeGeneratorImpl;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.dhis2.utils.RulesUtilsProviderImpl;
import org.dhis2.utils.rules.RuleEffectResult;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ObjectWithUid;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.event.EventTableInfo;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.option.OptionGroup;
import org.hisp.dhis.android.core.option.OptionSet;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramRule;
import org.hisp.dhis.android.core.program.ProgramRuleAction;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramRuleVariable;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.program.ProgramStageSectionDeviceRendering;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.program.ProgramType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceTableInfo;
import org.hisp.dhis.rules.models.Rule;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    private final BriteDatabase briteDatabase;
    private final String eventUid;
    private final Event currentEvent;
    private final FormRepository formRepository;
    private final D2 d2;
    private final boolean isEventEditable;
    private String lastUpdatedUid;
    private RuleEvent.Builder eventBuilder;
    private Map<String, List<Rule>> dataElementRules = new HashMap<>();
    private List<ProgramRule> mandatoryRules;
    private List<ProgramRule> rules;

    public EventCaptureRepositoryImpl(Context context, BriteDatabase briteDatabase, FormRepository formRepository, String eventUid, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;
        this.formRepository = formRepository;
        this.d2 = d2;

        currentEvent = d2.eventModule().events.uid(eventUid).withAllChildren().blockingGet();
        ProgramStage programStage = d2.programModule().programStages.uid(currentEvent.programStage()).withAllChildren().blockingGet();
        OrganisationUnit ou = d2.organisationUnitModule().organisationUnits.uid(currentEvent.organisationUnit()).withAllChildren().blockingGet();

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
    }

    private void loadDataElementRules(Event event) {
        float init = System.currentTimeMillis();
        rules = d2.programModule().programRules.byProgramUid().eq(event.program()).withAllChildren().blockingGet();
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
        Timber.d("LOAD MANDATORY RULES  AT %s", System.currentTimeMillis() - init);

        List<ProgramRuleVariable> variables = d2.programModule().programRuleVariables
                .byProgramUid().eq(event.program())
                .withAllChildren().blockingGet();
        Timber.d("LOAD VARIABLES RULES  AT %s", System.currentTimeMillis() - init);

        Iterator<ProgramRuleVariable> variableIterator = variables.iterator();
        while (variableIterator.hasNext()) {
            ProgramRuleVariable variable = variableIterator.next();
            if (variable.programStage() != null && variable.programStage().uid().equals(event.programStage()))
                variableIterator.remove();
            else if (variable.dataElement() == null)
                variableIterator.remove();
        }
        Timber.d("FINISHED REMOVING VARIABLES RULES  AT %s", System.currentTimeMillis() - init);

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
        Program program = d2.programModule().programs.uid(currentEvent.program()).withAllChildren().blockingGet();
        if (program.programType().equals(ProgramType.WITH_REGISTRATION)) {
            Iterator<ProgramRule> iterator = rules.iterator();
            while (iterator.hasNext()) {
                if (haveDisplayActionIndicator(iterator.next()))
                    iterator.remove();
            }
        }
        return rules;
    }

    private boolean haveDisplayActionIndicator(@NonNull ProgramRule programRule) {
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
            if (rule != null)
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
        Enrollment enrollment = d2.enrollmentModule().enrollments.uid(d2.eventModule().events.uid(eventUid).blockingGet().enrollment()).blockingGet();
        return enrollment == null || enrollment.status() == EnrollmentStatus.ACTIVE;
    }

    private boolean inOrgUnitRange(String eventUid) {
        Event event = d2.eventModule().events.uid(eventUid).blockingGet();
        String orgUnitUid = event.organisationUnit();
        Date eventDate = event.eventDate();
        boolean inRange = true;
        OrganisationUnit orgUnit = d2.organisationUnitModule().organisationUnits.uid(orgUnitUid).blockingGet();
        if (eventDate != null && orgUnit.openingDate() != null && eventDate.before(orgUnit.openingDate()))
            inRange = false;
        if (eventDate != null && orgUnit.closedDate() != null && eventDate.after(orgUnit.closedDate()))
            inRange = false;

        return inRange;
    }

    @Override
    public boolean isEnrollmentCancelled() {
        Enrollment enrollment = d2.enrollmentModule().enrollments.uid(d2.eventModule().events.uid(eventUid).blockingGet().enrollment()).blockingGet();
        if (enrollment == null)
            return false;
        else
            return d2.enrollmentModule().enrollments.uid(d2.eventModule().events.uid(eventUid).blockingGet().enrollment()).blockingGet().status() == EnrollmentStatus.CANCELLED;
    }

    @Override
    public boolean isEventExpired(String eventUid) {
        Event event = d2.eventModule().events.uid(eventUid).withAllChildren().blockingGet();
        Program program = d2.programModule().programs.uid(event.program()).withAllChildren().blockingGet();
        ProgramStage stage = d2.programModule().programStages.uid(event.programStage()).blockingGet();
        boolean isExpired = DateUtils.getInstance().isEventExpired(event.eventDate(), event.completedDate(), event.status(), program.completeEventsExpiryDays(), stage.periodType() != null ? stage.periodType() : program.expiryPeriodType(), program.expiryDays());
        boolean blockAfterComplete = event.status() == EventStatus.COMPLETED && stage.blockEntryForm();
        boolean isInCaptureOrgUnit = d2.organisationUnitModule().organisationUnits
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(event.organisationUnit()).one().blockingExists();
        boolean hasCatComboAccess = event.attributeOptionCombo() == null || getCatComboAccess(event);

        boolean editable = isEnrollmentOpen() && !blockAfterComplete && !isExpired &&
                getAccessDataWrite() && inOrgUnitRange(eventUid) && isInCaptureOrgUnit && hasCatComboAccess;

        return !editable;
    }

    private boolean getCatComboAccess(Event event) {
        if (event.attributeOptionCombo() != null) {
            List<String> optionUid = UidsHelper.getUidsList(d2.categoryModule()
                    .categoryOptionCombos.uid(event.attributeOptionCombo())
                    .withAllChildren().blockingGet().categoryOptions());
            List<CategoryOption> options = d2.categoryModule().categoryOptions.byUid().in(optionUid).blockingGet();
            boolean access = true;
            Date eventDate = event.eventDate();
            for (CategoryOption option : options) {
                if (!option.access().data().write())
                    access = false;
                if (eventDate != null && option.startDate() != null && eventDate.before(option.startDate()))
                    access = false;
                if (eventDate != null && option.endDate() != null && eventDate.after(option.endDate()))
                    access = false;
            }
            return access;
        } else
            return true;
    }

    @Override
    public Flowable<String> programStageName() {
        return Flowable.just(d2.eventModule().events.uid(eventUid).blockingGet())
                .map(event -> d2.programModule().programStages.uid(event.programStage()).blockingGet().displayName());
    }

    @Override
    public Flowable<String> eventDate() {
        return Flowable.just(d2.eventModule().events.uid(eventUid).blockingGet())
                .map(event -> DateUtils.uiDateFormat().format(event.eventDate()));
    }

    @Override
    public Flowable<String> orgUnit() {
        return Flowable.just(d2.eventModule().events.uid(eventUid).blockingGet())
                .map(event -> d2.organisationUnitModule().organisationUnits.uid(event.organisationUnit()).blockingGet().displayName());
    }


    @Override
    public Flowable<String> catOption() {
        return Flowable.just(d2.eventModule().events.uid(eventUid).blockingGet())
                .map(event -> d2.categoryModule().categoryOptionCombos.uid(event.attributeOptionCombo()))
                .map(categoryOptionComboRepo -> {
                    if (categoryOptionComboRepo.blockingGet() == null)
                        return "";
                    else
                        return categoryOptionComboRepo.blockingGet().displayName();
                })
                .map(displayName -> displayName.equals("default") ? "" : displayName);
    }

    @Override
    public Flowable<List<FormSectionViewModel>> eventSections() {
        return d2.eventModule().events.uid(eventUid).get()
                .map(eventSingle -> {
                    List<FormSectionViewModel> formSection = new ArrayList<>();
                    if (eventSingle.deleted() == null || !eventSingle.deleted()) {
                        ProgramStage stage = d2.programModule().programStages.uid(eventSingle.programStage()).withAllChildren().blockingGet();
                        if (stage.programStageSections().size() > 0) {
                            for (ProgramStageSection section : stage.programStageSections())
                                formSection.add(FormSectionViewModel.createForSection(eventUid, section.uid(), section.displayName(),
                                        section.renderType().mobile() != null ? section.renderType().mobile().type().name() : null));
                        } else
                            formSection.add(FormSectionViewModel.createForProgramStageWithLabel(eventUid, stage.displayName(), stage.uid()));
                    }
                    return formSection;
                }).toFlowable();
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list(String sectionUid) {
        return d2.eventModule().events.withAllChildren().uid(eventUid).get()
                .map(event -> {
                    List<FieldViewModel> fields = new ArrayList<>();
                    ProgramStage stage = d2.programModule().programStages.withAllChildren().uid(event.programStage()).blockingGet();

                    ProgramStageSection stageSection = d2.programModule().programStageSections.withDataElements().uid(sectionUid).blockingGet();
                    for (ProgramStageDataElement programStageDataElement : stage.programStageDataElements()) {
                        if (UidsHelper.getUidsList(stageSection.dataElements()).contains(programStageDataElement.dataElement().uid())) {
                            DataElement dataelement = d2.dataElementModule().dataElements.uid(programStageDataElement.dataElement().uid()).blockingGet();
                            fields.add(transform(programStageDataElement, dataelement,
                                    searchValueDataElement(programStageDataElement.dataElement().uid(), event.trackedEntityDataValues()), sectionUid));
                        }
                    }

                    return checkRenderType(fields, stage);
                }).toFlowable();
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> evaluateForSection(String sectionUid) {
        return d2.eventModule().events.withAllChildren().uid(eventUid).get()
                .flatMap(event -> d2.programModule().programStages.uid(event.programStage()).get()
                        .map(stage -> {
                            Timber.tag("EVENT SECTION").d("INIT CALCULATIONS");
                            RuleEffectResult effectResult = new RulesUtilsProviderImpl(new CodeGeneratorImpl()).evaluateEvent(eventUid, sectionUid);
                            List<FieldViewModel> fields = new ArrayList<>();
                            for (String deUid : effectResult.getFields()) {
                                DataElement dataelement = d2.dataElementModule().dataElements.uid(deUid).blockingGet();
                                ProgramStageDataElement programStageDataElement;
                                String value = d2.trackedEntityModule().trackedEntityDataValues.byDataElement().eq(deUid).byEvent().eq(eventUid).one().blockingExists() ?
                                        d2.trackedEntityModule().trackedEntityDataValues.byDataElement().eq(deUid).byEvent().eq(eventUid).one().blockingGet().value() : null;
                                try (Cursor query = briteDatabase.query("SELECT * FROM ProgramStageDataElement WHERE programStage = ? AND dataElement = ?", stage.uid(), deUid)) {
                                    query.moveToFirst();
                                    programStageDataElement = ProgramStageDataElement.create(query);
                                }

                                FieldViewModel field = transform(
                                        programStageDataElement,
                                        dataelement,
                                        value,
                                        sectionUid);
                                if (effectResult.getWarnings().get(deUid) != null)
                                    field = field.withWarning(effectResult.getWarnings().get(deUid));
                                if (effectResult.getErrors().get(deUid) != null)
                                    field = field.withError(effectResult.getErrors().get(deUid));
                                if (field instanceof SpinnerViewModel) {
                                    ((SpinnerViewModel) field).setOptionsToHide(effectResult.getOptionsToHide(), effectResult.getOptionGroupsToHide());
                                    ((SpinnerViewModel) field).setOptionGroupsToShow(effectResult.getShowOptionGroup());
                                }

                                if (effectResult.getMandatoryFields().contains(field.uid()))
                                    field.setMandatory();

                                boolean hideForOption = field instanceof ImageViewModel && effectResult.getOptionsToHide().contains(field.uid().split("\\.")[1]);
                                boolean hideForOptionGroup = field instanceof ImageViewModel && optionIsInOptionGroup(field.uid().split("\\.")[1], effectResult.getOptionGroupsToHide());

                                if (!hideForOption && !hideForOptionGroup)
                                    fields.add(field);

                            }

                            List<ProgramStageSection> sections = d2.programModule().programStageSections.byProgramStageUid().eq(stage.uid()).blockingGet();
                            Collections.sort(sections, (s1, s2) -> s1.sortOrder().compareTo(s2.sortOrder()));
                            if (sections.get(sections.size() - 1).uid().equals(sectionUid)) {
                                //TODO: EFFECT FOR DISPLAY KEY VALUE PAIR AND DISPLAY TEXT
                            }

                            return checkRenderType(fields, d2.programModule().programStages.uid(event.programStage()).withAllChildren().blockingGet());

                        })).doOnSuccess(data -> Timber.tag("EVENT SECTION").d("END CALCULATIONS")
                )
                .toFlowable();
    }

    private ProgramStageSectionRenderingType renderingType(String sectionUid) {
        ProgramStageSectionRenderingType renderingType = ProgramStageSectionRenderingType.LISTING;
        if (sectionUid != null) {
            ProgramStageSectionDeviceRendering stageSectionRendering = d2.programModule().programStageSections.uid(sectionUid).blockingGet().renderType().mobile();
            if (stageSectionRendering != null)
                renderingType = stageSectionRendering.type();
        }

        return renderingType;
    }

    private List<FieldViewModel> checkRenderType(List<FieldViewModel> fieldViewModels, ProgramStage stage) {
        ArrayList<FieldViewModel> renderList = new ArrayList<>();

        for (FieldViewModel fieldViewModel : fieldViewModels) {

            ProgramStageSectionRenderingType renderingType = renderingType(fieldViewModel.programStageSection());
            if (!isEmpty(fieldViewModel.optionSet()) && renderingType != ProgramStageSectionRenderingType.LISTING) {
                OptionSet optionSets = d2.optionModule().optionSets.withOptions().byUid().eq(fieldViewModel.optionSet() == null ? "" : fieldViewModel.optionSet()).one().blockingGet();
                for (Option option : optionSets.options()) {
                    ValueTypeDeviceRendering fieldRendering = null;

                    for (ProgramStageDataElement programStageDataElement : stage.programStageDataElements()) {
                        if (programStageDataElement.uid().equals(option.uid()))
                            fieldRendering = programStageDataElement.renderType().mobile();
                    }

                    ObjectStyle objectStyle = ObjectStyle.builder().build();
                    try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", option.uid())) {
                        if (objStyleCursor != null && objStyleCursor.moveToFirst())
                            objectStyle = ObjectStyle.create(objStyleCursor);
                    }

                    renderList.add(fieldFactory.create(
                            fieldViewModel.uid() + "." + option.uid(), //fist
                            option.displayName() + "-" + option.code(), ValueType.TEXT, false,
                            fieldViewModel.optionSet(), fieldViewModel.value(), fieldViewModel.programStageSection(),
                            fieldViewModel.allowFutureDate(), fieldViewModel.editable() == null ? false : fieldViewModel.editable(), renderingType, fieldViewModel.description(), fieldRendering, optionSets.options().size(), objectStyle));

                }
            } else
                renderList.add(fieldViewModel);
        }
        return renderList;
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list() {
        return d2.eventModule().events.withAllChildren().uid(eventUid).get()
                .map(event -> {
                    List<FieldViewModel> fields = new ArrayList<>();
                    ProgramStage stage = d2.programModule().programStages.withAllChildren().uid(event.programStage()).blockingGet();
                    if (stage.programStageSections().size() > 0) {
                        for (ProgramStageSection section : stage.programStageSections()) {
                            ProgramStageSection stageSection = d2.programModule().programStageSections.withDataElements().uid(section.uid()).blockingGet();
                            for (ProgramStageDataElement programStageDataElement : stage.programStageDataElements()) {
                                if (UidsHelper.getUidsList(stageSection.dataElements()).contains(programStageDataElement.dataElement().uid())) {
                                    DataElement dataelement = d2.dataElementModule().dataElements.uid(programStageDataElement.dataElement().uid()).blockingGet();
                                    fields.add(transform(programStageDataElement, dataelement,
                                            searchValueDataElement(programStageDataElement.dataElement().uid(), event.trackedEntityDataValues()), section.uid()));
                                }
                            }
                        }
                    } else
                        for (ProgramStageDataElement programStageDataElement : stage.programStageDataElements()) {
                            DataElement dataelement = d2.dataElementModule().dataElements.uid(programStageDataElement.dataElement().uid()).blockingGet();
                            fields.add(transform(programStageDataElement, dataelement,
                                    searchValueDataElement(programStageDataElement.dataElement().uid(), event.trackedEntityDataValues()), null));

                        }
                    return checkRenderType(fields, stage);
                }).toFlowable();
    }

    private String searchValueDataElement(String dataElement, List<TrackedEntityDataValue> dataValues) {
        for (TrackedEntityDataValue dataValue : dataValues)
            if (dataValue.dataElement().equals(dataElement)) {
                return dataValue.value();
            }

        return null;
    }

    @NonNull
    private FieldViewModel transform(@NonNull ProgramStageDataElement stage, DataElement dataElement, String value, String programStageSection) {
        String uid = dataElement.uid();
        String displayName = dataElement.displayName();
        String valueTypeName = dataElement.valueType().name();
        boolean mandatory = stage.compulsory();
        String optionSet = dataElement.optionSetUid();
        String dataValue = value;
        Option option = d2.optionModule().options.byOptionSetUid().eq(optionSet).byCode().eq(dataValue).one().blockingGet();
        int optionCount = d2.optionModule().options.byOptionSetUid().eq(optionSet).blockingCount();
        boolean allowFurureDates = stage.allowFutureDate();
        String formName = dataElement.displayFormName();
        String description = dataElement.displayDescription();

        if (option != null) {
            dataValue = option.displayName();
        }

        ValueTypeDeviceRendering fieldRendering = stage.renderType() == null ? null : stage.renderType().mobile();

        ObjectStyle objectStyle = ObjectStyle.builder().build();
        try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", uid)) {
            if (objStyleCursor != null && objStyleCursor.moveToFirst())
                objectStyle = ObjectStyle.create(objStyleCursor);
        }

        if (ValueType.valueOf(valueTypeName) == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(dataValue).blockingGet().displayName();
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
        return loadRules().flatMap(loadRules -> queryDataValues(eventUid))
                .map(dataValues -> eventBuilder.dataValues(dataValues).build())
                .switchMap(
                        event -> formRepository.ruleEngine()
                                .map(ruleEngine -> {
                                    if (isEmpty(lastUpdatedUid))
                                        return ruleEngine.evaluate(event, trasformToRule(rules)).call();
                                    else {
                                        List<Rule> updatedRules = dataElementRules.get(lastUpdatedUid) != null ? dataElementRules.get(lastUpdatedUid) : new ArrayList<Rule>();
                                        List<Rule> finalRules = updatedRules.isEmpty() ? trasformToRule(rules) : updatedRules;
                                        return ruleEngine.evaluate(event, finalRules).call();
                                    }
                                })
                                .map(Result::success)
                                .onErrorReturn(error -> Result.failure(new Exception(error)))

                );
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> fullCalculate() {
        return loadRules()
                .switchMap(loadRules -> queryDataValues(eventUid))
                .map(dataValues -> eventBuilder.dataValues(dataValues).build())
                .switchMap(
                        event -> formRepository.ruleEngine()
                                .switchMap(ruleEngine -> {
                                    if (isEmpty(lastUpdatedUid))
                                        return Flowable.fromCallable(ruleEngine.evaluate(event, trasformToRule(rules)));
                                    else {
                                        List<Rule> updatedRules = dataElementRules.get(lastUpdatedUid) != null ? dataElementRules.get(lastUpdatedUid) : new ArrayList<Rule>();
                                        List<Rule> finalRules = updatedRules.isEmpty() ? trasformToRule(rules) : updatedRules;
                                        return Flowable.fromCallable(ruleEngine.evaluate(event, finalRules));
                                    }
                                })
                                .map(Result::success)
                                .onErrorReturn(error -> Result.failure(new Exception(error)))
                );
    }

    private Flowable<Boolean> loadRules() {
        return Flowable.fromCallable(() -> {
            Timber.d("INIT RULES");
            long init = System.currentTimeMillis();
            loadDataElementRules(currentEvent);
            Timber.d("INIT RULES END AT %s", (System.currentTimeMillis() - init) / 1000);
            return true;
        });
    }

    @Override
    public Observable<Boolean> completeEvent() {
        ContentValues contentValues = currentEvent.toContentValues();
        contentValues.put(EventTableInfo.Columns.STATUS, EventStatus.COMPLETED.name());
        String completeDate = DateUtils.databaseDateFormat().format(DateUtils.getInstance().getToday());
        contentValues.put(EventTableInfo.Columns.COMPLETE_DATE, completeDate);
        contentValues.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        contentValues.put(EventTableInfo.Columns.STATE, currentEvent.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        boolean updated = briteDatabase.update(EventTableInfo.TABLE_INFO.name(), contentValues, "uid = ?", eventUid) > 0;
        if (updated && currentEvent.enrollment() != null)
            updateEnrollment(currentEvent.enrollment());
        return Observable.just(updated);
    }

    @Override
    public boolean reopenEvent() {
        ContentValues contentValues = currentEvent.toContentValues();
        contentValues.put(EventTableInfo.Columns.STATUS, EventStatus.ACTIVE.name());
        contentValues.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        contentValues.put(EventTableInfo.Columns.STATE, currentEvent.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        boolean updated = briteDatabase.update(EventTableInfo.TABLE_INFO.name(), contentValues, "uid = ?", eventUid) > 0;
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
                    EventTableInfo.TABLE_INFO.name(), EventTableInfo.Columns.UID
            );
            status = briteDatabase.delete(EventTableInfo.TABLE_INFO.name(), DELETE_WHERE, eventUid);
        } else {
            ContentValues contentValues = event.toContentValues();
            contentValues.put(EventTableInfo.Columns.DELETED, true);
            status = briteDatabase.update(EventTableInfo.TABLE_INFO.name(), contentValues, EventTableInfo.Columns.UID + " = ?", eventUid);
        }
        if (status == 1 && event.enrollment() != null)
            updateEnrollment(event.enrollment());

        return Observable.just(status == 1);
    }

    private void updateEnrollment(String enrollmentUid) {
        Enrollment enrollment = d2.enrollmentModule().enrollments.uid(enrollmentUid).blockingGet();
        ContentValues cv = enrollment.toContentValues();
        cv.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        cv.put("state", enrollment.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        briteDatabase.update(EnrollmentTableInfo.TABLE_INFO.name(), cv, "uid = ?", enrollmentUid);
        updateTei(enrollment.trackedEntityInstance());
    }

    private void updateTei(String teiUid) {
        TrackedEntityInstance tei = d2.trackedEntityModule().trackedEntityInstances.uid(teiUid).blockingGet();
        ContentValues cv = tei.toContentValues();
        cv.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        cv.put("state", tei.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        briteDatabase.update(TrackedEntityInstanceTableInfo.TABLE_INFO.name(), cv, "uid = ?", teiUid);
    }

    @Override
    public Observable<Boolean> updateEventStatus(EventStatus status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventTableInfo.Columns.STATUS, status.name());
        String updateDate = DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime());
        contentValues.put(EventTableInfo.Columns.LAST_UPDATED, updateDate);
        return Observable.just(briteDatabase.update(EventTableInfo.TABLE_INFO.name(), contentValues, "uid = ?", eventUid) > 0);
    }

    @Override
    public Observable<Boolean> rescheduleEvent(Date newDate) {
        ContentValues cv = currentEvent.toContentValues();
        cv.put(EventTableInfo.Columns.DUE_DATE, DateUtils.databaseDateFormat().format(newDate));
        cv.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        cv.put("state", currentEvent.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        cv.put("status", EventStatus.SCHEDULE.name());
        boolean updated = briteDatabase.update(EventTableInfo.TABLE_INFO.name(), cv, "uid = ?", eventUid) > 0;
        if (updated && currentEvent.enrollment() != null)
            updateEnrollment(currentEvent.enrollment());
        return Observable.just(updated);
    }

    @Override
    public Observable<String> programStage() {
        return Observable.defer(() -> Observable.just(d2.eventModule().events.uid(eventUid).blockingGet().programStage()));
    }

    @Override
    public boolean getAccessDataWrite() {
        boolean canWrite;
        canWrite =
                d2.programModule().programs.uid(
                        d2.eventModule().events.uid(eventUid).blockingGet().program()
                ).blockingGet().access().data().write();
        if (canWrite)
            canWrite =
                    d2.programModule().programStages.uid(
                            d2.eventModule().events.uid(eventUid).blockingGet().programStage()
                    ).blockingGet().access().data().write();
        return canWrite;
    }

    @Override
    public void setLastUpdated(String lastUpdatedUid) {
        this.lastUpdatedUid = lastUpdatedUid;
    }

    @Override
    public Flowable<EventStatus> eventStatus() {
        return Flowable.just(d2.eventModule().events.uid(eventUid).blockingGet())
                .map(Event::status);
    }

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues(String eventUid) {
        return d2.eventModule().events.withAllChildren().uid(eventUid).get()
                .map(event -> {

                    Date eventDate = event.eventDate();
                    String programStage = event.programStage();
                    List<RuleDataValue> ruleDataValues = new ArrayList<>();

                    for (TrackedEntityDataValue dataValue : event.trackedEntityDataValues()) {
                        List<ProgramRuleVariable> variables = d2.programModule().programRuleVariables.byDataElementUid().eq(dataValue.dataElement()).blockingGet();
                        Option option = null;
                        DataElement dataElement = d2.dataElementModule().dataElements.uid(dataValue.dataElement()).blockingGet();
                        if (dataElement.optionSet() != null) {
                            option = d2.optionModule().options.byOptionSetUid().eq(dataElement.optionSet().uid()).byCode().eq(dataValue.value()).one().blockingGet();
                        }
                        String value = dataValue.value();
                        for (ProgramRuleVariable variable : variables) {
                            if (variable.dataElement().uid().equals(dataValue.dataElement())) {
                                if (option != null) {
                                    value = variable.useCodeForOptionSet() ? option.code() : option.name();
                                }

                                if (dataElement.valueType() == ValueType.AGE) {
                                    value = value.split("T")[0];
                                }
                            }

                        }
                        ruleDataValues.add(RuleDataValue.create(eventDate, programStage,
                                dataElement.uid(), value));
                    }

                    return ruleDataValues;
                }).toFlowable();
    }


    @Override
    public Observable<List<OrganisationUnitLevel>> getOrgUnitLevels() {
        return Observable.just(d2.organisationUnitModule().organisationUnitLevels.blockingGet());
    }

    @Override
    public boolean optionIsInOptionGroup(String optionUid, String optionGroupToHide) {
        List<ObjectWithUid> optionGroupOptions = d2.optionModule().optionGroups.uid(optionGroupToHide).withAllChildren().blockingGet().options();
        boolean isInGroup = false;
        if (optionGroupOptions != null)
            for (ObjectWithUid uidObject : optionGroupOptions)
                if (uidObject.uid().equals(optionUid))
                    isInGroup = true;

        return isInGroup;
    }

    private boolean optionIsInOptionGroup(String optionUid, List<String> optionGroupsToHide) {
        List<OptionGroup> optionGroups = d2.optionModule().optionGroups.byUid().in(optionGroupsToHide).withAllChildren().blockingGet();
        boolean isInGroup = false;
        for (OptionGroup optionGroup : optionGroups) {
            List<ObjectWithUid> optionGroupOptions = optionGroup.options();
            if (optionGroupOptions != null)
                for (ObjectWithUid uidObject : optionGroupOptions)
                    if (uidObject.uid().equals(optionUid))
                        isInGroup = true;
        }
        return isInGroup;
    }

    @Override
    public String getSectionFor(String field) {
        String sectionToReturn = "NO_SECTION";
        List<ProgramStageSection> programStages = d2.programModule().programStageSections.byProgramStageUid().eq(currentEvent.programStage()).withDataElements().blockingGet();
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
        return Single.defer(() -> Single.fromCallable(() -> d2.userModule().authorities
                .byName().in("F_UNCOMPLETE_EVENT", "ALL").one().blockingExists()
        ));
    }

    private Observable<Program> getExpiryDateFromEvent(String eventUid) {
        return d2.eventModule().events.uid(eventUid).get().
                flatMap(event -> d2.programModule().programs.uid(event.program()).get())
                .toObservable();
    }

    @Override
    public Observable<Boolean> isCompletedEventExpired(String eventUid) {
        return Observable.zip(d2.eventModule().events.uid(eventUid).get().toObservable(),
                getExpiryDateFromEvent(eventUid),
                ((event, program) -> DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays())));
    }
}
