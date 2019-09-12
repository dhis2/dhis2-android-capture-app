package org.dhis2.usescases.programEventDetail;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.main.program.SyncStatusDialog;
import org.dhis2.utils.Constants;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;


/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailPresenter implements ProgramEventDetailContract.Presenter {

    private final ProgramEventDetailRepository eventRepository;
    private ProgramEventDetailContract.View view;
    protected String programId;
    private CompositeDisposable compositeDisposable;
    private FlowableProcessor<Trio<List<DatePeriod>, List<String>, List<CategoryOptionCombo>>> programQueries;

    //Search fields
    private List<DatePeriod> currentDateFilter;
    private List<String> currentOrgUnitFilter;
    private List<CategoryOptionCombo> currentCatOptionCombo;
    private FlowableProcessor<Boolean> processorDismissDialog;
    private FlowableProcessor<Pair<String, LatLng>> eventInfoProcessor;
    private FlowableProcessor<Unit> mapProcessor;

    ProgramEventDetailPresenter(
            @NonNull String programUid, @NonNull ProgramEventDetailRepository programEventDetailRepository) {
        this.eventRepository = programEventDetailRepository;
        this.programId = programUid;
        this.currentCatOptionCombo = new ArrayList<>();
        eventInfoProcessor = PublishProcessor.create();
        mapProcessor = PublishProcessor.create();
    }

    @Override
    public void init(ProgramEventDetailContract.View view) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();
        this.currentOrgUnitFilter = new ArrayList<>();
        this.currentDateFilter = new ArrayList<>();
        programQueries = PublishProcessor.create();

        this.processorDismissDialog = PublishProcessor.create();

        compositeDisposable.add(eventRepository.featureType()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.setFeatureType(),
                        Timber.tag("EVENTLIST")::e
                )
        );

        compositeDisposable.add(Observable.just(eventRepository.getAccessDataWrite())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setWritePermission,
                        Timber::e)
        );


        compositeDisposable.add(
                eventRepository.hasAccessToAllCatOptions()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setOptionComboAccess,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                eventRepository.program()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.computation())
                        .subscribe(
                                view::setProgram,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                eventRepository.catOptionCombos()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
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
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.setMap(),
                                throwable -> view.renderError(throwable.getMessage())
                        ));

        compositeDisposable.add(
                eventInfoProcessor
                        .flatMap(eventInfo -> eventRepository.getInfoForEvent(eventInfo.val0())
                                .map(eventData -> Pair.create(eventData, eventInfo.val1())))
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setEventInfo,
                                throwable -> view.renderError(throwable.getMessage())
                        ));

        compositeDisposable.add(
                FilterManager.getInstance().ouTreeFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                open -> view.openOrgUnitTreeSelector(),
                                Timber::e
                        )
        );

        compositeDisposable.add(processorDismissDialog
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        bool -> init(view),
                        Timber::d));

        compositeDisposable.add(
                FilterManager.getInstance().asFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                filterManager -> view.updateFilters(filterManager.getTotalFilters()),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                FilterManager.getInstance().getPeriodRequest()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                periodRequest -> view.showPeriodRequest(periodRequest),
                                Timber::e
                        ));
    }

    @Override
    public void updateDateFilter(List<DatePeriod> datePeriodList) {
        this.currentDateFilter = datePeriodList;
        programQueries.onNext(Trio.create(currentDateFilter, currentOrgUnitFilter, currentCatOptionCombo));
    }

    @Override
    public void onSyncIconClick(String uid) {
        view.showSyncDialog(uid, SyncStatusDialog.ConflictType.EVENT, processorDismissDialog);
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
