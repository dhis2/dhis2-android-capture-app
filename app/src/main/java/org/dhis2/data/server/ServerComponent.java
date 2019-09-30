package org.dhis2.data.server;

import androidx.annotation.NonNull;

import org.dhis2.data.dagger.PerServer;
import org.dhis2.data.database.DbModule;
import org.dhis2.data.user.UserComponent;
import org.dhis2.data.user.UserModule;

import dagger.Component;
import dagger.Subcomponent;

@PerServer
@Subcomponent(modules = {ServerModule.class, DbModule.class})
public interface ServerComponent {

    @NonNull
    UserManager userManager();

    @NonNull
    UserComponent plus(@NonNull UserModule userModule);

}
