package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import org.dhis2.data.dagger.PerFragment;

import dagger.Subcomponent;

@PerFragment
@Subcomponent(modules = IndicatorsModule.class)
public interface IndicatorsComponent {
    void inject(IndicatorsFragment indicatorsFragment);
}
