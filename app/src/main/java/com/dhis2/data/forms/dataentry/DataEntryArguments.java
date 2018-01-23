package com.dhis2.data.forms.dataentry;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DataEntryArguments implements Parcelable {

    @NonNull
    abstract String event();

    @NonNull
    abstract String section();

    @NonNull
    abstract String enrollment();

    @NonNull
    public static DataEntryArguments forEvent(@NonNull String event) {
        return new AutoValue_DataEntryArguments(event, "", "");
    }

    @NonNull
    public static DataEntryArguments forEventSection(@NonNull String event, @NonNull String section) {
        return new AutoValue_DataEntryArguments(event, section, "");
    }

    @NonNull
    public static DataEntryArguments forEnrollment(@NonNull String enrollment) {
        return new AutoValue_DataEntryArguments("", "", enrollment);
    }
}
