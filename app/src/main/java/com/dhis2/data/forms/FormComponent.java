package com.dhis2.data.forms;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerForm;
import com.dhis2.data.forms.dataentry.DataEntryComponent;
import com.dhis2.data.forms.dataentry.DataEntryModule;
import com.dhis2.data.forms.dataentry.DataEntryStoreModule;

import dagger.Subcomponent;

@PerForm
@Subcomponent(modules = FormModule.class)
public interface FormComponent {

    @NonNull
    DataEntryComponent plus(@NonNull DataEntryModule dataEntryModule,
                            @NonNull DataEntryStoreModule dataEntryStoreModule);

    void inject(FormFragment formFragment);
}