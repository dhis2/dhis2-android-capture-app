package com.dhis2.usescases.login;


import com.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = LoginModule.class)
public interface LoginComponent {
    void inject(LoginActivity loginActivity);
}
