package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.R;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRenderingModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramRuleAction;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramRuleModel;
import org.hisp.dhis.android.core.program.ProgramRuleVariableModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionDeviceRendering;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

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

    private static final String SECTION_RENDERING_TYPE = "SELECT ProgramStageSection.mobileRenderType FROM ProgramStageSection WHERE ProgramStageSection.uid = ?";
    private static final String ACCESS_QUERY = "SELECT ProgramStage.accessDataWrite FROM ProgramStage JOIN Event ON Event.programStage = ProgramStage.uid WHERE Event.uid = ?";
    private static final String PROGRAM_ACCESS_QUERY = "SELECT Program.accessDataWrite FROM Program JOIN Event ON Event.program = Program.uid WHERE Event.uid = ?";
    private static final String OPTIONS = "SELECT Option.uid, Option.displayName, Option.code FROM Option WHERE Option.optionSet = ?";

    private final BriteDatabase briteDatabase;
    private final String eventUid;
    private final FormRepository formRepository;
    private final D2 d2;
    private boolean accessDataWrite;
    private String lastUpdatedUid;

    public EventCaptureRepositoryImpl(Context context, BriteDatabase briteDatabase, FormRepository formRepository, String eventUid, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;
        this.formRepository = formRepository;
        this.d2 = d2;

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
    }

    @Override
    public boolean isEnrollmentOpen() {
        boolean isEnrollmentOpen = true;
        try (Cursor enrollmentCursor = briteDatabase.query("SELECT Enrollment.* FROM Enrollment JOIN Event ON Event.enrollment = Enrollment.uid WHERE Event.uid = ?", eventUid)) {
            if (enrollmentCursor != null && enrollmentCursor.moveToFirst()) {
                EnrollmentModel enrollment = EnrollmentModel.create(enrollmentCursor);
                isEnrollmentOpen = enrollment.enrollmentStatus() == EnrollmentStatus.ACTIVE;
            }
        }
        return isEnrollmentOpen;
    }

    @Override
    public boolean isEnrollmentCancelled() {
        boolean isEnrollmentCancelled = false;
        try (Cursor enrollmentCursor = briteDatabase.query("SELECT Enrollment.* FROM Enrollment JOIN Event ON Event.enrollment = Enrollment.uid WHERE Event.uid = ?", eventUid)) {
            if (enrollmentCursor != null && enrollmentCursor.moveToFirst()) {
                EnrollmentModel enrollment = EnrollmentModel.create(enrollmentCursor);
                isEnrollmentCancelled = enrollment.enrollmentStatus() == EnrollmentStatus.CANCELLED;
            }
        }
        return isEnrollmentCancelled;
    }

    @Override
    public Flowable<String> programStageName() {
        return briteDatabase.createQuery(ProgramStageModel.TABLE,
                "SELECT ProgramStage.* FROM ProgramStage " +
                        "JOIN Event ON Event.programStage = ProgramStage.uid " +
                        "WHERE Event.uid = ? LIMIT 1", eventUid)
                .mapToOne(cursor -> ProgramStageModel.create(cursor).displayName())
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<String> eventDate() {
        return briteDatabase.createQuery(ProgramStageModel.TABLE,
                "SELECT Event.* FROM Event " +
                        "WHERE Event.uid = ? LIMIT 1", eventUid)
                .mapToOne(cursor -> EventModel.create(cursor).eventDate())
                .map(eventDate -> DateUtils.uiDateFormat().format(eventDate))
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<String> orgUnit() {
        return briteDatabase.createQuery(ProgramStageModel.TABLE,
                "SELECT OrganisationUnit.* FROM OrganisationUnit " +
                        "JOIN Event ON Event.organisationUnit = OrganisationUnit.uid " +
                        "WHERE Event.uid = ? LIMIT 1", eventUid)
                .mapToOne(cursor -> OrganisationUnitModel.create(cursor).displayName())
                .toFlowable(BackpressureStrategy.LATEST);
    }


    @Override
    public Flowable<String> catOption() {
        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE,
                "SELECT CategoryOptionCombo.* FROM CategoryOptionCombo " +
                        "JOIN Event ON Event.attributeOptionCombo = CategoryOptionCombo.uid " +
                        "WHERE Event.uid = ? LIMIT 1", eventUid)
                .mapToOneOrDefault(cursor -> CategoryOptionComboModel.create(cursor).displayName(), "")
                .toFlowable(BackpressureStrategy.LATEST);
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
        accessDataWrite = getAccessDataWrite();
        return briteDatabase
                .createQuery(TrackedEntityDataValueModel.TABLE, prepareStatement(sectionUid, eventUid))
                .mapToList(this::transform)
                .map(this::checkRenderType)
                .toFlowable(BackpressureStrategy.LATEST);
    }

    private ProgramStageSectionRenderingType renderingType(String sectionUid) {
        ProgramStageSectionRenderingType renderingType = ProgramStageSectionRenderingType.LISTING;
        if (sectionUid != null) {
            ProgramStageSectionDeviceRendering stageSectionRendering = d2.programModule().programStageSections.uid(sectionUid).get().renderType().mobile();
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
        accessDataWrite = getAccessDataWrite();
        return briteDatabase
                .createQuery(TrackedEntityDataValueModel.TABLE, prepareStatement(eventUid))
                .mapToList(this::transform)
                .map(this::checkRenderType)
                .toFlowable(BackpressureStrategy.LATEST);
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

        ProgramStageSectionRenderingType renderingType = renderingType(programStageSection);

        return fieldFactory.create(uid, formName == null ? displayName : formName,
                ValueType.valueOf(valueTypeName), mandatory, optionSet, dataValue,
                programStageSection, allowFurureDates,
                isEnrollmentOpen() && eventStatus == EventStatus.ACTIVE && accessDataWrite,
                renderingType, description, fieldRendering, optionCount, objectStyle);
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return queryDataValues(eventUid)
                .switchMap(this::queryEvent)
                .switchMap(
                        event -> formRepository.ruleEngine()
                                .switchMap(ruleEngine -> {
                                    if (isEmpty(lastUpdatedUid))
                                        return Flowable.fromCallable(ruleEngine.evaluate(event));
                                    else
                                        return getRulesFor(lastUpdatedUid)
                                                .flatMap(rules -> {
                                                    if (!rules.isEmpty())
                                                        return Flowable.fromCallable(ruleEngine.evaluate(event, rules));
                                                    else
                                                        return Flowable.just(new ArrayList<RuleEffect>());
                                                });
                                })
                                .map(Result::success)
                                .onErrorReturn(error -> Result.failure(new Exception(error)))
                );
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate(String lastUpdatedElement) {
        if (lastUpdatedElement == null)
            return calculate();
        else
            return queryDataValues(eventUid)
                    .switchMap(this::queryEvent)
                    .flatMap(event ->
                            Flowable.zip(Flowable.just(event),
                                    getRulesFor(lastUpdatedElement),
                                    Pair::create))
                    .switchMap(
                            eventAndRules -> formRepository.ruleEngine()
                                    .switchMap(ruleEngine -> Flowable.fromCallable(ruleEngine.evaluate(eventAndRules.val0(), eventAndRules.val1()))
                                            .map(Result::success)
                                            .onErrorReturn(error -> Result.failure(new Exception(error)))
                                    )
                    );
    }

    private Flowable<List<Rule>> getRulesFor(String lastUpdatedElement) {
        AtomicReference<String> selectedProgramUid = new AtomicReference<>("");
        return Observable.just(d2.eventModule().events.uid(eventUid).get().program())
                .flatMap(programUid -> {
                            selectedProgramUid.set(programUid);
                            return briteDatabase.createQuery(ProgramRuleVariableModel.TABLE,
                                    "SELECT * FROM ProgramRuleVariable WHERE program = ? AND dataElement = ?", programUid, lastUpdatedElement)
                                    .mapToList(cursor -> "%" + ProgramRuleVariableModel.create(cursor).displayName() + "%");
                        }
                ).flatMap(variableList -> {
                    String likeCondition = "condition LIKE '%s' OR data LIKE '%s'";
                    StringBuilder st = new StringBuilder();
                    for (int i = 0; i < variableList.size(); i++) {
                        st.append(String.format(likeCondition, variableList.get(i), variableList.get(i)));
                        if (i != variableList.size() - 1)
                            st.append(" OR ");
                    }

                    if (!isEmpty(st))
                        return briteDatabase.createQuery(ProgramRuleModel.TABLE,
                                String.format("SELECT ProgramRule.* FROM ProgramRule " +
                                        "LEFT JOIN ProgramRuleAction ON ProgramRuleAction.programRule = ProgramRule.uid " +
                                        "WHERE program = ? AND %s", st.toString()), selectedProgramUid.get())
                                .mapToList(cursor -> {
                                    ProgramRuleModel ruleModel = ProgramRuleModel.create(cursor);
                                    List<RuleAction> ruleActions = new ArrayList<>();
                                    try (Cursor actionsCursor = briteDatabase.query(
                                            "SELECT " +
                                                    "ProgramRuleAction.programRule, " +
                                                    "ProgramRuleAction.programStage, " +
                                                    "ProgramRuleAction.programStageSection, " +
                                                    "ProgramRuleAction.programRuleActionType, " +
                                                    "ProgramRuleAction.programIndicator, " +
                                                    "ProgramRuleAction.trackedEntityAttribute, " +
                                                    "ProgramRuleAction.dataElement, " +
                                                    "ProgramRuleAction.location, " +
                                                    "ProgramRuleAction.content, " +
                                                    "ProgramRuleAction.data, " +
                                                    "ProgramRuleAction.option, " +
                                                    "ProgramRuleAction.optionGroup " +
                                                    "FROM ProgramRuleAction WHERE programRule = ?", ruleModel.uid())) {
                                        if (actionsCursor != null) {
                                            if (actionsCursor.moveToFirst()) {
                                                for (int i = 0; i < actionsCursor.getCount(); i++) {
                                                    ruleActions.add(RulesRepository.create(actionsCursor));
                                                    actionsCursor.moveToNext();
                                                }
                                            }
                                        }
                                    }

                                    return Rule.create(ruleModel.programStage(), ruleModel.priority(), ruleModel.condition(), ruleActions, ruleModel.displayName());
                                });
                    else
                        return Observable.just(new ArrayList<Rule>());
                }).map(ruleList -> {
                    Map<String, Rule> ruleMap = new HashMap<>();
                    for (Rule rule : ruleList)
                        ruleMap.put(rule.name(), rule);

                    try (Cursor hideRulesCursor = briteDatabase.query("SELECT ProgramRule.* FROM ProgramRule " +
                                    "JOIN ProgramRuleAction ON ProgramRuleAction.programRule = ProgramRule.uid " +
                                    "WHERE ProgramRule.program = ? " +
                                    "AND ProgramRuleAction.programRuleActionType IN (?,?,?,?,?,?,?)",
                            selectedProgramUid.get(),
                            ProgramRuleActionType.HIDEFIELD.name(), ProgramRuleActionType.HIDESECTION.name(),
                            ProgramRuleActionType.ASSIGN.name(), ProgramRuleActionType.SHOWERROR.name(),
                            ProgramRuleActionType.SHOWWARNING.name(),
                            ProgramRuleActionType.HIDEOPTION.name(), ProgramRuleActionType.HIDEOPTIONGROUP.name())) {
                        if (hideRulesCursor != null) {
                            if (hideRulesCursor.moveToFirst()) {
                                for (int i = 0; i < hideRulesCursor.getCount(); i++) {
                                    ProgramRuleModel ruleModel = ProgramRuleModel.create(hideRulesCursor);
                                    ruleMap.put(ruleModel.displayName(), Rule.create(ruleModel.programStage(), ruleModel.priority(), ruleModel.condition(), getRuleActionsFor(ruleModel.uid()), ruleModel.displayName()));
                                    hideRulesCursor.moveToNext();
                                }
                            }
                        }
                    }
                    return (List<Rule>) new ArrayList<>(ruleMap.values());
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    private List<RuleAction> getRuleActionsFor(String
                                                       programRuleUid) {
        List<RuleAction> ruleActions = new ArrayList<>();
        List<ProgramRuleAction> ruleActionsModule = d2.programModule().programRules.uid(programRuleUid).getWithAllChildren().programRuleActions();
        for (ProgramRuleAction ruleAction : ruleActionsModule) {
            ruleActions.add(RulesRepository.create(ruleAction.programRuleActionType(),
                    ruleAction.programStage() != null ? ruleAction.programStage().uid() : null,
                    ruleAction.programStageSection() != null ? ruleAction.programStageSection().uid() : null,
                    ruleAction.trackedEntityAttribute() != null ? ruleAction.trackedEntityAttribute().uid() : null,
                    ruleAction.dataElement() != null ? ruleAction.dataElement().uid() : null,
                    ruleAction.location(), ruleAction.content(), ruleAction.data(),
                    ruleAction.option() != null ? ruleAction.option().uid() : null,
                    ruleAction.optionGroup() != null ? ruleAction.optionGroup().uid() : null));
        }
        return ruleActions;
    }

    @Override
    public Observable<Boolean> completeEvent() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.STATUS, EventStatus.COMPLETED.name());
        String completeDate = DateUtils.databaseDateFormat().format(DateUtils.getInstance().getToday());
        contentValues.put(EventModel.Columns.COMPLETE_DATE, completeDate);
        return Observable.just(briteDatabase.update(EventModel.TABLE, contentValues, "uid = ?", eventUid) > 0);
    }

    @Override
    public boolean reopenEvent() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.STATUS, EventStatus.ACTIVE.name());
        return briteDatabase.update(EventModel.TABLE, contentValues, "uid = ?", eventUid) > 0;
    }

    @Override
    public Observable<Boolean> deleteEvent() {
        Cursor eventCursor = briteDatabase.query("SELECT Event.* FROM Event WHERE Event.uid = ?", eventUid);
        long status = -1;
        if (eventCursor != null && eventCursor.moveToNext()) {
            EventModel eventModel = EventModel.create(eventCursor);
            if (eventModel.state() == State.TO_POST) {
                String DELETE_WHERE = String.format(
                        "%s.%s = ?",
                        EventModel.TABLE, EventModel.Columns.UID
                );
                status = briteDatabase.delete(EventModel.TABLE, DELETE_WHERE, eventUid);
            } else {
                ContentValues contentValues = eventModel.toContentValues();
                contentValues.put(EventModel.Columns.STATE, State.TO_DELETE.name());
                status = briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + " = ?", eventUid);
            }
            if (status == 1 && eventModel.enrollment() != null)
                updateEnrollment(eventModel.enrollment());
        }
        return Observable.just(status == 1);
    }

    private void updateEnrollment(String enrollmentUid) {
        String SELECT_ENROLLMENT = "SELECT *\n" +
                "FROM Enrollment\n" +
                "WHERE uid = ? LIMIT 1;";
        try (Cursor enrollmentCursor = briteDatabase.query(SELECT_ENROLLMENT, enrollmentUid)) {
            if (enrollmentCursor != null && enrollmentCursor.moveToFirst()) {
                EnrollmentModel enrollmentModel = EnrollmentModel.create(enrollmentCursor);

                ContentValues cv = enrollmentModel.toContentValues();
                cv.put(EnrollmentModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
                cv.put(EnrollmentModel.Columns.STATE, enrollmentModel.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
                briteDatabase.update(EnrollmentModel.TABLE, cv, "uid = ?", enrollmentUid);
                updateTei(enrollmentModel.trackedEntityInstance());
            }
        }
    }

    private void updateTei(String teiUid) {
        String selectTei = "SELECT * FROM TrackedEntityInstance WHERE uid = ?";
        try (Cursor teiCursor = briteDatabase.query(selectTei, teiUid)) {
            if (teiCursor != null && teiCursor.moveToFirst()) {
                TrackedEntityInstanceModel teiModel = TrackedEntityInstanceModel.create(teiCursor);
                ContentValues cv = teiModel.toContentValues();
                cv.put(TrackedEntityInstanceModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
                cv.put(TrackedEntityInstanceModel.Columns.STATE,
                        teiModel.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
                briteDatabase.update(TrackedEntityInstanceModel.TABLE, cv, "uid = ?", teiUid);
            }
        }
    }

    @Override
    public Observable<Boolean> updateEventStatus(EventStatus
                                                         status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.STATUS, status.name());
        String updateDate = DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime());
        contentValues.put(EventModel.Columns.LAST_UPDATED, updateDate);
        return Observable.just(briteDatabase.update(EventModel.TABLE, contentValues, "uid = ?", eventUid) > 0);
    }

    @Override
    public Observable<Boolean> rescheduleEvent(Date newDate) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.DUE_DATE, DateUtils.databaseDateFormat().format(newDate));
        return Observable.just(briteDatabase.update(EventModel.TABLE, contentValues, "uid = ?", eventUid))
                .flatMap(result -> updateEventStatus(EventStatus.SCHEDULE));
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
        return briteDatabase.createQuery(EventModel.TABLE, "SELECT Event.status FROM Event WHERE Event.uid = ?", eventUid)
                .mapToOne(cursor -> EventStatus.valueOf(cursor.getString(0)))
                .toFlowable(BackpressureStrategy.LATEST);
    }

    private static final String QUERY_EVENT = "SELECT Event.uid,\n" +
            "  Event.programStage,\n" +
            "  Event.status,\n" +
            "  Event.eventDate,\n" +
            "  Event.dueDate,\n" +
            "  Event.organisationUnit,\n" +
            "  ProgramStage.displayName\n" +
            "FROM Event\n" +
            "JOIN ProgramStage ON ProgramStage.uid = Event.programStage\n" +
            "WHERE Event.uid = ?\n" +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'\n" +
            "LIMIT 1;";

    @NonNull
    private Flowable<RuleEvent> queryEvent
            (@NonNull List<RuleDataValue> dataValues) {
        return briteDatabase.createQuery(EventModel.TABLE, QUERY_EVENT, eventUid == null ? "" : eventUid)
                .mapToOne(cursor -> {
                    Date eventDate = cursor.isNull(3) ? parseDate(cursor.getString(4)) : parseDate(cursor.getString(3));
                    Date dueDate = cursor.isNull(4) ? eventDate : parseDate(cursor.getString(4));
                    String orgUnit = cursor.getString(5);
                    String orgUnitCode = getOrgUnitCode(orgUnit);
                    String programStageName = cursor.getString(6);
                    RuleEvent.Status status = RuleEvent.Status.valueOf(cursor.getString(2));

                    return RuleEvent.builder()
                            .event(cursor.getString(0))
                            .programStage(cursor.getString(1))
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

    @Nonnull
    private String getOrgUnitCode(String orgUnitUid) {
        String ouCode = "";
        try (Cursor cursor = briteDatabase.query("SELECT code FROM OrganisationUnit WHERE uid = ? LIMIT 1", orgUnitUid)) {
            if (cursor != null && cursor.moveToFirst() && cursor.getString(0) != null) {
                ouCode = cursor.getString(0);
            }
        }
        return ouCode;
    }

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

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues
            (String eventUid) {
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
                    return RuleDataValue.create(eventDate, programStage,
                            dataElement, value);
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private static Date parseDate(@NonNull String date) {
        try {
            return BaseIdentifiableObject.DATE_FORMAT.parse(date);
        } catch (ParseException parseException) {
            throw new RuntimeException(parseException);
        }
    }

    private HashMap<String, Pair<FormSectionViewModel, Boolean>> switchToMap
            (List<FormSectionViewModel> list) {
        HashMap<String, Pair<FormSectionViewModel, Boolean>> sectionsMap = new HashMap<>();
        for (FormSectionViewModel formSection : list) {
            sectionsMap.put(formSection.sectionUid(), Pair.create(formSection, true));
        }
        return sectionsMap;
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
}
