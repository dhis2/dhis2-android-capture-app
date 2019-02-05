package org.dhis2.data.forms;

import androidx.annotation.NonNull;

import org.dhis2.data.forms.dataentry.DataEntryComponent;
import org.dhis2.data.forms.dataentry.DataEntryModule;
import org.dhis2.data.forms.dataentry.DataEntryStoreModule;

import dagger.Subcomponent;

@PerForm
@Subcomponent(modules = FormModule.class)
public interface FormComponent {
    @NonNull
    DataEntryComponent plus(@NonNull DataEntryModule dataEntryModule,
                            @NonNull DataEntryStoreModule dataEntryStoreModule);

    void inject(FormFragment formFragment);
}