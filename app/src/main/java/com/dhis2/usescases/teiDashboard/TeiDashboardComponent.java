package com.dhis2.usescases.teiDashboard;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import com.dhis2.usescases.teiDashboard.tablet.TeiDashboardTabletActivity;

import dagger.Subcomponent;

/**
 * Created by ppajuelo on 30/11/2017.
 */
@PerActivity
@Subcomponent(modules = TeiDashboardModule.class)
public interface TeiDashboardComponent {
    void inject(TeiDashboardMobileActivity mobileActivity);

//    void inject(TeiDashboardTabletActivity tabletActivity);
}
