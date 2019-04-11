package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
@PerFragment
@Module
public class IndicatorsModule {

    private final String programUid;
    private final String teiUid;

    public IndicatorsModule(String programUid, String teiUid) {
        this.programUid = programUid;
        this.teiUid = teiUid;
    }

    @Provides
    @PerFragment
    IndicatorsContracts.Presenter providesPresenter(D2 d2, DashboardRepository dashboardRepository, RuleEngineRepository ruleEngineRepository) {
        return new IndicatorsPresenterImpl(d2, programUid, teiUid, dashboardRepository, ruleEngineRepository);
    }

}
