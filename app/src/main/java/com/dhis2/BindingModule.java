package com.dhis2;

import com.dhis2.usescases.login.LoginActivity;
import com.dhis2.usescases.login.LoginContractsModule;
import com.dhis2.usescases.main.program.ProgramContractModule;
import com.dhis2.usescases.main.program.ProgramFragment;
import com.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.usescases.splash.SplashActivity;
import com.dhis2.usescases.splash.SplashContractsModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by ppajuelo on 10/10/2017.
 */
@Module
abstract class BindingModule {

    /*All activities must be declare in the BindingModule following this pattern:
    *
    * @ContriburesAndroidInjector(modules = ACTIVITYMODULE.class)
    * abstract ACTIVITY bindNAMEActivity();
    *
    * */

    @ContributesAndroidInjector(modules = SplashContractsModule.class)
    abstract SplashActivity splashActivity();

    @ContributesAndroidInjector(modules = LoginContractsModule.class)
    abstract LoginActivity loginActivity();

    @ContributesAndroidInjector(modules = ProgramContractModule.class)
    abstract ProgramFragment programFragment();

    @ContributesAndroidInjector(modules = SearchTEContractsModule.class)
    abstract SearchTEActivity searchTEActivity();
}
