package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;

public interface IndicatorsPresenter extends TeiDashboardContracts.Presenter {

    void subscribeToIndicators(IndicatorsFragment indicatorsFragment);

}
