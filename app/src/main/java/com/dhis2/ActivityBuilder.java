package com.dhis2;

import com.dhis2.usescases.login.LoginActivity;
import com.dhis2.usescases.login.LoginContractsModule;
import com.dhis2.usescases.main.MainActivity;
import com.dhis2.usescases.main.MainContractsModule;
import com.dhis2.usescases.splash.SplashActivity;
import com.dhis2.usescases.splash.SplashContractsModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by ppajuelo on 10/10/2017.
 */
@Module
public abstract class ActivityBuilder {

    /*All activities must be declare in the ActivityBuilder following this pattern:
    *
    * @ContriburesAndroidInjector(modules = ACTIVITYMODULE.class)
    * abstract ACTIVITY bindNAMEActivity();
    *
    * */

    @ContributesAndroidInjector(modules = SplashContractsModule.class)
    abstract SplashActivity bindSplashnActivity();

    @ContributesAndroidInjector(modules = LoginContractsModule.class)
    abstract LoginActivity bindLoginActivity();

    @ContributesAndroidInjector(modules = MainContractsModule.class)
    abstract MainActivity bindMainActivity();

}
