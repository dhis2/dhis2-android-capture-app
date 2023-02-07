package org.dhis2.usescases.teiDashboard;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureModule;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.EventCaptureFormComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.EventCaptureFormModule;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsComponent;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsModule;
import org.dhis2.usescases.notes.NotesComponent;
import org.dhis2.usescases.notes.NotesModule;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipComponent;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipModule;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataComponent;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataModule;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.TeiEventCaptureModule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dagger.Subcomponent;

/**
 * Created by ppajuelo on 30/11/2017.
 */
@PerActivity
@Subcomponent(modules = {TeiDashboardModule.class
//        , TeiEventCaptureModule.class
})
public interface TeiDashboardComponent {

    @NonNull
    IndicatorsComponent plus(IndicatorsModule indicatorsModule);

    @NonNull
    NotesComponent plus(NotesModule notesModule);

    @NonNull
    TEIDataComponent plus(TEIDataModule teiDataModule);
//    TODO: reversed
//    @NonNull
//    EventCaptureFormComponent plus (EventCaptureFormModule eventCaptureFormModule);

    void inject(TeiDashboardMobileActivity mobileActivity);
}
