package org.dhis2.usescases.programEventDetail;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.utils.granular_sync.SyncStatusDialog;
import org.dhis2.utils.Constants;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;


/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailPresenter implements ProgramEventDetailContract.Presenter {

    private final ProgramEventDetailRepository eventRepository;
    private final SchedulerProvider schedulerProvider;
    private ProgramEventDetailContract.View view;
    protected String programId;
    private CompositeDisposable compositeDisposable;

    //Search fields
    private FlowableProcessor<Pair<String, LatLng>> eventInfoProcessor;
    private FlowableProcessor<Unit> mapProcessor;

    ProgramEventDetailPresenter(
            @NonNull String programUid, @NonNull ProgramEventDetailRepository programEventDetailRepository, SchedulerProvider schedulerProvider) {
        this.eventRepository = programEventDetailRepository;
        this.programId = programUid;
        this.schedulerProvider = schedulerProvider;
        eventInfoProcessor = PublishProcessor.create();
        mapProcessor = PublishProcessor.create();
    }

    @Override
    public void init(ProgramEventDetailContract.View view) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(eventRepository.featureType()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        view.setFeatureType(),
                        Timber.tag("EVENTLIST")::e
                )
        );

        compositeDisposable.add(Observable.just(eventRepository.getAccessDataWrite())
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        view::setWritePermission,
                        Timber::e)
        );


        compositeDisposable.add(
                eventRepository.hasAccessToAllCatOptions()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setOptionComboAccess,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                eventRepository.program()
                        .observeOn(schedulerProvider.ui())
                        .subscribeOn(schedulerProvider.computation())
                        .subscribe(
                                view::setProgram,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                eventRepository.catOptionCombos()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setCatOptionComboFilter,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                FilterManager.getInstance().asFlowable()
                        .startWith(FilterManager.getInstance())
                        .map(filterManager -> eventRepository.filteredProgramEvents(
                                filterManager.getPeriodFilters(),
                                filterManager.getOrgUnitUidsFilters(),
                                filterManager.getCatOptComboFilters(),
                                filterManager.getEventStatusFilters(),
                                filterManager.getStateFilters()
                        ))
                        .subscribeOn(schedulerProvider.computation())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setLiveData,
                                throwable -> view.renderError(throwable.getMessage())
                        ));

        compositeDisposable.add(
                mapProcessor
                        .flatMap(unit ->
                                FilterManager.getInstance().asFlowable()
                                        .startWith(FilterManager.getInstance())
                                        .flatMap(filterManager -> eventRepository.filteredEventsForMap(
                                                filterManager.getPeriodFilters(),
                                                filterManager.getOrgUnitUidsFilters(),
                                                filterManager.getCatOptComboFilters(),
                                                filterManager.getEventStatusFilters(),
                                                filterManager.getStateFilters()
                                        )))
                        .subscribeOn(schedulerProvider.computation())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view.setMap(),
                                throwable -> view.renderError(throwable.getMessage())
                        ));

        compositeDisposable.add(
                eventInfoProcessor
                        .flatMap(eventInfo -> eventRepository.getInfoForEvent(eventInfo.val0())
                                .map(eventData -> Pair.create(eventData, eventInfo.val1())))
                        .subscribeOn(schedulerProvider.computation())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setEventInfo,
                                throwable -> view.renderError(throwable.getMessage())
                        ));

        compositeDisposable.add(
                FilterManager.getInstance().ouTreeFlowable()
                        .doOnNext(queryData -> {
                            if (view.isMapVisible())
                                mapProcessor.onNext(new Unit());
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                open -> view.openOrgUnitTreeSelector(),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                FilterManager.getInstance().asFlowable()
                        .doOnNext(queryData -> {
                            if (view.isMapVisible())
                                mapProcessor.onNext(new Unit());
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                filterManager -> view.updateFilters(filterManager.getTotalFilters()),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                FilterManager.getInstance().getPeriodRequest()
                        .doOnNext(queryData -> {
                            if (view.isMapVisible())
                                mapProcessor.onNext(new Unit());
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                periodRequest -> view.showPeriodRequest(periodRequest),
                                Timber::e
                        ));
    }

    @Override
    public void onSyncIconClick(String uid) {
        view.showSyncDialog(
                new SyncStatusDialog.Builder()
                .setConflictType(SyncStatusDialog.ConflictType.EVENT)
                .setUid(uid)
                .onDismissListener(hasChanged->{
                    if(hasChanged)
                        FilterManager.getInstance().publishData();

                })
                .build()
        );
    }

    @Override
    public void getEventInfo(String eventUid, LatLng latLng) {
        eventInfoProcessor.onNext(Pair.create(eventUid, latLng));
    }

    @Override
    public void getMapData() {
        mapProcessor.onNext(new Unit());
    }

    @Override
    public void onEventClick(String eventId, String orgUnit) {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programId);
        bundle.putString(Constants.EVENT_UID, eventId);
        bundle.putString(ORG_UNIT, orgUnit);
        view.startActivity(EventCaptureActivity.class,
                EventCaptureActivity.getActivityBundle(eventId, programId),
                false, false, null
        );
    }

    public void addEvent() {
        view.startNewEvent();
    }

    @Override
    public void onBackClick() {

        view.back();
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    @Override
    public void showFilter() {
        view.showHideFilter();
    }

    @Override
    public void clearFilterClick() {
        FilterManager.getInstance().clearAllFilters();
        view.clearFilters();
    }

}
