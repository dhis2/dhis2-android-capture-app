package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;

interface DataEntryView {

    @NonNull
    Flowable<RowAction> rowActions();

    @NonNull
    Consumer<List<FieldViewModel>> showFields();

    void nextFocus();

    FlowableProcessor<RowAction> getActionProcessor();

    void showMessage(int messageId);
}
