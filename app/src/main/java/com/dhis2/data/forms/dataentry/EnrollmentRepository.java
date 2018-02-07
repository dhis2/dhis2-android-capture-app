package com.dhis2.data.forms.dataentry;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import static android.text.TextUtils.isEmpty;
import static hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Flowable;

final class EnrollmentRepository implements DataEntryRepository {
    private static final String QUERY = "SELECT \n" +
            "  Field.id,\n" +
            "  Field.label,\n" +
            "  Field.type,\n" +
            "  Field.mandatory,\n" +
            "  Field.optionSet,\n" +
            "  Value.value,\n" +
            "  Option.name\n" +
            "FROM (Enrollment INNER JOIN Program ON Program.uid = Enrollment.program)\n" +
            "  LEFT OUTER JOIN (\n" +
            "      SELECT\n" +
            "        TrackedEntityAttribute.uid AS id,\n" +
            "        TrackedEntityAttribute.displayName AS label,\n" +
            "        TrackedEntityAttribute.valueType AS type,\n" +
            "        TrackedEntityAttribute.optionSet AS optionSet,\n" +
            "        ProgramTrackedEntityAttribute.program AS program,\n" +
            "        ProgramTrackedEntityAttribute.sortOrder AS formOrder,\n" +
            "        ProgramTrackedEntityAttribute.mandatory AS mandatory\n" +
            "      FROM ProgramTrackedEntityAttribute INNER JOIN TrackedEntityAttribute\n" +
            "          ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n" +
            "    ) AS Field ON Field.program = Program.uid\n" +
            "  LEFT OUTER JOIN TrackedEntityAttributeValue AS Value ON (\n" +
            "    Value.trackedEntityAttribute = Field.id\n" +
            "        AND Value.trackedEntityInstance = Enrollment.trackedEntityInstance)\n" +
            "  LEFT OUTER JOIN Option ON (\n" +
            "    Field.optionSet = Option.optionSet AND Value.value = Option.code\n" +
            "  )\n" +
            "WHERE Enrollment.uid = ?\n" +
            "ORDER BY Field.formOrder ASC;";

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final FieldViewModelFactory fieldFactory;

    @NonNull
    private final String enrollment;

    EnrollmentRepository(@NonNull BriteDatabase briteDatabase,
            @NonNull FieldViewModelFactory fieldFactory,
            @NonNull String enrollment) {
        this.briteDatabase = briteDatabase;
        this.fieldFactory = fieldFactory;
        this.enrollment = enrollment;
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list() {
        return briteDatabase
                .createQuery(TrackedEntityAttributeValueModel.TABLE, QUERY, enrollment)
                .mapToList(this::transform).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private FieldViewModel transform(@NonNull Cursor cursor) {
        String dataValue = cursor.getString(5);
        String optionCodeName = cursor.getString(6);

        if (!isEmpty(optionCodeName)) {
            dataValue = optionCodeName;
        }

        return fieldFactory.create(cursor.getString(0), cursor.getString(1),
                ValueType.valueOf(cursor.getString(2)), cursor.getInt(3) == 1,
                cursor.getString(4), dataValue);
    }
}