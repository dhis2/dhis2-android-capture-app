package org.dhis2.data.service;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerService;

import dagger.Subcomponent;

@PerService
@Subcomponent(modules = DataServiceModule.class)
public interface DataServiceComponent {
    void inject(@NonNull SyncDataService syncService);
}
