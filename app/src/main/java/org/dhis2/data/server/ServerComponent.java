package org.dhis2.data.server;

import android.support.annotation.NonNull;

import org.dhis2.data.dagger.PerServer;
import org.dhis2.data.user.UserComponent;
import org.dhis2.data.user.UserModule;

import dagger.Subcomponent;

@PerServer
@Subcomponent(modules = ServerModule.class)
public interface ServerComponent {

    @NonNull
    UserManager userManager();

    @NonNull
    UserComponent plus(@NonNull UserModule userModule);
}
