package org.dhis2.data.service;

import androidx.annotation.NonNull;

import org.dhis2.commons.di.dagger.PerService;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 24/10/2018.
 */
@PerService
@Subcomponent(modules = SyncDataWorkerModule.class)
public interface SyncDataWorkerComponent {
    void inject(@NonNull SyncDataWorker syncDataWorker);
}
