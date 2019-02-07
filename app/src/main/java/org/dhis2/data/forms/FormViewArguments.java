package org.dhis2.data.forms;

import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import static org.dhis2.data.forms.FormViewArguments.Type.EMPTY_STATE;
import static org.dhis2.data.forms.FormViewArguments.Type.ENROLLMENT;
import static org.dhis2.data.forms.FormViewArguments.Type.EVENT;


@AutoValue
public abstract class FormViewArguments implements Parcelable {

    // this is the uid for an enrollment or an event
    @NonNull
    abstract String uid();

    @NonNull
    abstract Type type();

    @NonNull
    public static FormViewArguments createForEnrollment(@NonNull String enrollmentUid) {
        return new AutoValue_FormViewArguments(enrollmentUid, ENROLLMENT);
    }

    @NonNull
    public static FormViewArguments createForEvent(@NonNull String eventUid) {
        return new AutoValue_FormViewArguments(eventUid, EVENT);
    }

    @NonNull
    public static FormViewArguments createForEmptyState() {
        return new AutoValue_FormViewArguments("", EMPTY_STATE);
    }

    enum Type {
        ENROLLMENT, EVENT, EMPTY_STATE
    }
}