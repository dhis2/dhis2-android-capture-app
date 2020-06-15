package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.reactivex.Flowable;

public interface DataEntryStore {

    enum valueType {
        ATTR, DATA_ELEMENT
    }

    enum EntryMode {
        DE, ATTR,DV
    }

    @NonNull
    Flowable<Long> save(@NonNull String uid, @Nullable String value);
}
