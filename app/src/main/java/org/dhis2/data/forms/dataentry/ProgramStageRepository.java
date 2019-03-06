package org.dhis2.data.forms.dataentry;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.FieldViewModelUtils;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

@SuppressWarnings({
        "PMD.AvoidDuplicateLiterals"
})
final class ProgramStageRepository implements DataEntryRepository {
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
            "        LEFT JOIN ProgramStageSectionDataElementLink ON ProgramStageSectionDataElementLink.dataElement = ProgramStageDataElement.dataElement\n" +
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

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final FieldViewModelFactory fieldFactory;

    @NonNull
    private final String eventUid;

    @Nullable
    private final String sectionUid;

    private ProgramStageSectionRenderingType renderingType;
    private boolean accessDataWrite;

    ProgramStageRepository(@NonNull BriteDatabase briteDatabase,
                           @NonNull FieldViewModelFactory fieldFactory,
                           @NonNull String eventUid, @Nullable String sectionUid) {
        this.briteDatabase = briteDatabase;
        this.fieldFactory = fieldFactory;
        this.eventUid = eventUid;
        this.sectionUid = sectionUid;
        this.renderingType = ProgramStageSectionRenderingType.LISTING;
    }

    @NonNull
    @Override
    public Observable<List<FieldViewModel>> list() {

        Cursor cursor = briteDatabase.query(SECTION_RENDERING_TYPE, sectionUid == null ? "" : sectionUid);
        if (cursor != null && cursor.moveToFirst()) {
            renderingType = cursor.getString(0) != null ?
                    ProgramStageSectionRenderingType.valueOf(cursor.getString(0)) :
                    ProgramStageSectionRenderingType.LISTING;
            cursor.close();
        }

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
                .createQuery(TrackedEntityDataValueModel.TABLE, prepareStatement())
                .mapToList(this::transform)
                .map(this::checkRenderType);
    }

    private List<FieldViewModel> checkRenderType(List<FieldViewModel> fieldViewModels) {

        ArrayList<FieldViewModel> renderList = new ArrayList<>();

        if (renderingType != ProgramStageSectionRenderingType.LISTING) {
            for (FieldViewModel fieldViewModel : fieldViewModels) {
                renderList.addAll(parseRenderList(fieldViewModel));
            }
        } else {
            renderList.addAll(fieldViewModels);
        }
        return renderList;
    }

    private ArrayList<FieldViewModel> parseRenderList(FieldViewModel fieldViewModel) {
        ArrayList<FieldViewModel> renderList = new ArrayList<>();

        if (!isEmpty(fieldViewModel.optionSet())) {
            Cursor cursor = briteDatabase.query(OPTIONS, fieldViewModel.optionSet() == null ? "" : fieldViewModel.optionSet());
            if (cursor != null && cursor.moveToFirst()) {
                int optionCount = cursor.getCount();
                renderList.addAll(parseOptionCount(fieldViewModel, cursor, optionCount));
                cursor.close();
            }
        } else {
            renderList.add(fieldViewModel);
        }

        return renderList;
    }

    private ArrayList<FieldViewModel> parseOptionCount(FieldViewModel fieldViewModel, Cursor cursor, int optionCount) {
        ArrayList<FieldViewModel> renderList = new ArrayList<>();

        for (int i = 0; i < optionCount; i++) {
            String uid = cursor.getString(0);
            String displayName = cursor.getString(1);
            String optionCode = cursor.getString(2);

            ObjectStyleModel objectStyle = ObjectStyleModel.builder().build();
            try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", fieldViewModel.uid())) {
                if (objStyleCursor.moveToFirst())
                    objectStyle = ObjectStyleModel.create(objStyleCursor);
            }

            renderList.add(fieldFactory.create(
                    fieldViewModel.uid() + "." + uid, //fist
                    displayName + "-" + optionCode, ValueType.TEXT, false,
                    fieldViewModel.optionSet(), fieldViewModel.value(), fieldViewModel.programStageSection(),
                    fieldViewModel.allowFutureDate(), fieldViewModel.editable() == null ? false : fieldViewModel.editable(), renderingType, fieldViewModel.description(), null, optionCount, objectStyle));

            cursor.moveToNext();
        }

