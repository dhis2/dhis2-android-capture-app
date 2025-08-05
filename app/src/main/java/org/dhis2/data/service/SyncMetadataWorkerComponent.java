package org.dhis2.data.service;

import androidx.annotation.NonNull;

import org.dhis2.commons.di.dagger.PerService;

import dagger.Subcomponent;

@PerService
@Subcomponent(modules = SyncMetadataWorkerModule.class)
public interface SyncMetadataWorkerComponent {
    void inject(@NonNull SyncMetadataWorker syncMetadataWorker);
}
