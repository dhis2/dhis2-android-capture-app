package com.dhis2.data.forms.dataentry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.D2CallException;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.Calendar;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

final class EnrollmentRepository implements DataEntryRepository {
    private static final String QUERY = "SELECT \n" +
            "  Field.id,\n" +
            "  Field.label,\n" +
            "  Field.type,\n" +
            "  Field.mandatory,\n" +
            "  Field.optionSet,\n" +
            "  Value.value,\n" +
            "  Option.name,\n" +
            "  Field.allowFutureDate,\n" +
            "  Field.generated,\n" +
            "  Enrollment.organisationUnit,\n" +
            "  Enrollment.status\n" +
            "FROM (Enrollment INNER JOIN Program ON Program.uid = Enrollment.program)\n" +
            "  LEFT OUTER JOIN (\n" +
            "      SELECT\n" +
            "        TrackedEntityAttribute.uid AS id,\n" +
            "        TrackedEntityAttribute.displayName AS label,\n" +
            "        TrackedEntityAttribute.valueType AS type,\n" +
            "        TrackedEntityAttribute.optionSet AS optionSet,\n" +
            "        ProgramTrackedEntityAttribute.program AS program,\n" +
            "        ProgramTrackedEntityAttribute.mandatory AS mandatory,\n" +
            "        ProgramTrackedEntityAttribute.allowFutureDate AS allowFutureDate,\n" +
            "        TrackedEntityAttribute.generated AS generated\n" +
            "      FROM ProgramTrackedEntityAttribute INNER JOIN TrackedEntityAttribute\n" +
            "          ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n" +
            "    ) AS Field ON Field.program = Program.uid\n" +
            "  LEFT OUTER JOIN TrackedEntityAttributeValue AS Value ON (\n" +
            "    Value.trackedEntityAttribute = Field.id\n" +
            "        AND Value.trackedEntityInstance = Enrollment.trackedEntityInstance)\n" +
            "  LEFT OUTER JOIN Option ON (\n" +
            "    Field.optionSet = Option.optionSet AND Value.value = Option.code\n" +
            "  )\n" +
            "WHERE Enrollment.uid = ?";


    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final FieldViewModelFactory fieldFactory;

    @NonNull
    private final String enrollment;
    private final D2 d2;

    EnrollmentRepository(@NonNull BriteDatabase briteDatabase,
                         @NonNull FieldViewModelFactory fieldFactory,
                         @NonNull String enrollment, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.fieldFactory = fieldFactory;
        this.enrollment = enrollment;
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list() {
        return briteDatabase
                .createQuery(TrackedEntityAttributeValueModel.TABLE, QUERY, enrollment)
                .mapToList(this::transform).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrgUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, "SELECT * FROM " + OrganisationUnitModel.TABLE)
                .mapToList(OrganisationUnitModel::create);
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

        if (!isEmpty(optionCodeName)) {
            dataValue = optionCodeName;
        }

        if (generated && dataValue == null) {
            try {
                String teiUid = null;
                Cursor tei = briteDatabase.query("SELECT TrackedEntityInstance.uid FROM TrackedEntityInstance " +
                        "JOIN Enrollment ON Enrollment.trackedEntityInstance = TrackedEntityInstance.uid " +
                        "WHERE Enrollment.uid = ?", enrollment);
                if (tei != null && tei.moveToFirst()) {
                    teiUid = tei.getString(0);
                    tei.close();
                }

                if(teiUid!=null) { //checks if tei has been deleted
                    dataValue = d2.popTrackedEntityAttributeReservedValue(uid, orgUnitUid);

                    //Checks if ValueType is Numeric and that it start with a 0, then removes the 0
                    if(valueType == ValueType.NUMBER)
                        while(dataValue.startsWith("0")){
                            dataValue = d2.popTrackedEntityAttributeReservedValue(uid, orgUnitUid);
                    }

                    String INSERT = "INSERT INTO TrackedEntityAttributeValue\n" +
                            "(lastUpdated, value, trackedEntityAttribute, trackedEntityInstance)\n" +
                            "VALUES (?,?,?,?)";
                    SQLiteStatement updateStatement = briteDatabase.getWritableDatabase()
                            .compileStatement(INSERT);
                    sqLiteBind(updateStatement, 1, BaseIdentifiableObject.DATE_FORMAT
                            .format(Calendar.getInstance().getTime()));
                    sqLiteBind(updateStatement, 2, dataValue);
                    sqLiteBind(updateStatement, 3, uid);
                    sqLiteBind(updateStatement, 4, teiUid);

                    long insert = briteDatabase.executeInsert(
                            TrackedEntityAttributeValueModel.TABLE, updateStatement);
                    updateStatement.clearBindings();
                }
            } catch (D2CallException e) {
                Timber.e(e);
            }
        }

        return fieldFactory.create(uid,
                label, valueType, mandatory, optionSet, dataValue, null, allowFutureDates,
                !generated && enrollmentStatus == EnrollmentStatus.ACTIVE, null);

    }

    @Override
    public void assign(String field, String content) {
        Cursor dataValueCursor = briteDatabase.query("SELECT * FROM TrackedEntityAttributeValue WHERE trackedEntityAttribute = ?", field);
        if (dataValueCursor != null && dataValueCursor.moveToFirst()) {
            TrackedEntityAttributeValueModel dataValue = TrackedEntityAttributeValueModel.create(dataValueCursor);
            ContentValues contentValues = dataValue.toContentValues();
            contentValues.put(TrackedEntityAttributeValueModel.Columns.VALUE, content);
            int row = briteDatabase.update(TrackedEntityAttributeValueModel.TABLE, contentValues, "trackedEntityAttribute = ?", field);
            if (row == -1)
                Log.d(this.getClass().getSimpleName(), String.format("Error updating field %s", field));
        }
    }
}