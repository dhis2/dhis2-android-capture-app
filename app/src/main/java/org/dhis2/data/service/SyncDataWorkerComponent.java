package org.dhis2.data.service;

import org.dhis2.data.dagger.PerService;

import javax.annotation.Nonnull;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 24/10/2018.
 */
@PerService
@Subcomponent(modules = SyncDataWorkerModule.class)
public interface SyncDataWorkerComponent {
    void inject(@Nonnull SyncDataWorker syncDataWorker);
}
