package org.dhis2.usescases.programEventDetail;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.common.Unit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
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
    private FlowableProcessor<Unit> listDataProcessor;

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
        listDataProcessor = PublishProcessor.create();
    }

    @Override
    public void init() {
        compositeDisposable.add(FilterManager.getInstance().getCatComboRequest()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        catComboUid -> view.showCatOptComboDialog(catComboUid),
                        t8 -> Timber.e(t8)
                )
        );
        compositeDisposable.add(eventRepository.featureType()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        view::setFeatureType,
                        t7 -> Timber.e(t7)
                )
        );

        compositeDisposable.add(Observable.just(eventRepository.getAccessDataWrite())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        view::setWritePermission,
                        t6 -> Timber.e(t6))
        );


        compositeDisposable.add(
                eventRepository.hasAccessToAllCatOptions()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setOptionComboAccess,
                                t5 -> Timber.e(t5)
                        )
        );

        compositeDisposable.add(
                eventRepository.program()
                        .observeOn(schedulerProvider.ui())
                        .subscribeOn(schedulerProvider.io())
                        .subscribe(
                                view::setProgram,
                                t4 -> Timber.e(t4)
                        )
        );

        compositeDisposable.add(
                eventRepository.catOptionCombos()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setCatOptionComboFilter,
                                t3 -> Timber.e(t3)
                        )
        );

        ConnectableFlowable<FilterManager> updaterFlowable = filterManager.asFlowable()
                .startWith(filterManager)
                .publish();

        compositeDisposable.add(
                updaterFlowable
                        .observeOn(schedulerProvider.io())
                        .map(data -> view.isMapVisible())
                        .subscribeOn(schedulerProvider.io())
                        .subscribe(
                                isMapVisible -> {
                                    if (isMapVisible) {
                                        mapProcessor.onNext(new Unit());
                                    } else {
                                        listDataProcessor.onNext(new Unit());
                                    }
                                },
                                t2 -> Timber.e(t2)
                        )
        );

        compositeDisposable.add(
                listDataProcessor
                        .map(next -> eventRepository.filteredProgramEvents(
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
                                map -> view.setMap(map.component1(), map.component2(), map.component3()),
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
                                t1 -> Timber.e(t1)
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
                                t -> Timber.e(t)
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
                                t -> Timber.e(t)
                        ));

        updaterFlowable.connect();
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

    @Override
    public void filterCatOptCombo(String selectedCatOptionCombo) {
        FilterManager.getInstance().addCatOptCombo(
                eventRepository.getCatOptCombo(selectedCatOptionCombo)
        );
    }
}
