package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.R;
import org.dhis2.data.forms.FieldViewModelUtils;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRenderingModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageDataElementModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.AND;
import static org.dhis2.data.database.SqlConstants.AS;
import static org.dhis2.data.database.SqlConstants.COMMA;
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.JOIN;
import static org.dhis2.data.database.SqlConstants.LEFT_OUTER_JOIN;
import static org.dhis2.data.database.SqlConstants.LIMIT_1;
import static org.dhis2.data.database.SqlConstants.NOT_EQUAL;
import static org.dhis2.data.database.SqlConstants.ON;
import static org.dhis2.data.database.SqlConstants.ORDER_BY;
import static org.dhis2.data.database.SqlConstants.POINT;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.QUOTE;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.TABLE_POINT_FIELD_EQUALS;
import static org.dhis2.data.database.SqlConstants.VARIABLE;
import static org.dhis2.data.database.SqlConstants.WHERE;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCaptureRepositoryImpl implements EventCaptureContract.EventCaptureRepository {

    private final FieldViewModelFactory fieldFactory;


    private static final List<String> SECTION_TABLES = Arrays.asList(
            EventModel.TABLE, ProgramModel.TABLE, ProgramStageModel.TABLE, ProgramStageSectionModel.TABLE);

    private static final String SELECT_SECTIONS = SELECT +
            ProgramModel.TABLE + POINT + ProgramModel.Columns.UID + AS + "programUid" + COMMA +
            ProgramStageModel.TABLE + POINT + ProgramStageModel.Columns.UID + AS + "programStageUid" + COMMA +
            ProgramStageSectionModel.TABLE + POINT + ProgramStageSectionModel.Columns.UID + AS + "programStageSectionUid" + COMMA +
            ProgramStageSectionModel.TABLE + POINT + ProgramStageSectionModel.Columns.DISPLAY_NAME + AS + "programStageSectionDisplayName" + COMMA +
            ProgramStageModel.TABLE + POINT + ProgramStageModel.Columns.DISPLAY_NAME + AS + "programStageDisplayName" + COMMA +
            ProgramStageSectionModel.TABLE + POINT + ProgramStageSectionModel.Columns.MOBILE_RENDER_TYPE + AS + "renderType" +
            FROM + EventModel.TABLE +
            JOIN + ProgramModel.TABLE +
            ON + EventModel.TABLE + POINT + EventModel.Columns.PROGRAM +
            EQUAL + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID +
            JOIN + ProgramStageModel.TABLE +
            ON + EventModel.TABLE + POINT + EventModel.Columns.PROGRAM_STAGE +
            EQUAL + ProgramStageModel.TABLE + POINT + ProgramStageModel.Columns.UID +
            LEFT_OUTER_JOIN + ProgramStageSectionModel.TABLE +
            ON + ProgramStageSectionModel.TABLE + POINT + ProgramStageSectionModel.Columns.PROGRAM_STAGE +
            EQUAL + EventModel.TABLE + POINT + EventModel.Columns.PROGRAM_STAGE +
            WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID +
            EQUAL + QUESTION_MARK +
            AND + EventModel.TABLE + POINT + EventModel.Columns.STATE + NOT_EQUAL + QUOTE + State.TO_DELETE + QUOTE +
            ORDER_BY + ProgramStageSectionModel.TABLE + POINT + ProgramStageSectionModel.Columns.SORT_ORDER;

    private static final String QUERY = "SELECT\n" +
            "  Field.id,\n" +
            "  Field.label,\n" +
            "  Field.type,\n" +
            "  Field.mandatory,\n" +
            "  Field.optionSet,\n" +
            "  Value.VALUE,\n" +
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
            "        LEFT JOIN ProgramStageSectionDataElementLink ON ProgramStageSectionDataElementLink.dataElement = ProgramStageDataElement.dataElement\n" +
            "    ) AS Field ON (Field.stage = Event.programStage)\n" +
            "  LEFT OUTER JOIN TrackedEntityDataValue AS Value ON (\n" +
            "    Value.event = Event.uid AND Value.dataElement = Field.id\n" +
            "  )\n" +
            "  LEFT OUTER JOIN Option ON (\n" +
            "    Field.optionSet = Option.optionSet AND Value.VALUE = Option.code\n" +
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
    private ProgramStageSectionRenderingType renderingType;
    private boolean accessDataWrite;

    public EventCaptureRepositoryImpl(Context context, BriteDatabase briteDatabase, FormRepository formRepository, String eventUid) {
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;
        this.formRepository = formRepository;
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
        Cursor enrollmentCursor = briteDatabase.query("SELECT Enrollment.* FROM Enrollment JOIN Event ON Event.enrollment = Enrollment.uid WHERE Event.uid = ?", eventUid);
        if (enrollmentCursor != null) {
            if (enrollmentCursor.moveToFirst()) {
                EnrollmentModel enrollment = EnrollmentModel.create(enrollmentCursor);
                isEnrollmentOpen = enrollment.enrollmentStatus() == EnrollmentStatus.ACTIVE;
            }
            enrollmentCursor.close();
        }
        return isEnrollmentOpen;
    }

    @Override
    public Flowable<String> programStageName() {
        return briteDatabase.createQuery(ProgramStageModel.TABLE,
                SELECT + ProgramStageModel.TABLE + POINT + ALL + FROM + ProgramStageModel.TABLE +
                        JOIN + EventModel.TABLE + ON + EventModel.TABLE + EventModel.Columns.PROGRAM_STAGE +
                        EQUAL + ProgramStageModel.TABLE + POINT + ProgramStageModel.Columns.UID +
                        WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID +
                        EQUAL + QUESTION_MARK + LIMIT_1, eventUid)
                .mapToOne(cursor -> ProgramStageModel.create(cursor).displayName())
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<String> eventDate() {
        return briteDatabase.createQuery(ProgramStageModel.TABLE,
                SELECT + EventModel.TABLE + POINT + ALL + FROM + EventModel.TABLE +
                        WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID +
                        EQUAL + QUESTION_MARK + LIMIT_1, eventUid)
                .mapToOne(cursor -> EventModel.create(cursor).eventDate())
                .map(eventDate -> DateUtils.uiDateFormat().format(eventDate))
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<String> orgUnit() {
        return briteDatabase.createQuery(ProgramStageModel.TABLE,
                SELECT + OrganisationUnitModel.TABLE + POINT + ALL +
                        FROM + OrganisationUnitModel.TABLE +
                        JOIN + EventModel.TABLE + ON + EventModel.TABLE + POINT + EventModel.Columns.ORGANISATION_UNIT +
                        EQUAL + OrganisationUnitModel.TABLE + POINT + OrganisationUnitModel.Columns.UID +
                        WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID +
                        EQUAL + QUESTION_MARK + LIMIT_1, eventUid)
                .mapToOne(cursor -> OrganisationUnitModel.create(cursor).displayName())
                .toFlowable(BackpressureStrategy.LATEST);
    }


    @Override
    public Flowable<String> catOption() {
        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE,
                SELECT + CategoryOptionComboModel.TABLE + POINT + ALL +
                        FROM + CategoryOptionComboModel.TABLE +
                        JOIN + EventModel.TABLE + ON + EventModel.TABLE + POINT + EventModel.Columns.ATTRIBUTE_OPTION_COMBO +
                        EQUAL + CategoryOptionComboModel.TABLE + POINT + CategoryOptionComboModel.Columns.UID +
                        WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID +
                        EQUAL + QUESTION_MARK + LIMIT_1, eventUid)
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

        Cursor cursor = briteDatabase.query(SECTION_RENDERING_TYPE, sectionUid == null ? "" : sectionUid);
        if (cursor != null && cursor.moveToFirst()) {
            renderingType = cursor.getString(0) != null ?
                    ProgramStageSectionRenderingType.valueOf(cursor.getString(0)) :
                    ProgramStageSectionRenderingType.LISTING;
            cursor.close();
        } else
            renderingType = ProgramStageSectionRenderingType.LISTING;

        Cursor accessCursor = briteDatabase.query(ACCESS_QUERY, eventUid == null ? "" : eventUid);
        if (accessCursor != null && accessCursor.moveToFirst()) {
            accessDataWrite = accessCursor.getInt(0) == 1;
            accessCursor.close();
        }

        Cursor programAccessCursor = briteDatabase.query(PROGRAM_ACCESS_QUERY, eventUid == null ? "" : eventUid);
        if (programAccessCursor != null && programAccessCursor.moveToFirst()) {
            accessDataWrite = accessDataWrite && programAccessCursor.getInt(0) == 1;
            programAccessCursor.close();
        }

        return briteDatabase
                .createQuery(TrackedEntityDataValueModel.TABLE, prepareStatement(sectionUid, eventUid))
                .mapToList(this::transform)
                .map(this::checkRenderType)
                .toFlowable(BackpressureStrategy.LATEST);
    }

    private ArrayList<FieldViewModel> getRenderList(Cursor cursor, FieldViewModel fieldViewModel) {
        ArrayList<FieldViewModel> renderList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                String uid = cursor.getString(0);
                String displayName = cursor.getString(1);
                String optionCode = cursor.getString(2);

                ValueTypeDeviceRenderingModel fieldRendering = null;
                Cursor rendering = briteDatabase.query(SELECT + ValueTypeDeviceRenderingModel.TABLE + POINT + ALL +
                        FROM + ValueTypeDeviceRenderingModel.TABLE +
                        JOIN + ProgramStageDataElementModel.TABLE +
                        ON + ProgramStageDataElementModel.TABLE + POINT + ProgramStageDataElementModel.Columns.UID +
                        EQUAL + ValueTypeDeviceRenderingModel.Columns.UID +
                        WHERE + ProgramStageDataElementModel.TABLE + POINT + ProgramStageDataElementModel.Columns.UID +
                        EQUAL + QUESTION_MARK, uid);

                if (rendering != null && rendering.moveToFirst()) {
                    fieldRendering = ValueTypeDeviceRenderingModel.create(cursor);
                    rendering.close();
                }

                renderList.add(fieldFactory.create(
                        fieldViewModel.uid() + "." + uid, //fist
                        displayName + "-" + optionCode, ValueType.TEXT, false,
                        fieldViewModel.optionSet(), fieldViewModel.value(), fieldViewModel.programStageSection(),
                        fieldViewModel.allowFutureDate(), fieldViewModel.editable() == null ? false : fieldViewModel.editable(), renderingType, fieldViewModel.description(), fieldRendering));

                cursor.moveToNext();
            }
            cursor.close();
        }
        return renderList;
    }

    private List<FieldViewModel> checkRenderType(List<FieldViewModel> fieldViewModels) {
        ArrayList<FieldViewModel> renderList = new ArrayList<>();

        if (renderingType != ProgramStageSectionRenderingType.LISTING) {
            for (FieldViewModel fieldViewModel : fieldViewModels) {
                if (!isEmpty(fieldViewModel.optionSet())) {
                    Cursor cursor = briteDatabase.query(OPTIONS, fieldViewModel.optionSet() == null ? "" : fieldViewModel.optionSet());
                    renderList.addAll(getRenderList(cursor, fieldViewModel));
                } else
                    renderList.add(fieldViewModel);
            }
        } else
            renderList.addAll(fieldViewModels);

        return renderList;

    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list() {
        return briteDatabase
                .createQuery(TrackedEntityDataValueModel.TABLE, prepareStatement(eventUid))
                .mapToList(this::transform)
                .toFlowable(BackpressureStrategy.LATEST);
    }


    @NonNull
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    private String prepareStatement(String sectionUid, String eventUid) {
        String where;
        if (isEmpty(sectionUid)) {
            where = String.format(Locale.US, WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID +
                    EQUAL + QUOTE + VARIABLE + QUOTE, eventUid == null ? "" : eventUid);
        } else {
            where = String.format(Locale.US, WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID +
                    EQUAL + QUOTE + VARIABLE + QUOTE +
                    AND + "Field.section" +
                    EQUAL + QUOTE + VARIABLE + QUOTE, eventUid == null ? "" : eventUid, sectionUid);
        }

        return String.format(Locale.US, QUERY, where);
    }

    private StringBuilder getSectionUids(Cursor sectionsCursor) {
        StringBuilder sectionUids = new StringBuilder();
        if (sectionsCursor != null && sectionsCursor.moveToFirst()) {
            for (int i = 0; i < sectionsCursor.getCount(); i++) {
                if (sectionsCursor.getString(2) != null)
                    sectionUids.append(String.format("'%s'", sectionsCursor.getString(2)));
                if (i < sectionsCursor.getCount() - 1)
                    sectionUids.append(",");
                sectionsCursor.moveToNext();
            }
            sectionsCursor.close();
        }
        return sectionUids;
    }

    @NonNull
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    private String prepareStatement(String eventUid) {

        Cursor sectionsCursor = briteDatabase.query(SELECT_SECTIONS, eventUid);
        StringBuilder sectionUids = getSectionUids(sectionsCursor);

        String where;
        if (isEmpty(sectionUids)) {
            where = String.format(Locale.US, WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID +
                    EQUAL + QUOTE + VARIABLE + QUOTE, eventUid == null ? "" : eventUid);
        } else {
            where = String.format(Locale.US, WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID +
                    EQUAL + QUOTE + VARIABLE + QUOTE + AND + "Field.section IN (%s)", eventUid == null ? "" : eventUid, sectionUids);
        }

        return String.format(Locale.US, QUERY, where);
    }

    @NonNull
    private FieldViewModel transform(@NonNull Cursor cursor) {
        FieldViewModelUtils fieldViewModelUtils = new FieldViewModelUtils(cursor);
        ValueTypeDeviceRenderingModel fieldRendering = null;
        try {
            Cursor rendering = briteDatabase.query("SELECT ValueTypeDeviceRendering.* FROM ValueTypeDeviceRendering" +
                    " JOIN ProgramStageDataElement ON ProgramStageDataElement.uid = ValueTypeDeviceRendering.uid" +
                    " WHERE ProgramStageDataElement.dataElement = ?", fieldViewModelUtils.getUid());
            if (rendering != null) {
                if (rendering.moveToFirst())
                    fieldRendering = ValueTypeDeviceRenderingModel.create(rendering);
                rendering.close();
            }
        } catch (Exception e) {
            Timber.e(e);
        }


        return fieldFactory.create(fieldViewModelUtils.getUid(),
                fieldViewModelUtils.getFormLabel() == null ? cursor.getString(1) : fieldViewModelUtils.getFormLabel(),
                ValueType.valueOf(cursor.getString(2)), cursor.getInt(3) == 1,
                cursor.getString(4), fieldViewModelUtils.getDataValue(), cursor.getString(7), cursor.getInt(8) == 1,
                isEnrollmentOpen() && fieldViewModelUtils.getEventStatus() == EventStatus.ACTIVE && accessDataWrite,
                null, fieldViewModelUtils.getDescription(), fieldRendering);
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
    public Observable<Boolean> completeEvent() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.STATUS, EventStatus.COMPLETED.name());
        String completeDate = DateUtils.databaseDateFormat().format(DateUtils.getInstance().getToday());
        contentValues.put(EventModel.Columns.COMPLETE_DATE, completeDate);
        return Observable.just(briteDatabase.update(EventModel.TABLE, contentValues,
                EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventUid) > 0);
    }

    @Override
    public boolean reopenEvent() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.STATUS, EventStatus.ACTIVE.name());
        return briteDatabase.update(EventModel.TABLE, contentValues,
                EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventUid) > 0;
    }

    @Override
    public Observable<Boolean> deleteEvent() {
        Cursor eventCursor = briteDatabase.query(SELECT + EventModel.TABLE + POINT + ALL + FROM +
                EventModel.TABLE + WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventUid);
        long status = -1;
        if (eventCursor != null && eventCursor.moveToNext()) {
            EventModel eventModel = EventModel.create(eventCursor);
            if (eventModel.state() == State.TO_POST) {
                String deleteWhere = String.format(
                        TABLE_POINT_FIELD_EQUALS + QUESTION_MARK,
                        EventModel.TABLE, EventModel.Columns.UID
                );
                status = briteDatabase.delete(EventModel.TABLE, deleteWhere, eventUid);
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
        String selectEnrollment = SELECT + ALL + FROM + EnrollmentModel.TABLE + WHERE + EnrollmentModel.Columns.UID + EQUAL +
                QUESTION_MARK + LIMIT_1 + ";";
        Cursor enrollmentCursor = briteDatabase.query(selectEnrollment, enrollmentUid);
        if (enrollmentCursor != null && enrollmentCursor.moveToFirst()) {
            EnrollmentModel enrollmentModel = EnrollmentModel.create(enrollmentCursor);

            ContentValues cv = enrollmentModel.toContentValues();
            cv.put(EnrollmentModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
            cv.put(EnrollmentModel.Columns.STATE, enrollmentModel.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
            briteDatabase.update(EnrollmentModel.TABLE, cv, EventModel.Columns.UID + EQUAL + QUESTION_MARK, enrollmentUid);

            enrollmentCursor.close();

            updateTei(enrollmentModel.trackedEntityInstance());
        }

    }

    private void updateTei(String teiUid) {
        String selectTei = SELECT + ALL + FROM + TrackedEntityInstanceModel.TABLE + WHERE + TrackedEntityInstanceModel.Columns.UID + EQUAL + QUESTION_MARK;
        Cursor teiCursor = briteDatabase.query(selectTei, teiUid);
        if (teiCursor != null && teiCursor.moveToFirst()) {
            TrackedEntityInstanceModel teiModel = TrackedEntityInstanceModel.create(teiCursor);
            ContentValues cv = teiModel.toContentValues();
            cv.put(TrackedEntityInstanceModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
            cv.put(TrackedEntityInstanceModel.Columns.STATE,
                    teiModel.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
            briteDatabase.update(TrackedEntityInstanceModel.TABLE, cv,
                    TrackedEntityInstanceModel.Columns.UID + EQUAL + QUESTION_MARK, teiUid);
            teiCursor.close();
        }
    }

    @Override
    public Observable<Boolean> updateEventStatus(EventStatus status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.STATUS, status.name());
        String updateDate = DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime());
        contentValues.put(EventModel.Columns.LAST_UPDATED, updateDate);
        return Observable.just(briteDatabase.update(EventModel.TABLE, contentValues,
                EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventUid) > 0);
    }

    @Override
    public Observable<Boolean> rescheduleEvent(Date newDate) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.DUE_DATE, DateUtils.databaseDateFormat().format(newDate));
        return Observable.just(briteDatabase.update(EventModel.TABLE, contentValues,
                EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventUid))
                .flatMap(result -> updateEventStatus(EventStatus.SCHEDULE));
    }

    @Override
    public Observable<String> programStage() {
        return briteDatabase.createQuery(EventModel.TABLE,
                SELECT + ALL + FROM + EventModel.TABLE + WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID +
                        EQUAL + QUESTION_MARK, eventUid)
                .mapToOne(EventModel::create)
                .map(EventModel::programStage);
    }

    @Override
    public boolean getAccessDataWrite() {
        boolean canWrite = true;
        Cursor programAccessData = briteDatabase.query("SELECT Program.* FROM Program JOIN Event ON Event.program = Program.uid WHERE Event.uid = ? ", eventUid);
        if (programAccessData != null) {
            if (programAccessData.moveToFirst()) {
                canWrite = ProgramModel.create(programAccessData).accessDataWrite();
                if (canWrite) {
                    Cursor stageAccessData = briteDatabase.query("SELECT ProgramStage.* FROM ProgramStage JOIN Event ON Event.programStage = ProgramStage.uid WHERE Event.uid = ? ", eventUid);
                    if (stageAccessData != null) {
                        if (stageAccessData.moveToFirst()) {
                            canWrite = ProgramStageModel.create(stageAccessData).accessDataWrite();
                        }
                        stageAccessData.close();
                    }
                }
            }
            programAccessData.close();
        }
        return canWrite;
    }

    @Override
    public Flowable<EventStatus> eventStatus() {
        return briteDatabase.createQuery(EventModel.TABLE,
                SELECT + EventModel.TABLE + POINT + EventModel.Columns.STATUS + FROM + EventModel.TABLE +
                        WHERE + EventModel.TABLE + POINT + EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventUid)
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
    private Flowable<RuleEvent> queryEvent(@NonNull List<RuleDataValue> dataValues) {
        return briteDatabase.createQuery(EventModel.TABLE, QUERY_EVENT, eventUid == null ? "" : eventUid)
                .mapToOne(cursor -> {
                    Date eventDate = parseDate(cursor.getString(3));
                    Date dueDate = cursor.isNull(4) ? eventDate : parseDate(cursor.getString(4));
                    String orgUnit = cursor.getString(5);
                    String orgUnitCode = formRepository.getOrgUnitCode(orgUnit);
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

    private static final String QUERY_VALUES = "SELECT " +
            "  eventDate," +
            "  programStage," +
            "  dataElement," +
            "  VALUE" +
            " FROM TrackedEntityDataValue " +
            "  INNER JOIN Event ON TrackedEntityDataValue.event = Event.uid " +
            " WHERE event = ? AND VALUE IS NOT NULL AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "';";

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues(String eventUid) {
        return briteDatabase.createQuery(Arrays.asList(EventModel.TABLE,
                TrackedEntityDataValueModel.TABLE), QUERY_VALUES, eventUid == null ? "" : eventUid)
                .mapToList(cursor -> {
                    Date eventDate = parseDate(cursor.getString(0));
                    return RuleDataValue.create(eventDate, cursor.getString(1),
                            cursor.getString(2), cursor.getString(3));
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private static Date parseDate(@NonNull String date) throws ParseException {
        return BaseIdentifiableObject.DATE_FORMAT.parse(date);
    }

    @NonNull
    private FormSectionViewModel mapToFormSectionViewModels(@NonNull Cursor cursor) {
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