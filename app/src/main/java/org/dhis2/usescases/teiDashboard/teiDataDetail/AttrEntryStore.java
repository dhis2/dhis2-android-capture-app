package org.dhis2.usescases.teiDashboard.teiDataDetail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.reactivex.Flowable;

public interface AttrEntryStore {

    @NonNull
    Flowable<Long> save(@NonNull String uid, @Nullable String value);
}
