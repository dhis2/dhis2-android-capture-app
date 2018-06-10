package com.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.RowAction;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

interface DataEntryView {

    @NonNull
    Flowable<RowAction> rowActions();

    @NonNull
    Consumer<List<FieldViewModel>> showFields();

    void removeSection(String sectionUid);
}
