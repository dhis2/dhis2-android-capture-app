package org.dhis2.usescases.programEventDetail;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.dhis2.data.prefs.Preference;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.program.Program;

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
    private final PreferenceProvider preferences;
    private ProgramEventDetailContract.View view;
    CompositeDisposable compositeDisposable;
    private FlowableProcessor<Unit> listDataProcessor;

    //Search fields
    FlowableProcessor<String> eventInfoProcessor;
    FlowableProcessor<Unit> mapProcessor;

    public ProgramEventDetailPresenter(
            ProgramEventDetailContract.View view,
            @NonNull ProgramEventDetailRepository programEventDetailRepository,
            SchedulerProvider schedulerProvider,
            FilterManager filterManager,
            PreferenceProvider preferenceProvider) {
        this.view = view;
        this.eventRepository = programEventDetailRepository;
        this.schedulerProvider = schedulerProvider;
        this.filterManager = filterManager;
        this.preferences = preferenceProvider;
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
                        Timber::e
                )
        );
        compositeDisposable.add(eventRepository.featureType()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        view::setFeatureType,
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

        ConnectableFlowable<FilterManager> updaterFlowable = filterManager.asFlowable()
                .startWith(filterManager)
                .publish();

        compositeDisposable.add(
                eventRepository.textTypeDataElements()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setTextTypeDataElementsFilter,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                updaterFlowable
                        .observeOn(schedulerProvider.io())
                        .map(data -> view.isMapVisible())
                        .subscribeOn(schedulerProvider.io())
                        .subscribe(
                                isMapVisible -> {
                                    view.showFilterProgress();
                                    if (isMapVisible) {
                                        mapProcessor.onNext(new Unit());
                                    } else {
                                        listDataProcessor.onNext(new Unit());
                                    }
                                },
                                Timber::e
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
                                filterManager.getAssignedFilter(),
                                filterManager.getTexValueFilter()
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
                        .flatMap(eventRepository::getInfoForEvent)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::updateEventCarouselItem,
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

        updaterFlowable.connect();
    }

    @Override
    public void onSyncIconClick(String uid) {
        view.showSyncDialog(uid);
    }

    @Override
    public void getEventInfo(String eventUid) {
        if(preferences.getBoolean(Preference.EVENT_COORDINATE_CHANGED,false)){
            getMapData();
        }
        eventInfoProcessor.onNext(eventUid);
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

    @Override
    public Program getProgram() {
        return eventRepository.program().blockingFirst();
    }

    @Override
    public FeatureType getFeatureType(){
        return eventRepository.featureType().blockingGet();
    }
}
