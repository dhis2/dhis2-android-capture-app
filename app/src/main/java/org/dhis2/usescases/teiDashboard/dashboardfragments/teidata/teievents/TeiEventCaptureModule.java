package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents;

import androidx.annotation.NonNull;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.usescases.eventswithoutregistration.eventCapture.EventCaptureContract;
import org.dhis2.usescases.eventswithoutregistration.eventCapture.EventCapturePresenterImpl;
import org.dhis2.usescases.eventswithoutregistration.eventCapture.EventCaptureRepositoryImpl;
import org.dhis2.usescases.eventswithoutregistration.eventCapture.domain.ConfigureEventCompletionDialog;
import org.dhis2.usescases.eventswithoutregistration.eventCapture.provider.EventCaptureResourcesProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public class TeiEventCaptureModule {

    private final String eventUid;
    private final EventCaptureContract.View view;

    public TeiEventCaptureModule(EventCaptureContract.View view, String eventUid, boolean isPortrait) {
        this.view = view;
        this.eventUid = eventUid;
    }

    @Provides
    @PerActivity
    EventCaptureContract.Presenter providePresenter(@NonNull EventCaptureContract.EventCaptureRepository eventCaptureRepository,
                                                    SchedulerProvider schedulerProvider,
                                                    PreferenceProvider preferences,
                                                    ConfigureEventCompletionDialog configureEventCompletionDialog) {
        return new EventCapturePresenterImpl(
                view,
                eventUid,
                eventCaptureRepository,
                schedulerProvider,
                preferences,
                configureEventCompletionDialog);
    }

    @Provides
    @PerActivity
    EventCaptureContract.EventCaptureRepository provideRepository(D2 d2) {
        return new EventCaptureRepositoryImpl(eventUid, d2);
    }

    @Provides
    @PerActivity
    ConfigureEventCompletionDialog provideConfigureEventCompletionDialog(
            EventCaptureResourcesProvider eventCaptureResourcesProvider
    ) {
        return new ConfigureEventCompletionDialog(eventCaptureResourcesProvider);
    }

    @Provides
    @PerActivity
    EventCaptureResourcesProvider provideEventCaptureResourcesProvider(
            ResourceManager resourceManager
    ) {
        return new EventCaptureResourcesProvider(resourceManager);
    }

}