package org.dhis2.usescases.reservedValue;

import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = ReservedValueModule.class)
public interface ReservedValueComponent {
    void inject(ReservedValueActivity activity);
}
