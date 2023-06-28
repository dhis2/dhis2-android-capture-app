package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.database.Cursor;
import androidx.databinding.BaseObservable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.io.Serializable;

@AutoValue
public abstract class EnrollmentViewModel extends BaseObservable implements Serializable {

    @NonNull
    public abstract String uid();

    @NonNull
    public abstract String enrollmentDate();

    @Nullable
    public abstract String color();

    @Nullable
    public abstract String icon();

    @NonNull
    public abstract String programName();

    @NonNull
    public abstract String orgUnitName();

    @NonNull
    public abstract Boolean followUp();

    @NonNull
    public abstract String programUid();

    @NonNull
    public static EnrollmentViewModel create(@NonNull String uid, @NonNull String enrollmentDate, @Nullable String color,
                                             @Nullable String icon, @NonNull String progranName, @NonNull String orgUnitName, @NonNull Boolean followup, @NonNull String programUid) {
        return new AutoValue_EnrollmentViewModel(uid, enrollmentDate, color, icon, progranName, orgUnitName, followup,programUid);
    }
}
