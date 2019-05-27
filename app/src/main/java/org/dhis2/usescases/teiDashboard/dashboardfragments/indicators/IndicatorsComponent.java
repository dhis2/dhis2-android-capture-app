package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import org.dhis2.data.dagger.PerFragment;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
@PerFragment
@Subcomponent(modules = IndicatorsModule.class)
public interface IndicatorsComponent {

    void inject(IndicatorsFragment indicatorsFragment);

}
