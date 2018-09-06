package org.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;


import org.dhis2.data.dagger.PerFragment;

import dagger.Subcomponent;

@PerFragment
@Subcomponent(modules = DataEntryModule.class)
public interface DataEntryComponent {
    void inject(@NonNull DataEntryFragment dataEntryFragment);
}
