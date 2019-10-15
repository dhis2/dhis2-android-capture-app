package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

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
import org.dhis2.data.forms.dataentry.fields.picture.PictureViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.dhis2.utils.custom_views.PictureView;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ObjectWithUid;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.fileresource.FileResource;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.option.OptionGroup;
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
import org.hisp.dhis.android.core.program.ProgramStageSectionRendering;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.program.ProgramType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;
import org.hisp.dhis.rules.models.Rule;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.io.File;
import java.util.ArrayList;
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

    public static final String NO_SECTION = "NO_SECTION";

    private final FieldViewModelFactory fieldFactory;
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

        currentEvent = d2.eventModule().events.withTrackedEntityDataValues().uid(eventUid).blockingGet();
        ProgramStage programStage = d2.programModule().programStages.withProgramStageDataElements().withProgramStageSections().uid(currentEvent.programStage()).blockingGet();
        OrganisationUnit ou = d2.organisationUnitModule().organisationUnits.uid(currentEvent.organisationUnit()).blockingGet();

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
                .withDataElements().withProgramIndicators().blockingGet();
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
        rules = d2.programModule().programRules.byProgramUid().eq(event.program()).withProgramRuleActions().blockingGet();
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
                .blockingGet();

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
        Program program = d2.programModule().programs.uid(currentEvent.program()).blockingGet();
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
        Event event = d2.eventModule().events.uid(eventUid).blockingGet();
        Program program = d2.programModule().programs.uid(event.program()).blockingGet();
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
                    .categoryOptionCombos.withCategoryOptions().uid(event.attributeOptionCombo())
                    .blockingGet().categoryOptions());
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
                        ProgramStage stage = d2.programModule().programStages.withProgramStageSections().uid(eventSingle.programStage()).blockingGet();
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

        if (sectionFields.containsKey(sectionUid))
            return Flowable.just(sectionFields.get(sectionUid))
                    .flatMapIterable(fieldViewModels -> fieldViewModels)
                    .map(fieldViewModel -> {
                        String uid = fieldViewModel.uid();
                        TrackedEntityDataValueObjectRepository valueRepository = d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, uid);

                        String value = null;
                        if (valueRepository.blockingExists()) {
                            value = valueRepository.blockingGet().value();

                            if (fieldViewModel instanceof OrgUnitViewModel && !isEmpty(value)) {
                                value = value + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(value).blockingGet().displayName();
                            }

                            if ((fieldViewModel instanceof SpinnerViewModel || fieldViewModel instanceof ImageViewModel) && !isEmpty(value)) {
                                value = d2.optionModule().options.byOptionSetUid().eq(fieldViewModel.optionSet()).byCode().eq(value).one().blockingGet().displayName();
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
            return d2.programModule().programStageSections.withDataElements().uid(sectionUid)
                    .get().toFlowable()
                    .map(section -> section.dataElements())
                    .flatMapIterable(UidsHelper::getUidsList)
                    .map(deUid -> {
                        DataElement de = d2.dataElementModule().dataElements.uid(deUid).blockingGet();
                        ProgramStageDataElement stageDataElement = stageDataElementsMap.get(deUid);
                        TrackedEntityDataValueObjectRepository valueRepository = d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, deUid);
                        ProgramStageSection section = sectionMap.get(sectionUid);


                        String uid = de.uid();
                        String displayName = de.displayName();
                        ValueType valueType = de.valueType();
                        boolean mandatory = stageDataElement.compulsory() != null ? stageDataElement.compulsory() : false;
                        String optionSet = de.optionSetUid();
                        String dataValue = valueRepository.blockingExists() ? valueRepository.blockingGet().value() : null;
                        boolean allowFurureDates = stageDataElement.allowFutureDate() != null ? stageDataElement.allowFutureDate() : false;
                        String formName = de.displayFormName();
                        String description = de.displayDescription();

                        int optionCount = 0;
                        if (!isEmpty(optionSet)) {
                            if (!isEmpty(dataValue)) {
                                dataValue = d2.optionModule().options.byOptionSetUid().eq(optionSet).byCode().eq(dataValue).one().blockingGet().displayName();
                            }
                            optionCount = d2.optionModule().options.byOptionSetUid().eq(optionSet).blockingCount();
                        }

                        ValueTypeDeviceRendering fieldRendering = stageDataElement.renderType() != null && stageDataElement.renderType().mobile() != null ? stageDataElement.renderType().mobile() : null;

                        ObjectStyle objectStyle = de.style() != null ? de.style() : ObjectStyle.builder().build();

                        if (valueType == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
                            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(dataValue).blockingGet().displayName();
                        }

                        ProgramStageSectionRenderingType renderingType = section.renderType() != null && section.renderType().mobile() != null ? section.renderType().mobile().type() : null;

                        return fieldFactory.create(uid, formName == null ? displayName : formName,
                                valueType, mandatory, optionSet, dataValue,
                                section.uid(), allowFurureDates,
                                !isEventEditable,
                                renderingType, description, fieldRendering, optionCount, objectStyle, de.fieldMask());
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
            ProgramStageSectionRendering sectionRendering = d2.programModule().programStageSections.uid(sectionUid).blockingGet().renderType();
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
                List<Option> options = d2.optionModule().options.byOptionSetUid().eq(fieldViewModel.optionSet() == null ? "" : fieldViewModel.optionSet())
                        .withStyle().blockingGet();
                for (Option option : options) {
                    ValueTypeDeviceRendering fieldRendering = null;

                    if (stageDataElementsMap.containsKey(fieldViewModel.uid())) {
                        ProgramStageDataElement psDE = stageDataElementsMap.get(fieldViewModel.uid());
                        fieldRendering = psDE.renderType() != null && psDE.renderType().mobile() != null ? psDE.renderType().mobile() : null;
                    }

                    ObjectStyle objectStyle = option.style();

                    renderList.add(fieldFactory.create(
                            fieldViewModel.uid() + "." + option.uid(),
                            option.displayName() + "-" + option.code(), ValueType.TEXT, false,
                            fieldViewModel.optionSet(), fieldViewModel.value(), fieldViewModel.programStageSection(),
                            fieldViewModel.allowFutureDate(), fieldViewModel.editable() == null ? false : fieldViewModel.editable(), renderingType, fieldViewModel.description(), fieldRendering, options.size(), objectStyle, fieldViewModel.fieldMask()));

                }
            } else
                renderList.add(fieldViewModel);
        }
        return renderList;
    }

    private ProgramStageSectionRenderingType getSectionRenderingType(ProgramStageSection stageSection) {
        ProgramStageSectionRenderingType renderingType = ProgramStageSectionRenderingType.LISTING;
        if (stageSection != null && stageSection.renderType() != null & stageSection.renderType().mobile() != null)
            renderingType = stageSection.renderType().mobile().type();

        return renderingType;
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
                        if (valueRepository.blockingExists()) {
                            value = valueRepository.blockingGet().value();

                            if (fieldViewModel instanceof OrgUnitViewModel && !isEmpty(value)) {
                                value = value + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(value).blockingGet().displayName();
                            }

                            if ((fieldViewModel instanceof SpinnerViewModel || fieldViewModel instanceof ImageViewModel) && !isEmpty(value)) {
                                value = d2.optionModule().options.byOptionSetUid().eq(fieldViewModel.optionSet()).byCode().eq(value).one().blockingGet().displayName();
                            }

                            if (fieldViewModel instanceof PictureViewModel) {
                                FileResource fileResource = d2.fileResourceModule().fileResources.uid(value).blockingGet();
                                if (fileResource != null)
                                    value = fileResource.path();
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
        else return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event ->
                        d2.programModule().programStages.withProgramStageSections()
                                .withProgramStageDataElements()
                                .withStyle().uid(event.programStage()).get()
                                .map(stage -> {
                                    List<ProgramStageDataElement> stageDataElements = stage.programStageDataElements();
                                    if (stage.programStageSections() != null && !stage.programStageSections().isEmpty()) {
                                        //REORDER PROGRAM STAGE DATA ELEMENTS
                                        List<String> dataElementsOrder = new ArrayList<>();
                                        for (ProgramStageSection section : stage.programStageSections()) {
                                            dataElementsOrder.addAll(UidsHelper.getUidsList(
                                                    d2.programModule().programStageSections.withDataElements().uid(section.uid()).blockingGet().dataElements()
                                            ));
                                        }
                                        Collections.sort(stageDataElements, (de1, de2) -> {
                                            Integer pos1 = dataElementsOrder.indexOf(de1.dataElement().uid());
                                            Integer pos2 = dataElementsOrder.indexOf(de2.dataElement().uid());
                                            return pos1.compareTo(pos2);
                                        });
                                    }
                                    return stageDataElements;
                                }).toFlowable()
                                .flatMapIterable(list -> list)
                                .map(programStageDataElement -> {
                                    DataElement de = d2.dataElementModule().dataElements.uid(programStageDataElement.dataElement().uid()).blockingGet();
                                    TrackedEntityDataValueObjectRepository valueRepository = d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, de.uid());

                                    ProgramStageSection programStageSection = null;
                                    List<ProgramStageSection> sections = d2.programModule().programStageSections.withDataElements().byProgramStageUid().eq(event.programStage()).blockingGet();
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
                                    String dataValue = valueRepository.blockingExists() ? valueRepository.blockingGet().value() : null;
                                    boolean allowFurureDates = programStageDataElement.allowFutureDate() != null ? programStageDataElement.allowFutureDate() : false;
                                    String formName = de.displayFormName();
                                    String description = de.displayDescription();

                                    int optionCount = 0;
                                    if (!isEmpty(optionSet)) {
                                        if (!isEmpty(dataValue)) {
                                            dataValue = d2.optionModule().options.byOptionSetUid().eq(optionSet).byCode().eq(dataValue).one().blockingGet().displayName();
                                        }
                                        optionCount = d2.optionModule().options.byOptionSetUid().eq(optionSet).blockingCount();
                                    }

                                    ValueTypeDeviceRendering fieldRendering = null;
                                    //TODO: CHANGE
                                    try (Cursor rendering = briteDatabase.query("SELECT * FROM ValueTypeDeviceRendering WHERE uid = ?", programStageDataElement.uid())) {
                                        if (rendering != null && rendering.moveToFirst()) {
                                            fieldRendering = ValueTypeDeviceRendering.create(rendering);
                                        }
                                    }

                                    ObjectStyle objectStyle = de.style() != null ? de.style() : ObjectStyle.builder().build();

                                    if (valueType == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
                                        dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(dataValue).blockingGet().displayName();
                                    }

                                    ProgramStageSectionRenderingType renderingType = programStageSection != null && programStageSection.renderType() != null &&
                                            programStageSection.renderType().mobile() != null ?
                                            programStageSection.renderType().mobile().type() : null;

                                    return fieldFactory.create(uid, formName == null ? displayName : formName,
                                            valueType, mandatory, optionSet, dataValue,
                                            programStageSection != null ? programStageSection.uid() : null, allowFurureDates,
                                            !isEventEditable,
                                            renderingType, description, fieldRendering, optionCount, objectStyle, de.fieldMask());
                                }).toList()).toFlowable()
                .map(data -> {
                    sectionFields.put("ALL", data);
                    return data;
                })
                .map(this::checkRenderType);
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

        ValueTypeDeviceRendering fieldRendering = stage.renderType() != null && stage.renderType().mobile() != null ? stage.renderType().mobile() : null;

        ObjectStyle objectStyle = dataElement.style();

        if (ValueType.valueOf(valueTypeName) == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(dataValue).blockingGet().displayName();
        }

        ProgramStageSectionRenderingType renderingType = renderingType(programStageSection);

        return fieldFactory.create(uid, formName == null ? displayName : formName,
                ValueType.valueOf(valueTypeName), mandatory, optionSet, dataValue,
                programStageSection, allowFurureDates,
                !isEventEditable,
                renderingType, description, fieldRendering, optionCount, objectStyle, dataElement.fieldMask());
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
        return Observable.fromCallable(() -> {
            try {
                d2.eventModule().events.uid(currentEvent.uid()).setStatus(EventStatus.COMPLETED);
                return true;
            } catch (D2Error d2Error) {
                d2Error.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public boolean reopenEvent() {
        try {
            d2.eventModule().events.uid(currentEvent.uid())
                    .setStatus(EventStatus.ACTIVE);
            return true;
        } catch (D2Error d2Error) {
            d2Error.printStackTrace();
            return false;
        }
    }

    @Override
    public Observable<Boolean> deleteEvent() {
        return d2.eventModule().events.uid(eventUid).delete().toObservable();
    }

    @Override
    public Observable<Boolean> updateEventStatus(EventStatus status) {

        return Observable.fromCallable(() -> {
            d2.eventModule().events.uid(currentEvent.uid())
                    .setStatus(status);
            return true;
        });
    }

    @Override
    public Observable<Boolean> rescheduleEvent(Date newDate) {
        return Observable.fromCallable(() -> {
            d2.eventModule().events.uid(currentEvent.uid())
                    .setDueDate(newDate);
            d2.eventModule().events.uid(currentEvent.uid())
                    .setStatus(EventStatus.SCHEDULE);
            return true;
        });
    }

    @Override
    public Observable<String> programStage() {
        return Observable.defer(() -> Observable.just(currentEvent.programStage()));
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
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> d2.trackedEntityModule().trackedEntityDataValues.byEvent().eq(eventUid).byValue().isNotNull().get()
                        .toFlowable()
                        .flatMapIterable(values -> values)
                        .map(trackedEntityDataValue -> {
                            DataElement de = d2.dataElementModule().dataElements.uid(trackedEntityDataValue.dataElement()).blockingGet();
                            String value = trackedEntityDataValue.value();
                            if (de.optionSetUid() != null) {
                                ProgramRuleVariable variable = d2.programModule().programRuleVariables
                                        .byDataElementUid().eq(de.uid())
                                        .byProgramUid().eq(event.program())
                                        .one().blockingGet();
                                Option option = d2.optionModule().options.byOptionSetUid().eq(de.optionSetUid())
                                        .byCode().eq(value).one().blockingGet();
                                if (variable == null || variable.useCodeForOptionSet() != null && variable.useCodeForOptionSet())
                                    value = option.code();
                                else
                                    value = option.name();

                                if (de.valueType() == ValueType.AGE)
                                    value = value.split("T")[0];
                            }

                            return RuleDataValue.create(event.eventDate(), event.programStage(), de.uid(), value);
                        }).toList()).toFlowable();
    }


    @Override
    public Observable<List<OrganisationUnitLevel>> getOrgUnitLevels() {
        return Observable.just(d2.organisationUnitModule().organisationUnitLevels.blockingGet());
    }

    @Override
    public boolean optionIsInOptionGroup(String optionUid, String optionGroupToHide) {
        List<ObjectWithUid> optionGroupOptions = d2.optionModule().optionGroups.withOptions().uid(optionGroupToHide).blockingGet().options();
        boolean isInGroup = false;
        if (optionGroupOptions != null)
            for (ObjectWithUid uidObject : optionGroupOptions)
                if (uidObject.uid().equals(optionUid))
                    isInGroup = true;

        return isInGroup;
    }

    private boolean optionIsInOptionGroup(String optionUid, List<String> optionGroupsToHide) {
        List<OptionGroup> optionGroups = d2.optionModule().optionGroups.byUid().in(optionGroupsToHide).withOptions().blockingGet();
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

    @Override
    public void assign(String uid, String value) {
        try {
            if (d2.dataElementModule().dataElements.uid(uid).blockingExists()) {
                handleAssignToDataElement(uid, value);
            } else if (d2.trackedEntityModule().trackedEntityAttributes.uid(uid).blockingExists()) {
                handleAssignToAttribute(uid, value);
            }
        } catch (D2Error d2Error) {
            Timber.e(d2Error.originalException());
        }
    }

    @Override
    public void saveImage(String uid, String filePath) {
        String newFilePath = filePath;
        TrackedEntityDataValueObjectRepository valueRepository = d2.trackedEntityModule().trackedEntityDataValues
                .value(eventUid, uid);
        if (d2.dataElementModule().dataElements.uid(uid).blockingGet().valueType() == ValueType.IMAGE
                && filePath != null) {
            try {
                newFilePath = getFileResource(filePath);
            } catch (D2Error d2Error) {
                d2Error.printStackTrace();
            }
        }
        try {
            if (!isEmpty(filePath))
                valueRepository.blockingSet(newFilePath);
            else
                valueRepository.blockingDelete();
        }catch (D2Error d2Error){}

    }

    private String getFileResource(String path) throws D2Error {
        File file = new File(path);
        return d2.fileResourceModule().fileResources.blockingAdd(file);
    }

    private void handleAssignToDataElement(String deUid, String value) throws D2Error {
        List<String> eventUids;
        if (currentEvent.enrollment() != null) {
            eventUids = UidsHelper.getUidsList(d2.eventModule().events
                    .byEnrollmentUid().eq(currentEvent.enrollment())
                    .byStatus().in(EventStatus.ACTIVE, EventStatus.COMPLETED)
                    .blockingGet());
        } else {
            eventUids = UidsHelper.getUidsList(d2.eventModule().events
                    .byProgramUid().eq(currentEvent.program())
                    .byProgramStageUid().eq(currentEvent.programStage())
                    .byOrganisationUnitUid().eq(currentEvent.organisationUnit())
                    .blockingGet());
        }

        for (String eventUid : eventUids) {
            if (!isEmpty(value))
                d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, deUid).blockingSet(value);
            else if (d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, deUid).blockingExists())
                d2.trackedEntityModule().trackedEntityDataValues.value(eventUid, deUid).blockingDelete();
        }
    }

    private void handleAssignToAttribute(String attributeUid, String value) throws D2Error {
        String tei = d2.enrollmentModule().enrollments.uid(currentEvent.enrollment()).blockingGet().trackedEntityInstance();
        if (!isEmpty(value))
            d2.trackedEntityModule().trackedEntityAttributeValues.value(attributeUid, tei).blockingSet(value);
        else if (d2.trackedEntityModule().trackedEntityAttributeValues.value(attributeUid, tei).blockingExists())
            d2.trackedEntityModule().trackedEntityAttributeValues.value(attributeUid, tei).blockingDelete();
    }
}

