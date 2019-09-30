package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.sharedPreferences.SharePreferencesProvider;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
@PerFragment
@Module
public class TEIDataModule {

    private final String programUid;
    private final String teiUid;

    public TEIDataModule(String programUid, String teiUid) {
        this.programUid = programUid;
        this.teiUid = teiUid;
    }

    @Provides
    @PerFragment
    TEIDataContracts.Presenter providesPresenter(D2 d2, DashboardRepository dashboardRepository,
                                                 SharePreferencesProvider provider) {
        return new TEIDataPresenterImpl(d2, dashboardRepository, programUid, teiUid,provider);
    }

}
