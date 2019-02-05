package org.dhis2.data.forms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @Nullable
    public abstract String renderType();

    @NonNull
    public static FormSectionViewModel createForSection(@NonNull String eventUid,
                                                        @NonNull String sectionUid,
                                                        @NonNull String label,
                                                        @Nullable String renderType) {
        return new AutoValue_FormSectionViewModel(eventUid, sectionUid, label, Type.SECTION, renderType);
    }

    @NonNull
    public static FormSectionViewModel createForProgramStage(@NonNull String eventUid,
                                                             @NonNull String programStageUid) {
        return new AutoValue_FormSectionViewModel(eventUid, programStageUid, null, Type.PROGRAM_STAGE, null);
    }

    @NonNull
    public static FormSectionViewModel createForProgramStageWithLabel(@NonNull String eventUid,
                                                                      @NonNull String programStageDisplayName,
                                                                      @NonNull String programStageUid) {
        return new AutoValue_FormSectionViewModel(eventUid, null, programStageDisplayName, Type.PROGRAM_STAGE, null);
    }

    @NonNull
    static FormSectionViewModel createForEnrollment(@NonNull String enrollmentUid) {
        return new AutoValue_FormSectionViewModel(enrollmentUid, null, null, Type.ENROLLMENT, null);
    }

    enum Type {
        SECTION, PROGRAM_STAGE, ENROLLMENT
    }
}
