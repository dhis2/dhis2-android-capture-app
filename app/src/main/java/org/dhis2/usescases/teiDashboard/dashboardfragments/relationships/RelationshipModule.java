package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
@PerFragment
@Module
public class RelationshipModule {

    private final String programUid;
    private final String teiUid;
    private final RelationshipView view;

    public RelationshipModule(String programUid, String teiUid, RelationshipView view) {
        this.programUid = programUid;
        this.teiUid = teiUid;
        this.view = view;
    }

    @Provides
    @PerFragment
    RelationshipPresenter providesPresenter(D2 d2, DashboardRepository dashboardRepository, SchedulerProvider schedulerProvider) {
        return new RelationshipPresenter(d2, programUid, teiUid, dashboardRepository, schedulerProvider, view);
    }

}
