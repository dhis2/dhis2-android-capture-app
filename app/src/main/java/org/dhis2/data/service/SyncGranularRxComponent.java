package org.dhis2.data.service;

import org.dhis2.data.dagger.PerService;

import javax.annotation.Nonnull;

import dagger.Subcomponent;

@PerService
@Subcomponent(modules = SyncGranularRxModule.class)
public interface SyncGranularRxComponent {
    void inject(@Nonnull SyncGranularWorker syncGranularWorker);
}
