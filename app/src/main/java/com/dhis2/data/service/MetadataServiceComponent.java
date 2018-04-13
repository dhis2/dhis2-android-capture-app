package com.dhis2.data.service;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerService;

import dagger.Subcomponent;

@PerService
@Subcomponent(modules = MetadataServiceModule.class)
public interface MetadataServiceComponent {
    void inject(@NonNull SyncMetadataService syncService);
}
