package org.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.R;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by Cristian on 22/03/2018.
 */

public class EventSummaryRepositoryImpl implements EventSummaryRepository {


    private final FieldViewModelFactory fieldFactory;

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final FormRepository formRepository;

    @Nullable
    private final String eventUid;


    private static final List<String> SECTION_TABLES = Arrays.asList(
            "Event", "Program", "ProgramStage", "ProgramStageSection");
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
            "AND Event.state != '" + State.TO_DELETE.name() + "' ORDER BY ProgramStageSection.sortOrder";

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
            "        DataElement.displayFormName AS formLabel,\n" +
            "        DataElement.valueType AS type,\n" +
            "        DataElement.uid AS id,\n" +
            "        DataElement.optionSet AS optionSet,\n" +
            "        ProgramStageDataElement.sortOrder AS formOrder,\n" +
            "        ProgramStageDataElement.programStage AS stage,\n" +
            "        ProgramStageDataElement.compulsory AS mandatory,\n" +
            "        ProgramStageSectionDataElementLink.programStageSection AS section,\n" +
            "        ProgramStageDataElement.allowFutureDate AS allowFutureDate,\n" +
            "        DataElement.displayDescription AS displayDescription,\n" +
            "        ProgramStageSectionDataElementLink.sortOrder AS sectionOrder\n" +
            "      FROM ProgramStageDataElement\n" +
            "        INNER JOIN DataElement ON DataElement.uid = ProgramStageDataElement.dataElement\n" +
            "        LEFT JOIN ProgramStageSection ON ProgramStageSection.programStage = ProgramStageDataElement.programStage\n" +
            "        LEFT JOIN ProgramStageSectionDataElementLink ON ProgramStageSectionDataElementLink.programStageSection = ProgramStageSection.uid AND ProgramStageSectionDataElementLink.dataElement = DataElement.uid\n" + "    ) AS Field ON (Field.stage = Event.programStage)\n" +
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


    private final D2 d2;


