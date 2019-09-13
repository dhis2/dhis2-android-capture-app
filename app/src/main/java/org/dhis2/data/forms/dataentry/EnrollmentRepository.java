package org.dhis2.data.forms.dataentry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitTableInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.hisp.dhis.android.core.arch.db.stores.internal.StoreUtils.sqLiteBind;

final class EnrollmentRepository implements DataEntryRepository {
    private static final String QUERY = "SELECT \n" +
            "  Field.id,\n" +
            "  Field.label,\n" +
            "  Field.type,\n" +
            "  Field.mandatory,\n" +
            "  Field.optionSet,\n" +
            "  Value.value,\n" +
            "  Option.displayName,\n" +
            "  Field.allowFutureDate,\n" +
            "  Field.generated,\n" +
            "  Enrollment.organisationUnit,\n" +
            "  Enrollment.status,\n" +
            "  Field.displayDescription,\n" +
            "  Field.pattern, \n" +
            "  Field.formName, \n" +
            "  Field.formOrder, \n" +
            "  Field.fieldMask \n" +
            "FROM (Enrollment INNER JOIN Program ON Program.uid = Enrollment.program)\n" +
            "  LEFT OUTER JOIN (\n" +
            "      SELECT\n" +
            "        TrackedEntityAttribute.uid AS id,\n" +
            "        TrackedEntityAttribute.displayName AS label,\n" +
            "        TrackedEntityAttribute.valueType AS type,\n" +
            "        TrackedEntityAttribute.pattern AS pattern,\n" +
            "        TrackedEntityAttribute.optionSet AS optionSet,\n" +
            "        ProgramTrackedEntityAttribute.program AS program,\n" +
            "        ProgramTrackedEntityAttribute.sortOrder AS formOrder,\n" +
            "        ProgramTrackedEntityAttribute.mandatory AS mandatory,\n" +
            "        ProgramTrackedEntityAttribute.allowFutureDate AS allowFutureDate,\n" +
            "        TrackedEntityAttribute.generated AS generated,\n" +
            "        TrackedEntityAttribute.displayDescription AS displayDescription, \n" +
            "        TrackedEntityAttribute.formName AS formName, \n" +
            "        TrackedEntityAttribute.fieldMask AS fieldMask \n" +
            "      FROM ProgramTrackedEntityAttribute INNER JOIN TrackedEntityAttribute\n" +
            "          ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n" +
            "    ) AS Field ON Field.program = Program.uid\n" +
            "  LEFT OUTER JOIN TrackedEntityAttributeValue AS Value ON (\n" +
            "    Value.trackedEntityAttribute = Field.id\n" +
            "        AND Value.trackedEntityInstance = Enrollment.trackedEntityInstance)\n" +
            "  LEFT OUTER JOIN Option ON (\n" +
            "    Field.optionSet = Option.optionSet AND Value.value = Option.code\n" +
            "  )\n" +
            "WHERE Enrollment.uid = ? " +
            "ORDER BY Field.formOrder ASC;";


    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final FieldViewModelFactory fieldFactory;

    @NonNull
    private final String enrollment;
    private final Context context;
    private final D2 d2;

