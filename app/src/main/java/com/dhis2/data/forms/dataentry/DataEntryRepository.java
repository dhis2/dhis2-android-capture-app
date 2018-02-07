package com.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;


import com.dhis2.data.forms.dataentry.fields.FieldViewModel;

import java.util.List;

import io.reactivex.Flowable;

public interface DataEntryRepository {

    @NonNull
    Flowable<List<FieldViewModel>> list();
}
