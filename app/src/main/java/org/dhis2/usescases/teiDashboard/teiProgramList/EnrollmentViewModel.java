package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.database.Cursor;
import androidx.databinding.BaseObservable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import java.io.Serializable;

@AutoValue
public abstract class EnrollmentViewModel extends BaseObservable implements Serializable {

    static class Columns {
        static final String UID = "uid";
        static final String ENROLLMENT_DATE = "enrollmentDate";
        static final String ICON = "icon";
        static final String COLOR = "color";
        static final String PROGRAM_NAME = "programName";
        static final String ORG_UNIT = "OrgUnitName";
        static final String FOLLOW_UP = "followup";
        static final String PROGRAM_UID = "programUid";
    }

    @NonNull
    @ColumnName(Columns.UID)
    public abstract String uid();

    @NonNull
    @ColumnName(Columns.ENROLLMENT_DATE)
    public abstract String enrollmentDate();

    @Nullable
    @ColumnName(Columns.COLOR)
    public abstract String color();

    @Nullable
    @ColumnName(Columns.ICON)
    public abstract String icon();

    @NonNull
    @ColumnName(Columns.PROGRAM_NAME)
    public abstract String programName();

    @NonNull
    @ColumnName(Columns.ORG_UNIT)
    public abstract String orgUnitName();

    @NonNull
    @ColumnName(Columns.FOLLOW_UP)
    public abstract Boolean followUp();

    @NonNull
    @ColumnName(Columns.PROGRAM_UID)
    public abstract String programUid();

    @NonNull
    public static EnrollmentViewModel create(@NonNull String uid, @NonNull String enrollmentDate, @Nullable String color,
                                             @Nullable String icon, @NonNull String progranName, @NonNull String orgUnitName, @NonNull Boolean followup, @NonNull String programUid) {
        return new AutoValue_EnrollmentViewModel(uid, enrollmentDate, color, icon, progranName, orgUnitName, followup,programUid);
    }

    public static EnrollmentViewModel fromCursor(Cursor cursor) {
        return AutoValue_EnrollmentViewModel.createFromCursor(cursor);
    }

}
