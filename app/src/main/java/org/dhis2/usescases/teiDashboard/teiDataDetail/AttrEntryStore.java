package org.dhis2.usescases.teiDashboard.teiDataDetail;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Flowable;

public interface AttrEntryStore {

    @NonNull
    Flowable<Long> save(@NonNull String uid, @Nullable String value);
}
