package org.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Flowable;

public interface DataEntryStore {

    @NonNull
    Flowable<Long> save(@NonNull String uid, @Nullable String value);
}
