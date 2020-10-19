package org.dhis2.data.service;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerService;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 24/10/2018.
 */
@PerService
@Subcomponent(modules = SyncMetadataWorkerModule.class)
public interface SyncMetadataWorkerComponent {
    void inject(@NonNull SyncMetadataWorker syncMetadataWorker);
}
