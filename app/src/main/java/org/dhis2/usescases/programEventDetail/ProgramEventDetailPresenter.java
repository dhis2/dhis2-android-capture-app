package org.dhis2.usescases.programEventDetail;
import androidx.annotation.NonNull;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.common.Unit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailPresenter implements ProgramEventDetailContract.Presenter {

    private final ProgramEventDetailRepository eventRepository;
    private final SchedulerProvider schedulerProvider;
    private final FilterManager filterManager;
    private ProgramEventDetailContract.View view;
    CompositeDisposable compositeDisposable;

    //Search fields
    FlowableProcessor<Pair<String, LatLng>> eventInfoProcessor;
    FlowableProcessor<Unit> mapProcessor;

    public ProgramEventDetailPresenter(
            ProgramEventDetailContract.View view,
            @NonNull ProgramEventDetailRepository programEventDetailRepository,
            SchedulerProvider schedulerProvider,
            FilterManager filterManager) {
        this.view = view;
        this.eventRepository = programEventDetailRepository;
        this.schedulerProvider = schedulerProvider;
        this.filterManager = filterManager;
        eventInfoProcessor = PublishProcessor.create();
        mapProcessor = PublishProcessor.create();
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init() {
        compositeDisposable.add(eventRepository.featureType()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        featureType -> view.setFeatureType(),
                        Timber::e
                )
        );

        compositeDisposable.add(Observable.just(eventRepository.getAccessDataWrite())
                .subscribeOn(schedulerProvider.io())
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
                        .subscribeOn(schedulerProvider.io())
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
                filterManager.asFlowable()
                        .startWith(filterManager)
                        .map(filterManager -> eventRepository.filteredProgramEvents(
                                filterManager.getPeriodFilters(),
                                filterManager.getOrgUnitUidsFilters(),
                                filterManager.getCatOptComboFilters(),
                                filterManager.getEventStatusFilters(),
                                filterManager.getStateFilters(),
                                filterManager.getSortingItem(),
                                filterManager.getAssignedFilter()
                        ))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setLiveData,
                                throwable -> view.renderError(throwable.getMessage())
                        ));

        compositeDisposable.add(
                mapProcessor
                        .observeOn(schedulerProvider.io())
                        .switchMap(unit ->
                                filterManager.asFlowable()
                                        .startWith(FilterManager.getInstance())
                                        .flatMap(filterManager -> eventRepository.filteredEventsForMap(
                                                filterManager.getPeriodFilters(),
                                                filterManager.getOrgUnitUidsFilters(),
                                                filterManager.getCatOptComboFilters(),
                                                filterManager.getEventStatusFilters(),
                                                filterManager.getStateFilters(),
                                                filterManager.getAssignedFilter()
                                        )))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                map -> view.setMap(map.component1(), map.component2()),
                                throwable -> view.renderError(throwable.getMessage())
                        ));

        compositeDisposable.add(
                eventInfoProcessor
                        .flatMap(eventInfo -> eventRepository.getInfoForEvent(eventInfo.val0())
                                .map(eventData -> Pair.create(eventData, eventInfo.val1())))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setEventInfo,
                                throwable -> view.renderError(throwable.getMessage())
                        ));

        compositeDisposable.add(
                filterManager.ouTreeFlowable()
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
                filterManager.asFlowable()
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
                filterManager.getPeriodRequest()
                        .doOnNext(queryData -> {
                            if (view.isMapVisible())
                                mapProcessor.onNext(new Unit());
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                periodRequest -> view.showPeriodRequest(periodRequest.getFirst()),
                                Timber::e
                        ));
    }

    @Override
    public void onSyncIconClick(String uid) {
        view.showSyncDialog(uid);
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
        view.navigateToEvent(eventId, orgUnit);
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
        filterManager.clearAllFilters();
        view.clearFilters();
    }

    @Override
    public boolean hasAssignment() {
        return eventRepository.hasAssignment();
    }
}