    EnrollmentRepository(@NonNull Context context,
                         @NonNull BriteDatabase briteDatabase,
                         @NonNull FieldViewModelFactory fieldFactory,
                         @NonNull String enrollment, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.fieldFactory = fieldFactory;
        this.enrollment = enrollment;
        this.context = context;
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list() {
        return briteDatabase
                .createQuery("TrackedEntityAttributeValue", QUERY, enrollment)
                .mapToList(this::transform)
                .map(list -> list)
                .toFlowable(BackpressureStrategy.BUFFER);
    }

    @Override
    public Observable<List<OrganisationUnitLevel>> getOrgUnitLevels() {
        return Observable.just(d2.organisationUnitModule().organisationUnitLevels.blockingGet());
    }

    public List<FieldViewModel> fieldList() {
        List<FieldViewModel> list = new ArrayList<>();
        try (Cursor listCursor = briteDatabase.query(QUERY, enrollment)) {
            listCursor.moveToFirst();
            do {
                list.add(transform(listCursor));
            } while (listCursor.moveToNext());

        }

        return list;
    }


    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits() {
        return briteDatabase.createQuery(OrganisationUnitTableInfo.TABLE_INFO.name(), "SELECT * FROM " +OrganisationUnitTableInfo.TABLE_INFO.name())
                .mapToList(OrganisationUnit::create);
    }

    @NonNull
    private FieldViewModel transform(@NonNull Cursor cursor) {
        String uid = cursor.getString(0);
        String label = cursor.getString(1);
        ValueType valueType = ValueType.valueOf(cursor.getString(2));
        boolean mandatory = cursor.getInt(3) == 1;
        String optionSet = cursor.getString(4);
        boolean allowFutureDates = cursor.getInt(7) == 1;
        boolean generated = cursor.getInt(8) == 1;
        String orgUnitUid = cursor.getString(9);

        String dataValue = cursor.getString(5);
        String optionCodeName = cursor.getString(6);

        EnrollmentStatus enrollmentStatus = EnrollmentStatus.valueOf(cursor.getString(10));
        String description = cursor.getString(11);
        String pattern = cursor.getString(12);
        String formName = cursor.getString(13); //TODO: WE NEED THE DISPLAY FORM NAME WHEN AVAILABLE IN THE SDK
        String fieldMask = cursor.getString(15);

        if (!isEmpty(optionCodeName)) {
            dataValue = optionCodeName;
        }

        if (valueType == ValueType.IMAGE)
            uid = d2.enrollmentModule().enrollments.uid(enrollment).blockingGet().trackedEntityInstance() + "_" + uid;

        int optionCount = 0;
        if (!isEmpty(optionSet))
            try (Cursor countCursor = briteDatabase.query("SELECT COUNT (uid) FROM Option WHERE optionSet = ?", optionSet)) {
                if (countCursor != null && countCursor.moveToFirst()) {
                    optionCount = countCursor.getInt(0);
                }
            } catch (Exception e) {
                Timber.e(e);
            }

        String warning = null;

        if (generated && dataValue == null) {
            try {
                String teiUid = null;
                try (Cursor tei = briteDatabase.query("SELECT TrackedEntityInstance.uid FROM TrackedEntityInstance " +
                        "JOIN Enrollment ON Enrollment.trackedEntityInstance = TrackedEntityInstance.uid " +
                        "WHERE Enrollment.uid = ?", enrollment)) {
                    if (tei != null && tei.moveToFirst()) {
                        teiUid = tei.getString(0);
                    }
                }

                //checks if tei has been deleted
                if (teiUid != null) {
                    try {
                        dataValue = d2.trackedEntityModule().reservedValueManager.blockingGetValue(uid, orgUnitUid);
                    } catch (Exception e) {
                        dataValue = null;
                        warning = context.getString(R.string.no_reserved_values);
                    }

                    //Checks if ValueType is Numeric and that it start with a 0, then removes the 0
                    if (valueType == ValueType.NUMBER)
                        while (dataValue.startsWith("0")) {
                            dataValue = d2.trackedEntityModule().reservedValueManager.blockingGetValue(uid, orgUnitUid);
                        }

                    if (!isEmpty(dataValue)) {
                        String INSERT = "INSERT INTO TrackedEntityAttributeValue\n" +
                                "(lastUpdated, value, trackedEntityAttribute, trackedEntityInstance)\n" +
                                "VALUES (?,?,?,?)";
                        SQLiteStatement updateStatement = briteDatabase.getWritableDatabase()
                                .compileStatement(INSERT);
                        sqLiteBind(updateStatement, 1, BaseIdentifiableObject.DATE_FORMAT
                                .format(Calendar.getInstance().getTime()));
                        sqLiteBind(updateStatement, 2, dataValue == null ? "" : dataValue);
                        sqLiteBind(updateStatement, 3, uid == null ? "" : uid);
                        sqLiteBind(updateStatement, 4, teiUid == null ? "" : teiUid);

                        long insert = briteDatabase.executeInsert(
                                "TrackedEntityAttributeValue", updateStatement);
                        updateStatement.clearBindings();
                    } else
                        mandatory = true;
                }
            } catch (Exception e) {
                Timber.e(e);
                warning = context.getString(R.string.no_reserved_values);
                mandatory = true;
            }
        }

        ValueTypeDeviceRendering fieldRendering = null;
        if (uid == null) {
            uid = "";
        }
        try (Cursor rendering = briteDatabase.query("SELECT ValueTypeDeviceRendering.* FROM ValueTypeDeviceRendering " +
                "JOIN ProgramTrackedEntityAttribute ON ProgramTrackedEntityAttribute.uid = ValueTypeDeviceRendering.uid WHERE ProgramTrackedEntityAttribute.trackedEntityAttribute = ?", uid)) {
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

        if (warning != null) {
            return fieldFactory.create(uid,
                    label, valueType, mandatory, optionSet, dataValue, null, allowFutureDates,
                    false, null, description, fieldRendering, optionCount, objectStyle, fieldMask)
                    .withWarning(warning);
        } else {
            return fieldFactory.create(uid,
                    label, valueType, mandatory, optionSet, dataValue, null, allowFutureDates,
                    !generated, null, description, fieldRendering, optionCount, objectStyle, fieldMask);
        }
    }

    @Override
    public void assign(String field, String content) {
        try (Cursor dataValueCursor = briteDatabase.query("SELECT * FROM TrackedEntityAttributeValue WHERE trackedEntityAttribute = ?", field == null ? "" : field)) {
            if (dataValueCursor != null && dataValueCursor.moveToFirst()) {
                TrackedEntityAttributeValue dataValue = TrackedEntityAttributeValue.create(dataValueCursor);
                ContentValues contentValues = dataValue.toContentValues();
                contentValues.put("value", content);
                int row = briteDatabase.update("TrackedEntityAttributeValue", contentValues, "trackedEntityAttribute = ?", field == null ? "" : field);
                if (row == -1) {
                    Timber.d("Error updating field %s", field == null ? "" : field);
                }
            }
        }
    }
}