        return renderList;
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrgUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, "SELECT * FROM " + OrganisationUnitModel.TABLE)
                .mapToList(OrganisationUnitModel::create);
    }

    @Override
    public void assign(String field, String content) {
        Cursor dataValueCursor = briteDatabase.query("SELECT * FROM TrackedEntityDataValue WHERE dataElement = ?", field == null ? "" : field);
        if (dataValueCursor != null && dataValueCursor.moveToFirst()) {
            TrackedEntityDataValueModel dataValue = TrackedEntityDataValueModel.create(dataValueCursor);
            ContentValues contentValues = dataValue.toContentValues();
            contentValues.put(TrackedEntityDataValueModel.Columns.VALUE, content);
            int row = briteDatabase.update(TrackedEntityDataValueModel.TABLE, contentValues, "dataElement = ?", field == null ? "" : field);
            if (row == -1)
                Log.d(this.getClass().getSimpleName(), String.format("Error updating field %s", field == null ? "" : field));
        }
    }

    @NonNull
    private FieldViewModel transform(@NonNull Cursor cursor) {
        FieldViewModelUtils fieldViewModelUtils = new FieldViewModelUtils(cursor);

        int optionCount = FieldViewModelUtils.getOptionCount(briteDatabase, fieldViewModelUtils.getOptionSetUid());

        ValueTypeDeviceRendering fieldRendering = FieldViewModelUtils.getValueTypeDeviceRendering(briteDatabase, fieldViewModelUtils.getUid());

        Cursor eventCursor = briteDatabase.query("SELECT * FROM Event WHERE uid = ?", eventUid);
        eventCursor.moveToFirst();
        EventModel eventModel = EventModel.create(eventCursor);
        eventCursor.close();
        Cursor programStageCursor = briteDatabase.query("SELECT * FROM ProgramStage WHERE uid = ?", eventModel.programStage());
        programStageCursor.moveToFirst();
        ProgramStage programStage = ProgramStage.create(programStageCursor);
        programStageCursor.close();
        Cursor programCursor = briteDatabase.query("SELECT * FROM Program WHERE uid = ?", eventModel.program());
        programCursor.moveToFirst();
        ProgramModel programModel = ProgramModel.create(programCursor);
        programCursor.close();

        boolean hasExpired = DateUtils.getInstance().hasExpired(eventModel, programModel.expiryDays(), programModel.completeEventsExpiryDays(), programStage.periodType() != null ? programStage.periodType() : programModel.expiryPeriodType());

        ObjectStyleModel objectStyle = ObjectStyleModel.builder().build();
        try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", fieldViewModelUtils.getUid())) {
            if (objStyleCursor.moveToFirst())
                objectStyle = ObjectStyleModel.create(objStyleCursor);
        }

        return fieldFactory.create(fieldViewModelUtils.getUid(),
                isEmpty(fieldViewModelUtils.getFormLabel()) ? fieldViewModelUtils.getLabel() : fieldViewModelUtils.getFormLabel(),
                fieldViewModelUtils.getValueType(), fieldViewModelUtils.isMandatory(), fieldViewModelUtils.getOptionSetUid(),
                fieldViewModelUtils.getDataValue(), fieldViewModelUtils.getSection(),
                fieldViewModelUtils.getAllowFutureDates(),
                accessDataWrite && fieldViewModelUtils.getEventStatus() == EventStatus.ACTIVE && !hasExpired, renderingType,
                fieldViewModelUtils.getDescription(), fieldRendering, optionCount, objectStyle);
    }

    @NonNull
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    private String prepareStatement() {
        String where;
        if (isEmpty(sectionUid)) {
            where = String.format(Locale.US, "WHERE Event.uid = '%s'", eventUid);
        } else {
            where = String.format(Locale.US, "WHERE Event.uid = '%s' AND " +
                    "Field.section = '%s'", eventUid, sectionUid);
        }

        return String.format(Locale.US, QUERY, where);
    }
}