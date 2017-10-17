package com.dhis2.data.service;

import android.support.annotation.NonNull;

import com.data.dagger.PerService;

import dagger.Subcomponent;

@PerService
@Subcomponent(modules = ServiceModule.class)
public interface ServiceComponent {
    void inject(@NonNull SyncService syncService);
}
