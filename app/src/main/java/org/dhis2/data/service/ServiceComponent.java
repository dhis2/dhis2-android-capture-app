package org.dhis2.data.service;

import android.support.annotation.NonNull;

import org.dhis2.data.dagger.PerService;

import dagger.Subcomponent;

@PerService
@Subcomponent(modules = ServiceModule.class)
public interface ServiceComponent {
    void inject(@NonNull SyncService syncService);
}
