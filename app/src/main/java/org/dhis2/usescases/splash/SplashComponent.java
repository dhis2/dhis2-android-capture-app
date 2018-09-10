package org.dhis2.usescases.splash;


import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = SplashModule.class)
public interface SplashComponent {
    void inject(SplashActivity splashActivity);
}
