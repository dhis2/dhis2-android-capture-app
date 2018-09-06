package org.dhis2.data.forms.dataentry;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DataEntryArguments implements Parcelable {

    @NonNull
    abstract String event();

    @NonNull
    abstract String section();

    @NonNull
    abstract String enrollment();

    @Nullable
    abstract String renderType();

    @NonNull
    public static DataEntryArguments forEvent(@NonNull String event) {
        return new AutoValue_DataEntryArguments(event, "", "",null);
    }

    @NonNull
    public static DataEntryArguments forEventSection(@NonNull String event, @NonNull String section, String renderType) {
        return new AutoValue_DataEntryArguments(event, section, "",renderType);
    }

    @NonNull
    public static DataEntryArguments forEnrollment(@NonNull String enrollment) {
        return new AutoValue_DataEntryArguments("", "", enrollment,null);
    }
}
