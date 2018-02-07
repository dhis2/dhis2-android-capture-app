package com.dhis2.usescases.splash;


import com.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = SplashModule.class)
public interface SplashComponent {
    void inject(SplashActivity splashActivity);
}
