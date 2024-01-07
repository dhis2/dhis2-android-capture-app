package org.dhis2.usescases.teidashboard;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.usescases.teidashboard.dashboardfragments.indicators.IndicatorsComponent;
import org.dhis2.usescases.teidashboard.dashboardfragments.indicators.IndicatorsModule;
import org.dhis2.usescases.notes.NotesComponent;
import org.dhis2.usescases.notes.NotesModule;
import org.dhis2.usescases.teidashboard.dashboardfragments.teidata.TEIDataComponent;
import org.dhis2.usescases.teidashboard.dashboardfragments.teidata.TEIDataModule;

import androidx.annotation.NonNull;
import dagger.Subcomponent;

/**
 * Created by ppajuelo on 30/11/2017.
 */
@PerActivity
@Subcomponent(modules = {TeiDashboardModule.class, ViewModelFactoryModule.class})
public interface TeiDashboardComponent {

    @NonNull
    IndicatorsComponent plus(IndicatorsModule indicatorsModule);

    @NonNull
    NotesComponent plus(NotesModule notesModule);

    @NonNull
    TEIDataComponent plus(TEIDataModule teiDataModule);

    void inject(TeiDashboardMobileActivity mobileActivity);
}
