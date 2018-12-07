package org.dhis2.usescases.login;


import android.support.annotation.NonNull;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.usescases.synchronization.SynchronizationComponent;
import org.dhis2.usescases.synchronization.SynchronizationModule;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = LoginModule.class)
public interface LoginComponent {
    void inject(LoginActivity loginActivity);
}
