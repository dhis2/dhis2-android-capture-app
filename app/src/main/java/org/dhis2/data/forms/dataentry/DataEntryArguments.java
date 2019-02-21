package org.dhis2.data.forms.dataentry;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DataEntryArguments implements Parcelable {

    @NonNull
    public abstract String event();

    @NonNull
    public abstract String section();

    @NonNull
    public abstract String enrollment();

    @Nullable
    public abstract String renderType();

    @NonNull
    public static DataEntryArguments forEvent(@NonNull String event,String renderType) {
        return new AutoValue_DataEntryArguments(event, "", "",renderType);
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
