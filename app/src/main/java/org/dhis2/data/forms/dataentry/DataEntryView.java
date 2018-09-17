package org.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

interface DataEntryView {

    @NonNull
    Flowable<RowAction> rowActions();

    @NonNull
    Consumer<List<FieldViewModel>> showFields();

    void removeSection(String sectionUid);

    void messageOnComplete(String message, boolean canComplete);
}
