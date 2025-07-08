package org.dhis2.usescases.teiDashboard.teiProgramList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;

import com.google.auto.value.AutoValue;

import org.dhis2.ui.MetadataIconData;

import java.io.Serializable;

@AutoValue
public abstract class EnrollmentViewModel extends BaseObservable implements Serializable {

    @NonNull
    public abstract String uid();

    @NonNull
    public abstract String enrollmentDate();

    @Nullable
    public abstract MetadataIconData metadataIconData();

    @NonNull
    public abstract String programName();

    @NonNull
    public abstract String orgUnitName();

    @NonNull
    public abstract Boolean followUp();

    @NonNull
    public abstract String programUid();

    @NonNull
    public static EnrollmentViewModel create(@NonNull String uid, @NonNull String enrollmentDate, @Nullable MetadataIconData metadataIconData, @NonNull String progranName, @NonNull String orgUnitName, @NonNull Boolean followup, @NonNull String programUid) {
        return new AutoValue_EnrollmentViewModel(uid, enrollmentDate, metadataIconData, progranName, orgUnitName, followup, programUid);
    }
}
