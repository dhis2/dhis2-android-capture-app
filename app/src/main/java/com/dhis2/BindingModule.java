package com.dhis2;

import com.dhis2.usescases.appInfo.AppInfoFragment;
import com.dhis2.usescases.appInfo.InfoModule;
import com.dhis2.usescases.enrollment.EnrollmentActivity;
import com.dhis2.usescases.enrollment.EnrollmentModule;
import com.dhis2.usescases.login.LoginActivity;
import com.dhis2.usescases.login.LoginContractsModule;
import com.dhis2.usescases.main.program.ProgramContractModule;
import com.dhis2.usescases.main.program.ProgramFragment;
import com.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.usescases.splash.SplashActivity;
import com.dhis2.usescases.splash.SplashContractsModule;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by ppajuelo on 10/10/2017.
 */
@Module
abstract class BindingModule {

    /*All activities must be declare in the BindingRepoModule following this pattern:
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

    @ContributesAndroidInjector(modules = InfoModule.class)
    abstract AppInfoFragment infoFragment();

    @ContributesAndroidInjector(modules = EnrollmentModule.class)
    abstract EnrollmentActivity enrollmentActivity();

}