    public EventSummaryRepositoryImpl(@NonNull Context context,
                                      @NonNull BriteDatabase briteDatabase,
                                      @NonNull FormRepository formRepository,
                                      @Nullable String eventUid,
                                      @NonNull D2 d2) {
        this.briteDatabase = briteDatabase;
        this.formRepository = formRepository;
        this.eventUid = eventUid;
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

    @NonNull
    @Override
    public Flowable<List<FormSectionViewModel>> programStageSections(String eventUid) {
        return briteDatabase
                .createQuery(SECTION_TABLES, SELECT_SECTIONS, eventUid == null ? "" : eventUid)
                .mapToList(cursor -> mapToFormSectionViewModels(eventUid, cursor))
                .distinctUntilChanged().toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public boolean isEnrollmentOpen() {
        boolean isEnrollmentOpen = true;
        if (d2.eventModule().events.byUid().eq(eventUid).one().blockingExists()) {
            isEnrollmentOpen = d2.enrollmentModule().enrollments.byUid()
                    .eq(d2.eventModule().events.byUid().eq(eventUid).one().blockingGet().enrollment())
                    .one().blockingGet().status() == EnrollmentStatus.ACTIVE;
        }
        return isEnrollmentOpen;
    }

    @NonNull
    private FormSectionViewModel mapToFormSectionViewModels(@NonNull String eventUid, @NonNull Cursor cursor) {
        // GET PROGRAMSTAGE DISPLAYNAME IN CASE THERE ARE NO SECTIONS
        if (cursor.getString(2) == null) {
            // This programstage has no sections
            return FormSectionViewModel.createForProgramStageWithLabel(eventUid, cursor.getString(4), cursor.getString(1));
        } else {
            // This programstage has sections
            return FormSectionViewModel.createForSection(eventUid, cursor.getString(2), cursor.getString(3), cursor.getString(5));
        }
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list(String sectionUid, String eventUid) {
        return briteDatabase
                .createQuery("TrackedEntityDataValue", prepareStatement(sectionUid, eventUid))
                .mapToList(this::transform)
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
    private FieldViewModel transform(@NonNull Cursor cursor) {
        String uid = cursor.getString(0);
        ValueType valueType = ValueType.valueOf(cursor.getString(2));
        String dataValue = cursor.getString(5);
        String optionCodeName = cursor.getString(6);
        EventStatus eventStatus = EventStatus.valueOf(cursor.getString(9));
        String formName = cursor.getString(10);
        String description = cursor.getString(11);
        String optionSet = cursor.getString(4);
        if (!isEmpty(optionCodeName)) {
            dataValue = optionCodeName;
        }

        int optionCount = 0;
        if (optionSet != null)
            try (Cursor countCursor = briteDatabase.query("SELECT COUNT (uid) FROM Option WHERE optionSet = ?", optionSet)) {
                if (countCursor != null && countCursor.moveToFirst())
                    optionCount = countCursor.getInt(0);
            } catch (Exception e) {
                Timber.e(e);
            }

        ValueTypeDeviceRendering fieldRendering = null;
        try (Cursor rendering = briteDatabase.query("SELECT * FROM ValueTypeDeviceRendering WHERE uid = ?", uid)) {
            if (rendering != null && rendering.moveToFirst()) {
                fieldRendering = ValueTypeDeviceRendering.create(rendering);
            }
        }

        ObjectStyle objectStyle = ObjectStyle.builder().build();
        try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", uid)) {
            if (objStyleCursor != null && objStyleCursor.moveToFirst())
                objectStyle = ObjectStyle.create(objStyleCursor);
        }


        if (valueType == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(dataValue).blockingGet().displayName();
        }

        return fieldFactory.create(uid, formName == null ? cursor.getString(1) : formName,
                valueType, cursor.getInt(3) == 1,
                optionSet, dataValue, cursor.getString(7), cursor.getInt(8) == 1,
                eventStatus == EventStatus.ACTIVE, null, description, fieldRendering, optionCount, objectStyle);
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return queryDataValues(eventUid)
                .switchMap(this::queryEvent)
                .switchMap(
                        event -> formRepository.ruleEngine()
                                .switchMap(ruleEngine -> Flowable.fromCallable(ruleEngine.evaluate(event))
                                        .map(Result::success)
                                        .onErrorReturn(error -> Result.failure(new Exception(error)))
                                )
                );
    }

    @Override
    public Observable<Event> changeStatus(String eventUid) {
        return d2.eventModule().events.uid(eventUid).get()
                .map(event -> {
                    switch (event.status()) {
                        case ACTIVE:
                        case SKIPPED:
                        case VISITED:
                        case SCHEDULE:
                        case OVERDUE:
                            d2.eventModule().events.uid(event.uid()).setStatus(EventStatus.COMPLETED);
                            d2.eventModule().events.uid(event.uid()).setCompletedDate(DateUtils.getInstance().getToday());
                            break;
                        case COMPLETED:
                            d2.eventModule().events.uid(event.uid()).setStatus(EventStatus.ACTIVE);
                            break;
                    }
                    return event;
                }).toObservable();
    }

    @Override
    public Flowable<Event> getEvent(String eventId) {
        return d2.eventModule().events.uid(eventId).get().toFlowable();
    }

    @Override
    public Observable<Boolean> accessDataWrite(String eventId) {
        return d2.eventModule().events.uid(eventId).get()
                .map(Event::programStage)
                .flatMap(programStageUid -> d2.programModule().programStages.uid(programStageUid).get())
                .flatMap(programStage -> d2.programModule().programs.uid(programStage.program().uid()).get()
                        .map(program -> program.access().data().write() && programStage.access().data().write()))
                .toObservable();
    }

    @NonNull
    private Flowable<RuleEvent> queryEvent(@NonNull List<RuleDataValue> dataValues) {
        return d2.eventModule().events.uid(eventUid)
                .get()
                .map(event ->
                        RuleEvent.builder()
                                .event(eventUid)
                                .programStage(event.programStage())
                                .programStageName(d2.programModule().programStages.uid(event.programStage()).blockingGet().displayName())
                                .status(RuleEvent.Status.valueOf(event.status().name()))
                                .eventDate(event.eventDate())
                                .dueDate(event.dueDate())
                                .organisationUnit(event.organisationUnit())
                                .organisationUnitCode(d2.organisationUnitModule().organisationUnits.uid(event.organisationUnit()).blockingGet().code())
                                .dataValues(dataValues)
                                .build()
                ).toFlowable();
    }

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues(String eventUid) {
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> d2.trackedEntityModule().trackedEntityDataValues
                        .byEvent().eq(eventUid)
                        .get()
                        .toFlowable()
                        .flatMapIterable(list -> list)
                        .map(dataValue ->
                                RuleDataValue.create(event.eventDate(), event.programStage(), dataValue.dataElement(), dataValue.value())
                        ).toList()).toFlowable();
    }

    @Override
    public Observable<Program> getProgramWithId(String programUid) {
        return d2.programModule().programs.withAllChildren().byUid().eq(programUid).one().get().toObservable();
    }
}