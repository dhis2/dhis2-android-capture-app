package org.dhis2.data.server;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerServer;
import org.dhis2.data.database.DbModule;
import org.dhis2.data.user.UserComponent;
import org.dhis2.data.user.UserModule;
import org.dhis2.utils.granular_sync.GranularSyncComponent;
import org.dhis2.utils.granular_sync.GranularSyncModule;

import dagger.Component;
import dagger.Subcomponent;

@PerServer
@Subcomponent(modules = {ServerModule.class, DbModule.class})
public interface ServerComponent {

    @NonNull
    UserManager userManager();

    @NonNull
    UserComponent plus(@NonNull UserModule userModule);

    @NonNull
    GranularSyncComponent plus(@NonNull GranularSyncModule granularSyncModule);

}
