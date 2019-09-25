package org.dhis2.data.forms.dataentry;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitTableInfo;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueTableInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

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
            "  Field.sectionOrder,\n" +
            "  Field.fieldMask\n" +
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
            "        DataElement.fieldMask as fieldMask,\n" +
            "        ProgramStageSectionDataElementLink.sortOrder AS sectionOrder\n" + //This should override dataElement formOrder
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
    private final D2 d2;


    ProgramStageRepository(@NonNull BriteDatabase briteDatabase,
                           @NonNull FieldViewModelFactory fieldFactory,
                           @NonNull String eventUid, @Nullable String sectionUid, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.fieldFactory = fieldFactory;
        this.eventUid = eventUid;
        this.sectionUid = sectionUid;
        this.renderingType = ProgramStageSectionRenderingType.LISTING;
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list() {

        try (Cursor cursor = briteDatabase.query(SECTION_RENDERING_TYPE, sectionUid == null ? "" : sectionUid)) {
            if (cursor != null && cursor.moveToFirst()) {
                renderingType = cursor.getString(0) != null ?
                        ProgramStageSectionRenderingType.valueOf(cursor.getString(0)) :
                        ProgramStageSectionRenderingType.LISTING;
            }
        }

        try (Cursor accessCursor = briteDatabase.query(ACCESS_QUERY, eventUid)) {
            if (accessCursor != null && accessCursor.moveToFirst()) {
                accessDataWrite = accessCursor.getInt(0) == 1;
            }
        }

        try (Cursor programAccessCursor = briteDatabase.query(PROGRAM_ACCESS_QUERY, eventUid)) {
            if (programAccessCursor != null && programAccessCursor.moveToFirst()) {
                accessDataWrite = accessDataWrite && programAccessCursor.getInt(0) == 1;
            }
        }

        return briteDatabase
                .createQuery(TrackedEntityDataValueTableInfo.TABLE_INFO.name(), prepareStatement())
                .mapToList(this::transform)
                .map(this::checkRenderType)
                .toFlowable(BackpressureStrategy.BUFFER);
    }

    private List<FieldViewModel> checkRenderType(List<FieldViewModel> fieldViewModels) {

        ArrayList<FieldViewModel> renderList = new ArrayList<>();

        if (renderingType != ProgramStageSectionRenderingType.LISTING) {

            for (FieldViewModel fieldViewModel : fieldViewModels) {
                if (!isEmpty(fieldViewModel.optionSet())) {
                    try (Cursor cursor = briteDatabase.query(OPTIONS, fieldViewModel.optionSet() == null ? "" : fieldViewModel.optionSet())) {
                        if (cursor != null && cursor.moveToFirst()) {
                            int optionCount = cursor.getCount();
                            for (int i = 0; i < optionCount; i++) {
                                String uid = cursor.getString(0);
                                String displayName = cursor.getString(1);
                                String optionCode = cursor.getString(2);

                                ObjectStyle objectStyle = ObjectStyle.builder().build();
                                try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", fieldViewModel.uid())) {
                                    if (objStyleCursor.moveToFirst())
                                        objectStyle = ObjectStyle.create(objStyleCursor);
                                }

                                renderList.add(fieldFactory.create(
                                        fieldViewModel.uid() + "." + uid, //fist
                                        displayName + "-" + optionCode, ValueType.TEXT, false,
                                        fieldViewModel.optionSet(), fieldViewModel.value(), fieldViewModel.programStageSection(),
                                        fieldViewModel.allowFutureDate(), fieldViewModel.editable() == null ? false : fieldViewModel.editable(), renderingType, fieldViewModel.description(), null, optionCount, objectStyle, fieldViewModel.fieldMask()));

                                cursor.moveToNext();
                            }
                        }
                    }

                } else
                    renderList.add(fieldViewModel);
            }


        } else
            renderList.addAll(fieldViewModels);

        return renderList;

    }

    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits() {
        return briteDatabase.createQuery(OrganisationUnitTableInfo.TABLE_INFO.name(), "SELECT * FROM " + OrganisationUnitTableInfo.TABLE_INFO.name())
                .mapToList(OrganisationUnit::create);
    }

    @NonNull
    private FieldViewModel transform(@NonNull Cursor cursor) {
        String uid = cursor.getString(0);
        String label = cursor.getString(1);
        ValueType valueType = ValueType.valueOf(cursor.getString(2));
        boolean mandatory = cursor.getInt(3) == 1;
        String optionSetUid = cursor.getString(4);
        String dataValue = cursor.getString(5);
        String optionCodeName = cursor.getString(6);
        String section = cursor.getString(7);
        Boolean allowFutureDates = cursor.getInt(8) == 1;
        EventStatus eventStatus = EventStatus.valueOf(cursor.getString(9));
        String formLabel = cursor.getString(10);
        String description = cursor.getString(11);
        String fieldMask = cursor.getString(14);

        if (!isEmpty(optionCodeName)) {
            dataValue = optionCodeName;
        }

        int optionCount = 0;
        try (Cursor countCursor = briteDatabase.query("SELECT COUNT (uid) FROM Option WHERE optionSet = ?", optionSetUid)) {
            if (countCursor != null && countCursor.moveToFirst())
                optionCount = countCursor.getInt(0);
        } catch (Exception e) {
            Timber.e(e);
        }
        ValueTypeDeviceRendering fieldRendering = null;
        try (Cursor rendering = briteDatabase.query("SELECT ValueTypeDeviceRendering.* FROM ValueTypeDeviceRendering" +
                " JOIN ProgramStageDataElement ON ProgramStageDataElement.uid = ValueTypeDeviceRendering.uid" +
                " WHERE ProgramStageDataElement.uid = ?", uid)) {
            if (rendering != null && rendering.moveToFirst()) {
                fieldRendering = ValueTypeDeviceRendering.create(rendering);
            }
        }

        Event event;
        ProgramStage programStage;
        Program program;
        try (Cursor eventCursor = briteDatabase.query("SELECT * FROM Event WHERE uid = ?", eventUid)) {
            eventCursor.moveToFirst();
            event = Event.create(eventCursor);
        }

        try (Cursor programStageCursor = briteDatabase.query("SELECT * FROM ProgramStage WHERE uid = ?", event.programStage())) {
            programStageCursor.moveToFirst();
            programStage = ProgramStage.create(programStageCursor);
        }

        try (Cursor programCursor = briteDatabase.query("SELECT * FROM Program WHERE uid = ?", event.program())) {
            programCursor.moveToFirst();
            program = Program.create(programCursor);
        }

        boolean hasExpired = DateUtils.getInstance().hasExpired(event, program.expiryDays(), program.completeEventsExpiryDays(), programStage.periodType() != null ? programStage.periodType() : program.expiryPeriodType());

        ObjectStyle objectStyle = ObjectStyle.builder().build();
        try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", uid)) {
            if (objStyleCursor != null && objStyleCursor.moveToFirst())
                objectStyle = ObjectStyle.create(objStyleCursor);
        }

        if (valueType == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(dataValue).blockingGet().displayName();
        }

        return fieldFactory.create(uid, isEmpty(formLabel) ? label : formLabel, valueType, mandatory, optionSetUid, dataValue, section,
                allowFutureDates, accessDataWrite && eventStatus == EventStatus.ACTIVE && !hasExpired, renderingType, description, fieldRendering, optionCount, objectStyle, fieldMask);
    }

    @NonNull
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    private String prepareStatement() {
        String where;
        if (isEmpty(sectionUid)) {
            where = String.format(Locale.US, "WHERE Event.uid = '%s'", eventUid == null ? "" : eventUid);
        } else {
            where = String.format(Locale.US, "WHERE Event.uid = '%s' AND " +
                    "Field.section = '%s'", eventUid == null ? "" : eventUid, sectionUid == null ? "" : sectionUid);
        }

        return String.format(Locale.US, QUERY, where);
    }

    @Override
    public Observable<List<OrganisationUnitLevel>> getOrgUnitLevels() {
        return Observable.just(d2.organisationUnitModule().organisationUnitLevels.blockingGet());
    }
}
