package org.dhis2.usescases.programEventDetail;

import androidx.annotation.NonNull;

import org.dhis2.commons.filters.data.FilterPresenter;
import org.dhis2.commons.filters.data.FilterRepository;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController;
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper;
import org.dhis2.commons.filters.workingLists.WorkingListItem;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

import static org.dhis2.utils.analytics.matomo.Actions.OPEN_ANALYTICS;
import static org.dhis2.utils.analytics.matomo.Actions.SYNC_EVENT;
import static org.dhis2.utils.analytics.matomo.Categories.EVENT_LIST;
import static org.dhis2.utils.analytics.matomo.Labels.CLICK;

public class ProgramEventDetailPresenter implements ProgramEventDetailContract.Presenter {

    private final ProgramEventDetailRepository eventRepository;
    private final SchedulerProvider schedulerProvider;
    private final FilterManager filterManager;
    private final FilterRepository filterRepository;
    private ProgramEventDetailContract.View view;
    CompositeDisposable compositeDisposable;
    private FlowableProcessor<Unit> listDataProcessor;
    private EventFilterToWorkingListItemMapper workingListMapper;
    private DisableHomeFiltersFromSettingsApp disableHomFilters;
    private MatomoAnalyticsController matomoAnalyticsController;

    public ProgramEventDetailPresenter(
            ProgramEventDetailContract.View view,
            @NonNull ProgramEventDetailRepository programEventDetailRepository,
            SchedulerProvider schedulerProvider,
            FilterManager filterManager,
            EventFilterToWorkingListItemMapper workingListMapper,
            FilterRepository filterRepository,
            FilterPresenter filterPresenter,
            DisableHomeFiltersFromSettingsApp disableHomFilters,
            MatomoAnalyticsController matomoAnalyticsController) {
        this.view = view;
        this.eventRepository = programEventDetailRepository;
        this.schedulerProvider = schedulerProvider;
        this.filterManager = filterManager;
        this.workingListMapper = workingListMapper;
        this.filterRepository = filterRepository;
        this.disableHomFilters = disableHomFilters;
        this.matomoAnalyticsController = matomoAnalyticsController;
        compositeDisposable = new CompositeDisposable();
        listDataProcessor = PublishProcessor.create();
    }

    @Override
    public void init() {
        compositeDisposable.add(
                Observable.just(filterRepository.programFilters(getProgram().uid()))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                filters -> {
                                    if (filters.isEmpty()) {
                                        view.hideFilters();
                                    } else {
                                        view.setFilterItems(filters);
                                    }
                                },
                                Timber::e
                        )
        );

        compositeDisposable.add(FilterManager.getInstance().getCatComboRequest()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        catComboUid -> view.showCatOptComboDialog(catComboUid),
                        Timber::e
                )
        );

        compositeDisposable.add(Single.zip(
                Single.just(eventRepository.getAccessDataWrite()),
                eventRepository.hasAccessToAllCatOptions(),
                (hasWritePermission, hasAccessToAllCatOptions) ->
                        hasWritePermission && hasAccessToAllCatOptions)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        view::setWritePermission,
                        Timber::e)
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
                filterManager.ouTreeFlowable()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                open -> view.openOrgUnitTreeSelector(),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                filterManager.asFlowable().onBackpressureLatest()
                        .doOnNext(filterManager -> view.showFilterProgress())
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                filterManager -> view.updateFilters(filterManager.getTotalFilters()),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                filterManager.getPeriodRequest()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                periodRequest -> view.showPeriodRequest(periodRequest.getFirst()),
                                Timber::e
                        ));
    }

    @Override
    public void onSyncIconClick(String uid) {
        matomoAnalyticsController.trackEvent(EVENT_LIST, SYNC_EVENT, CLICK);
        view.showSyncDialog(uid);
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
    public FeatureType getFeatureType() {
        return eventRepository.featureType().blockingGet();
    }

    @Override
    public List<WorkingListItem> workingLists() {
        return eventRepository.workingLists().toFlowable()
                .flatMapIterable(data -> data)
                .map(eventFilter -> workingListMapper.map(eventFilter))
                .toList().blockingGet();
    }

    @Override
    public void clearOtherFiltersIfWebAppIsConfig() {
        List<FilterItem> filters = filterRepository.homeFilters();
        disableHomFilters.execute(filters);
    }

    @Override
    public void setOpeningFilterToNone(){
        filterRepository.collapseAllFilters();
    }

    @Override
    public String getStageUid() {
        return eventRepository.programStage().blockingGet().uid();
    }

    @Override
    public void setOrgUnitFilters(List<OrganisationUnit> selectedOrgUnits) {
        FilterManager.getInstance().addOrgUnits(selectedOrgUnits);
    }

    @Override
    public void trackEventProgramAnalytics() {
        matomoAnalyticsController.trackEvent(EVENT_LIST, OPEN_ANALYTICS, CLICK);
    }
}
