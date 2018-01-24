package com.dhis2.data.forms;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FormSectionViewModel {

    // uid of Event or Enrollment
    @NonNull
    public abstract String uid();

    @Nullable
    public abstract String sectionUid();

    @Nullable
    public abstract String label();

    @NonNull
    public abstract Type type();

    @NonNull
    static FormSectionViewModel createForSection(@NonNull String eventUid,
                                                 @NonNull String sectionUid, @NonNull String label) {
        return new AutoValue_FormSectionViewModel(eventUid, sectionUid, label, Type.SECTION);
    }

    @NonNull
    static FormSectionViewModel createForProgramStage(
            @NonNull String eventUid, @NonNull String programStageUid) {
        return new AutoValue_FormSectionViewModel(eventUid, programStageUid, null, Type.PROGRAM_STAGE);
    }

    @NonNull
    static FormSectionViewModel createForEnrollment(@NonNull String enrollmentUid) {
        return new AutoValue_FormSectionViewModel(enrollmentUid, null, null, Type.ENROLLMENT);
    }

    enum Type {
        SECTION, PROGRAM_STAGE, ENROLLMENT
    }
}
