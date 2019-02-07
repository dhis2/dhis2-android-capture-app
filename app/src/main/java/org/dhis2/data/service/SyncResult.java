package org.dhis2.data.service;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;


@AutoValue
public abstract class SyncResult {

    @NonNull
    abstract Boolean isIdle();

    @NonNull
    public abstract Boolean inProgress();

    @NonNull
    public abstract Boolean isSuccess();

    @NonNull
    public abstract String message();

    @NonNull
    public static SyncResult idle() {
        return new AutoValue_SyncResult(true, false, false, "");
    }

    @NonNull
    public static SyncResult progress() {
        return new AutoValue_SyncResult(false, true, false, "");
    }

    @NonNull
    public static SyncResult success() {
        return new AutoValue_SyncResult(false, false, true, "");
    }

    @NonNull
    public static SyncResult failure(@NonNull String message) {
        return new AutoValue_SyncResult(false, false, false, message);
    }
}
