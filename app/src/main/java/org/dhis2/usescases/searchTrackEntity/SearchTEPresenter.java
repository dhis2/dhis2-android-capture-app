package org.dhis2.usescases.searchTrackEntity;

import static android.app.Activity.RESULT_OK;
import static org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment.TEI_A_UID;
import static org.dhis2.utils.analytics.AnalyticsConstants.ADD_RELATIONSHIP;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_ENROLL;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_RELATIONSHIP;
import static org.dhis2.utils.analytics.AnalyticsConstants.SEARCH_TEI;
import static org.dhis2.utils.analytics.matomo.Actions.SYNC_TEI;
import static org.dhis2.utils.analytics.matomo.Categories.TRACKER_LIST;
import static org.dhis2.utils.analytics.matomo.Labels.CLICK;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.content.res.AppCompatResources;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.FeatureCollection;

import org.dhis2.R;
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker;
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener;
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.data.FilterRepository;
import org.dhis2.commons.filters.workingLists.TeiFilterToWorkingListItemMapper;
import org.dhis2.commons.idlingresource.CountingIdlingResourceSingleton;
import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.dhislogic.DhisMapUtils;
import org.dhis2.data.search.SearchParametersModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.form.model.ActionType;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.RowAction;
import org.dhis2.uicomponents.map.geometry.mapper.EventsByProgramStage;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection;
import org.dhis2.uicomponents.map.mapper.EventToEventUiComponent;
import org.dhis2.uicomponents.map.model.EventUiComponentModel;
import org.dhis2.uicomponents.map.model.StageStyle;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModelExtensionsKt;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModelKt;
import org.dhis2.utils.DhisTextUtils;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.utils.ObjectStyleUtils;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController;
import org.dhis2.utils.customviews.OrgUnitDialog;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

public class SearchTEPresenter implements SearchTEContractsModule.Presenter {

    private static final Program ALL_TE_TYPES = null;
    private static final int MAX_NO_SELECTED_PROGRAM_RESULTS = 5;
    private final SearchRepository searchRepository;
    private final D2 d2;
    private final SchedulerProvider schedulerProvider;
    private final SearchTEContractsModule.View view;
    private final AnalyticsHelper analyticsHelper;
    private final BehaviorSubject<String> currentProgram;
    private final PreferenceProvider preferences;
    private final TeiFilterToWorkingListItemMapper workingListMapper;
    private final FilterRepository filterRepository;
    private Program selectedProgram;

    private final CompositeDisposable compositeDisposable;
    private TrackedEntityType trackedEntity;
    private HashMap<String, String> queryData;

    private Date selectedEnrollmentDate;

    private final FlowableProcessor<HashMap<String, String>> queryProcessor;
    private String trackedEntityType;
    private final FlowableProcessor<Unit> mapProcessor;
    private final FlowableProcessor<Unit> enrollmentMapProcessor;
    private Dialog dialogDisplayed;
    private final FlowableProcessor<Unit> mapDataProcessor;
    private final FlowableProcessor<Unit> listDataProcessor;

    private boolean showList = true;
    private final MapTeisToFeatureCollection mapTeisToFeatureCollection;
    private final MapTeiEventsToFeatureCollection mapTeiEventsToFeatureCollection;
    private final MapCoordinateFieldToFeatureCollection mapCoordinateFieldToFeatureCollection;
    private final EventToEventUiComponent eventToEventUiComponent;
    private boolean teiTypeHasAttributesToDisplay = true;
    private boolean isSearching;
    private final DhisMapUtils mapUtils;
    private final Flowable<RowAction> fieldProcessor;
    private final DisableHomeFiltersFromSettingsApp disableHomeFilters;
    private final MatomoAnalyticsController matomoAnalyticsController;
    private final SearchMessageMapper searchMessageMapper;

    public SearchTEPresenter(SearchTEContractsModule.View view,
                             D2 d2,
                             DhisMapUtils mapUtils,
                             SearchRepository searchRepository,
                             SchedulerProvider schedulerProvider,
                             AnalyticsHelper analyticsHelper,
                             @Nullable String initialProgram,
                             MapTeisToFeatureCollection mapTeisToFeatureCollection,
                             MapTeiEventsToFeatureCollection mapTeiEventsToFeatureCollection,
                             MapCoordinateFieldToFeatureCollection mapCoordinateFieldToFeatureCollection,
                             EventToEventUiComponent eventToEventUiComponent,
                             PreferenceProvider preferenceProvider,
                             TeiFilterToWorkingListItemMapper workingListMapper,
                             FilterRepository filterRepository,
                             Flowable<RowAction> fieldProcessor,
                             DisableHomeFiltersFromSettingsApp disableHomeFilters,
                             MatomoAnalyticsController matomoAnalyticsController,
                             SearchMessageMapper searchMessageMapper) {
        this.view = view;
        this.preferences = preferenceProvider;
        this.searchRepository = searchRepository;
        this.d2 = d2;
        this.mapUtils = mapUtils;
        this.schedulerProvider = schedulerProvider;
        this.analyticsHelper = analyticsHelper;
        this.mapTeisToFeatureCollection = mapTeisToFeatureCollection;
        this.mapTeiEventsToFeatureCollection = mapTeiEventsToFeatureCollection;
        this.mapCoordinateFieldToFeatureCollection = mapCoordinateFieldToFeatureCollection;
        this.fieldProcessor = fieldProcessor;
        this.searchMessageMapper = searchMessageMapper;
        this.workingListMapper = workingListMapper;
        this.eventToEventUiComponent = eventToEventUiComponent;
        this.filterRepository = filterRepository;
        this.disableHomeFilters = disableHomeFilters;
        this.matomoAnalyticsController = matomoAnalyticsController;
        compositeDisposable = new CompositeDisposable();
        queryData = new HashMap<>();
        queryProcessor = PublishProcessor.create();
        mapProcessor = PublishProcessor.create();
        enrollmentMapProcessor = PublishProcessor.create();
        mapDataProcessor = PublishProcessor.create();
        listDataProcessor = PublishProcessor.create();
        selectedProgram = initialProgram != null ? d2.programModule().programs().uid(initialProgram).blockingGet() : null;
        currentProgram = BehaviorSubject.createDefault(initialProgram != null ? initialProgram : "");
    }

    //-----------------------------------
    //region LIFECYCLE

    @Override
    public void init(String trackedEntityType) {
        this.trackedEntityType = trackedEntityType;
        this.trackedEntity = searchRepository.getTrackedEntityType(trackedEntityType).blockingFirst();

        compositeDisposable.add(currentProgram
                .switchMap(programUid ->
                        FilterManager.getInstance().asFlowable()
                                .startWith(FilterManager.getInstance())
                                .map(filterManager -> {
                                    if (programUid.isEmpty()) {
                                        return filterRepository.globalTrackedEntityFilters();
                                    } else {
                                        return filterRepository.programFilters(programUid);
                                    }
                                }).toObservable())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        filters -> {
                            if (!filters.isEmpty()) {
                                view.setInitialFilters(filters);
                            }
                        }
                        , Timber::e
                )
        );

        compositeDisposable.add(
                searchRepository.programsWithRegistration(trackedEntityType)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(programs -> {
                                    Collections.sort(programs, (program1, program2) -> program1.displayName().compareToIgnoreCase(program2.displayName()));
                                    if (selectedProgram != null) {
                                        setProgram(selectedProgram);
                                    } else {
                                        setProgram(ALL_TE_TYPES);
                                    }
                                    view.setPrograms(programs);
                                }, Timber::d
                        ));

        compositeDisposable.add(currentProgram
                .flatMap(programUid ->
                        searchRepository.searchFields(programUid, queryData))
                .subscribe(
                        data -> {
                            if (data.isEmpty()) {
                                teiTypeHasAttributesToDisplay = false;
                            }
                            populateList(data);
                        },
                        Timber::d)
        );

        compositeDisposable.add(fieldProcessor
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui())
                .subscribe(rowAction -> {
                    if (rowAction.getType() == ActionType.ON_TEXT_CHANGE ||
                            rowAction.getType() == ActionType.ON_SAVE
                    ) {
                        Map<String, String> queryDataBU = new HashMap<>(queryData);
                        view.setFabIcon(true);
                        updateQueryData(rowAction);

                        if (!queryData.equals(queryDataBU)) { //Only when queryData has changed
                            updateQueryData(rowAction);
                        }
                        view.showClearSearch(!queryData.isEmpty());

                        if (rowAction.getType() == ActionType.ON_SAVE) {
                            populateList(null);
                        }
                    }
                }, Timber::d)
        );


        ConnectableFlowable<Pair<HashMap<String, String>, FilterManager>> updaterFlowable = currentProgram.distinctUntilChanged().toFlowable(BackpressureStrategy.LATEST)
                .switchMap(program ->
                        Flowable.combineLatest(queryProcessor.startWith(queryData),
                                FilterManager.getInstance().asFlowable().startWith(FilterManager.getInstance()),
                                Pair::create)
                )
                .onBackpressureLatest()
                .publish();


        compositeDisposable.add(
                updaterFlowable
                        .observeOn(schedulerProvider.io())
                        .map(data -> view.isMapVisible())
                        .subscribeOn(schedulerProvider.io())
                        .subscribe(
                                isMapVisible -> {
                                    view.showFilterProgress();
                                    if (isMapVisible) {
                                        mapDataProcessor.onNext(new Unit());
                                    } else {
                                        listDataProcessor.onNext(new Unit());
                                    }
                                },
                                Timber::e
                        )
        );

        compositeDisposable.add(
                listDataProcessor
                        .switchMap(map -> {
                            CountingIdlingResourceSingleton.INSTANCE.increment();
                            return Flowable.just(searchRepository.searchTrackedEntities(
                                    new SearchParametersModel(
                                            selectedProgram,
                                            trackedEntityType,
                                            queryData
                                    ),
                                    canSearchOnline()));
                        })
                        .doOnError(this::handleError)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setLiveData, Timber::d)
        );

        compositeDisposable.add(
                mapDataProcessor
                        .switchMap(unit ->
                                searchRepository.searchTeiForMap(
                                        new SearchParametersModel(
                                                selectedProgram,
                                                trackedEntityType,
                                                queryData
                                        ),
                                        canSearchOnline()))
                        .map(teis -> new kotlin.Pair<>(teis, searchRepository.getEventsForMap(teis)))
                        .map(teis -> {
                                    Map<String, FeatureCollection> coordinateFields = new HashMap<>();
                                    Map<String, FeatureCollection> dataElements = mapCoordinateFieldToFeatureCollection.map(
                                            mapUtils.getCoordinateDataElementInfo(
                                                    EventViewModelKt.uids(teis.component2())
                                            ));
                                    Map<String, FeatureCollection> attributes = mapCoordinateFieldToFeatureCollection.map(
                                            mapUtils.getCoordinateAttributeInfo(
                                                    SearchTeiModelExtensionsKt.uids(teis.component1())
                                            )
                                    );
                                    coordinateFields.putAll(dataElements);
                                    coordinateFields.putAll(attributes);
                                    List<EventUiComponentModel> eventsUi = eventToEventUiComponent.mapList(teis.component2(), teis.component1());
                                    kotlin.Pair<HashMap<String, FeatureCollection>, BoundingBox> teisFeatCollection = mapTeisToFeatureCollection.map(teis.component1(), selectedProgram != null);
                                    EventsByProgramStage events = mapTeiEventsToFeatureCollection.map(eventsUi).component1();
                                    return new TrackerMapData(
                                            teis.component1(),
                                            events,
                                            teisFeatCollection.component1(),
                                            teisFeatCollection.component2(),
                                            eventsUi,
                                            coordinateFields
                                    );
                                }
                        )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setMap,
                                Timber::e,
                                () -> Timber.d("COMPLETED")
                        ));

        compositeDisposable.add(
                queryProcessor
                        .startWith(queryData)
                        .subscribeOn(schedulerProvider.ui())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(data -> view.clearData(), Timber::d)
        );

        compositeDisposable.add(
                FilterManager.getInstance().ouTreeFlowable()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                open -> view.openOrgUnitTreeSelector(),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                FilterManager.getInstance().getPeriodRequest()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::showPeriodRequest,
                                Timber::e
                        ));

        compositeDisposable.add(
                FilterManager.getInstance().asFlowable().onBackpressureLatest()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                filterManager -> view.updateFilters(filterManager.getTotalFilters()),
                                Timber::e
                        )
        );

        updaterFlowable.connect();

    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
    }
    //endregion

    //------------------------------------------
    //region DATA
    @Override
    public SearchMessageResult getMessage(List<SearchTeiModel> list) {
        return searchMessageMapper.getSearchMessage(
                list,
                selectedProgram,
                queryData,
                teiTypeHasAttributesToDisplay,
                MAX_NO_SELECTED_PROGRAM_RESULTS,
                getTrackedEntityName().displayName()
        );
    }

    private void handleError(Throwable throwable) {
        if (throwable instanceof D2Error) {
            D2Error exception = (D2Error) throwable;
            switch (exception.errorCode()) {
                case UNEXPECTED:
                    view.displayMessage(view.getContext().getString(R.string.online_search_unexpected));
                    break;
                case SEARCH_GRID_PARSE:
                    view.displayMessage(view.getContext().getString(R.string.online_search_parsing_error));
                    break;
                case API_RESPONSE_PROCESS_ERROR:
                    view.displayMessage(view.getContext().getString(R.string.online_search_error));
                    break;
                case API_UNSUCCESSFUL_RESPONSE:
                    view.displayMessage(view.getContext().getString(R.string.online_search_response_error));
                    break;
            }
        }
    }

    @Override
    public TrackedEntityType getTrackedEntityName() {
        return trackedEntity;
    }

    @Override
    public TrackedEntityType getTrackedEntityType(String trackedEntityTypeUid) {
        return searchRepository.getTrackedEntityType(trackedEntityTypeUid).blockingFirst();
    }

    @Override
    public Program getProgram() {
        return selectedProgram;
    }

    //endregion

    @Override
    public void setProgram(Program newProgramSelected) {
        if (newProgramSelected != ALL_TE_TYPES) {
       //     view.setProgramFilters();
            String previousProgramUid = selectedProgram != null ? selectedProgram.uid() : "";
            String currentProgramUid = newProgramSelected.uid();
            if (isPreviousAndCurrentProgramTheSame(newProgramSelected,
                    previousProgramUid,
                    currentProgramUid))
                return;
        } else {
        //    view.setNonProgramFilters();
        }

        boolean otherProgramSelected;
        if (newProgramSelected == null) {
            otherProgramSelected = selectedProgram != null;
        } else {
            otherProgramSelected = !newProgramSelected.equals(selectedProgram);
        }
        selectedProgram = newProgramSelected;
        view.clearList(newProgramSelected == null ? null : newProgramSelected.uid());
        view.clearData();
        view.setFabIcon(true);
        showList = true;

        if (otherProgramSelected) {
            preferences.removeValue(Preference.CURRENT_ORG_UNIT);
            queryData.clear();
            searchRepository.setCurrentProgram(newProgramSelected != null ? newProgramSelected.uid() : null);
            view.updateNavigationBar();
        }

        currentProgram.onNext(newProgramSelected != null ? newProgramSelected.uid() : "");
    }

    private boolean isPreviousAndCurrentProgramTheSame(Program programSelected, String previousProgramUid, String currentProgramUid) {
        return previousProgramUid != null && previousProgramUid.equals(currentProgramUid) ||
                programSelected == selectedProgram;
    }

    @Override
    public void resetSearch() {
        showList = true;
        queryData.clear();
        listDataProcessor.onNext(new Unit());
    }

    @Override
    public void onClearClick() {
        isSearching = true;
        queryData.clear();
        view.setFabIcon(true);
        view.showClearSearch(false);
        searchRepository.setCurrentProgram(selectedProgram != null ? selectedProgram.uid() : null);
        currentProgram.onNext(selectedProgram != null ? selectedProgram.uid() : "");
        queryProcessor.onNext(new HashMap<>());
    }

    @Override
    public void onBackClick() {
        view.onBackClicked();
    }

    @Override
    public void onFabClick(boolean needsSearch) {
        if (!needsSearch) {
            onEnrollClick();
        } else if (!selectedProgramMinNumberOfAttributesCheck()) {
            view.displayMinNumberOfAttributesMessage(selectedProgram.minAttributesRequiredToSearch());
        } else {
            isSearching = true;
            analyticsHelper.setEvent(SEARCH_TEI, CLICK, SEARCH_TEI);
            view.clearData();

            List<String> optionSetIds = new ArrayList<>();
            view.updateFiltersSearch(queryData.entrySet().size());

            for (Map.Entry<String, String> entry : queryData.entrySet()) {
                if (entry.getValue().equals("null_os_null"))
                    optionSetIds.add(entry.getKey());
            }
            for (String id : optionSetIds) {
                queryData.remove(id);
            }

            if (compliesWithMinAttributesToSearch()) {
                view.setFabIcon(false);
                queryProcessor.onNext(queryData);
            } else {
                if (selectedProgram.displayFrontPageList()) {
                    showList = false;
                }
                view.setFabIcon(true);
                queryProcessor.onNext(new HashMap<>());
            }
            view.closeFilters();
        }
    }

    private boolean compliesWithMinAttributesToSearch() {
        if (selectedProgram != null) {
            if (selectedProgram.displayFrontPageList() && queryData.isEmpty()) {
                return false;
            } else if (selectedProgram.displayFrontPageList() && !queryData.isEmpty() && queryData.size() < selectedProgram.minAttributesRequiredToSearch()) {
                return false;
            } else if (!selectedProgram.displayFrontPageList() && queryData.size() < selectedProgram.minAttributesRequiredToSearch()) {
                return false;
            } else if (!selectedProgram.displayFrontPageList() && queryData.size() == 0 && selectedProgram.minAttributesRequiredToSearch() == 0) {
                return true;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onEnrollClick() {
        if (selectedProgram != null)
            if (canCreateTei())
                enroll(selectedProgram.uid(), null);
            else
                view.displayMessage(view.getContext().getString(R.string.search_access_error));
        else
            view.displayMessage(view.getContext().getString(R.string.search_program_not_selected));
    }

    private boolean canCreateTei() {
        boolean programAccess = selectedProgram.access().data().write() != null && selectedProgram.access().data().write();
        boolean teTypeAccess = d2.trackedEntityModule().trackedEntityTypes().uid(
                selectedProgram.trackedEntityType().uid()
        ).blockingGet().access().data().write();
        return programAccess && teTypeAccess;
    }

    private void enroll(String programUid, String uid) {
        selectedEnrollmentDate = Calendar.getInstance().getTime();

        OrgUnitDialog orgUnitDialog = OrgUnitDialog.getInstace().setMultiSelection(false);
        orgUnitDialog.setTitle("Enrollment Org Unit")
                .setPossitiveListener(v -> {
                    if (orgUnitDialog.getSelectedOrgUnit() != null && !orgUnitDialog.getSelectedOrgUnit().isEmpty())
                        showEnrollmentDatePicker(orgUnitDialog.getSelectedOrgUnitModel(), programUid, uid);
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(v -> orgUnitDialog.dismiss());

        compositeDisposable.add(getOrgUnits()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        allOrgUnits -> {
                            if (allOrgUnits.size() > 1) {
                                orgUnitDialog.setOrgUnits(allOrgUnits);
                                orgUnitDialog.setProgram(programUid);
                                if (!orgUnitDialog.isAdded())
                                    orgUnitDialog.show(view.getAbstracContext().getSupportFragmentManager(), "OrgUnitEnrollment");
                            } else if (allOrgUnits.size() == 1)
                                showEnrollmentDatePicker(allOrgUnits.get(0), programUid, uid);
                        },
                        Timber::d
                )
        );
    }

    private void showEnrollmentDatePicker(OrganisationUnit selectedOrgUnit, String programUid, String uid) {
        showCalendar(selectedOrgUnit, programUid, uid);
    }

    private void showCalendar(OrganisationUnit selectedOrgUnit, String programUid, String uid) {
        Date minDate = null;
        Date maxDate = null;

        if (selectedOrgUnit.openingDate() != null)
            minDate = selectedOrgUnit.openingDate();

        if (selectedOrgUnit.closedDate() == null && !selectedProgram.selectEnrollmentDatesInFuture()) {
            maxDate = new Date(System.currentTimeMillis());
        } else if (selectedOrgUnit.closedDate() != null && !selectedProgram.selectEnrollmentDatesInFuture()) {
            if (selectedOrgUnit.closedDate().before(new Date(System.currentTimeMillis()))) {
                maxDate = selectedOrgUnit.closedDate();
            } else {
                maxDate = new Date(System.currentTimeMillis());
            }
        } else if (selectedOrgUnit.closedDate() != null && selectedProgram.selectEnrollmentDatesInFuture()) {
            maxDate = selectedOrgUnit.closedDate();
        }

        CalendarPicker dialog = new CalendarPicker(view.getContext());
        dialog.setTitle(selectedProgram.enrollmentDateLabel());
        dialog.setMinDate(minDate);
        dialog.setMaxDate(maxDate);
        dialog.isFutureDatesAllowed(true);
        dialog.setListener(new OnDatePickerListener() {
            @Override
            public void onNegativeClick() {
            }

            @Override
            public void onPositiveClick(@NotNull DatePicker datePicker) {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
                selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
                selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
                selectedCalendar.set(Calendar.MINUTE, 0);
                selectedCalendar.set(Calendar.SECOND, 0);
                selectedCalendar.set(Calendar.MILLISECOND, 0);
                selectedEnrollmentDate = selectedCalendar.getTime();

                enrollInOrgUnit(selectedOrgUnit.uid(), programUid, uid, selectedEnrollmentDate);
            }
        });
        dialog.show();
    }

    private void enrollInOrgUnit(String orgUnitUid, String programUid, String uid, Date enrollmentDate) {
        compositeDisposable.add(
                searchRepository.saveToEnroll(trackedEntity.uid(), orgUnitUid, programUid, uid, queryData, enrollmentDate, view.fromRelationshipTEI())
                        .subscribeOn(schedulerProvider.computation())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(enrollmentAndTEI -> {
                                    analyticsHelper.setEvent(CREATE_ENROLL, CLICK, CREATE_ENROLL);
                                    view.goToEnrollment(
                                            enrollmentAndTEI.val0(),
                                            selectedProgram.uid()
                                    );
                                },
                                Timber::d)
        );
    }

    @Override
    public void onTEIClick(String TEIuid, String enrollmentUid, boolean isOnline) {
        if (!isOnline) {
            openDashboard(TEIuid, enrollmentUid);
        } else
            downloadTei(TEIuid, enrollmentUid);
    }

    @Override
    public void addRelationship(@NonNull String teiUid, @Nullable String relationshipTypeUid, boolean online) {
        if (teiUid.equals(view.fromRelationshipTEI())) {
            view.displayMessage(view.getContext().getString(R.string.relationship_error_recursive));
        } else if (!online) {
            analyticsHelper.setEvent(ADD_RELATIONSHIP, CLICK, ADD_RELATIONSHIP);
            Intent intent = new Intent();
            intent.putExtra(TEI_A_UID, teiUid);
            if (relationshipTypeUid != null)
                intent.putExtra("RELATIONSHIP_TYPE_UID", relationshipTypeUid);
            view.getAbstractActivity().setResult(RESULT_OK, intent);
            view.getAbstractActivity().finish();
        } else {
            analyticsHelper.setEvent(ADD_RELATIONSHIP, CLICK, ADD_RELATIONSHIP);
            downloadTeiForRelationship(teiUid, relationshipTypeUid);
        }
    }

    @Override
    public void downloadTei(String teiUid, String enrollmentUid) {
        compositeDisposable.add(searchRepository.downloadTei(teiUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        view.downloadProgress(),
                        Timber::d,
                        () -> {
                            if (d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingExists()) {
                                openDashboard(teiUid, enrollmentUid);
                            } else {
                                view.couldNotDownload(trackedEntity.displayName());
                            }
                        })
        );
    }

    @Override
    public void downloadTeiWithReason(String teiUid, String enrollmentUid, String reason) {

    }

    @Override
    public String nameOUByUid(String uid) {
        OrganisationUnit organisationUnit = d2.organisationUnitModule().organisationUnits().uid(uid).blockingGet();
        return organisationUnit != null ? organisationUnit.name() : null;
    }

    @Override
    public void downloadTeiForRelationship(String TEIuid, @Nullable String relationshipTypeUid) {
        List<String> teiUids = new ArrayList<>();
        teiUids.add(TEIuid);
        compositeDisposable.add(
                d2.trackedEntityModule().trackedEntityInstanceDownloader()
                        .byUid().in(teiUids)
                        .overwrite(true)
                        .download()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view.downloadProgress(),
                                Timber::d,
                                () -> addRelationship(TEIuid, relationshipTypeUid, false))
        );
    }

    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits() {
        return searchRepository.getOrgUnits(selectedProgram != null ? selectedProgram.uid() : null);
    }

    private void openDashboard(String teiUid, String enrollmentUid) {
        view.openDashboard(teiUid, selectedProgram != null ? selectedProgram.uid() : null, enrollmentUid);
    }

    @Override
    public String getProgramColor(String uid) {
        return searchRepository.getProgramColor(uid);
    }

    @Override
    public HashMap<String, String> getQueryData() {
        return queryData;
    }

    @Override
    public void onSyncIconClick(String teiUid) {
        matomoAnalyticsController.trackEvent(TRACKER_LIST, SYNC_TEI, CLICK);
        view.showSyncDialog(
                new SyncStatusDialog.Builder()
                        .setConflictType(SyncStatusDialog.ConflictType.TEI)
                        .setUid(teiUid)
                        .onDismissListener(hasChanged -> {
                            if (hasChanged && view.isMapVisible())
                                mapProcessor.onNext(new Unit());
                            else if (hasChanged)
                                queryProcessor.onNext(queryData);
                        })
                        .build()
        );
    }

    @Override
    public void showFilter() {
        view.showHideFilter();
    }

    @Override
    public void showFilterGeneral() {
        view.showHideFilterGeneral();
    }

    @Override
    public void clearFilterClick() {
        view.clearFilters();
    }

    @Override
    public void closeFilterClick() {
        view.closeFilters();
    }

    @Override
    public void getMapData() {
        mapProcessor.onNext(new Unit());
        FilterManager.getInstance().publishData();
    }

    @Override
    public void getListData() {
        listDataProcessor.onNext(new Unit());
    }

    @Override
    public void getEnrollmentMapData() {
        enrollmentMapProcessor.onNext(new Unit());
    }

    @Override
    public Drawable getSymbolIcon() {
        TrackedEntityType teiType = d2.trackedEntityModule().trackedEntityTypes().withTrackedEntityTypeAttributes().uid(trackedEntityType).blockingGet();

        if (teiType.style() != null && teiType.style().icon() != null) {
            return
                    ObjectStyleUtils.getIconResource(view.getContext(), teiType.style().icon(), R.drawable.ic_default_icon);
        } else
            return AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_default_icon);
    }

    @Override
    public Drawable getEnrollmentSymbolIcon() {
        if (selectedProgram != null) {
            if (selectedProgram.style() != null && selectedProgram.style().icon() != null) {
                return ObjectStyleUtils.getIconResource(view.getContext(), selectedProgram.style().icon(), R.drawable.ic_default_outline);
            } else
                return AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_default_outline);
        }

        return null;
    }

    @Override
    public int getTEIColor() {
        TrackedEntityType teiType = d2.trackedEntityModule().trackedEntityTypes().withTrackedEntityTypeAttributes().uid(trackedEntityType).blockingGet();

        if (teiType.style() != null && teiType.style().color() != null) {
            return ColorUtils.parseColor(teiType.style().color());
        } else
            return -1;
    }

    @Override
    public int getEnrollmentColor() {
        if (selectedProgram != null && selectedProgram.style() != null && selectedProgram.style().color() != null)
            return ColorUtils.parseColor(selectedProgram.style().color());
        else
            return -1;
    }

    @Override
    public HashMap<String, StageStyle> getProgramStageStyle() {
        HashMap<String, StageStyle> stagesStyleMap = new HashMap<>();
        if (selectedProgram != null) {
            List<ProgramStage> programStages = d2.programModule().programStages().byProgramUid().eq(selectedProgram.uid()).byFeatureType().neq(FeatureType.NONE).blockingGet();
            for (ProgramStage stage : programStages) {
                int color;
                Drawable icon;
                if (stage.style() != null && stage.style().color() != null) {
                    color = ColorUtils.parseColor(stage.style().color());
                } else {
                    color = -1;
                }
                if (stage.style() != null && stage.style().icon() != null) {
                    icon = ObjectStyleUtils.getIconResource(view.getContext(), stage.style().icon(), R.drawable.ic_clinical_f_outline);
                } else {
                    icon = AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_clinical_f_outline);
                }
                stagesStyleMap.put(stage.displayName(), new StageStyle(color, icon));
            }
        }
        return stagesStyleMap;
    }

    @Override
    public void restoreQueryData(HashMap<String, String> queryData) {
        this.queryData = queryData;
    }

    @Override
    public void deleteRelationship(String relationshipUid) {
        try {
            d2.relationshipModule().relationships().withItems().uid(relationshipUid).blockingDelete();
        } catch (D2Error error) {
            Timber.d(error);
        } finally {
            analyticsHelper.setEvent(DELETE_RELATIONSHIP, CLICK, DELETE_RELATIONSHIP);
            mapProcessor.onNext(new Unit());
        }
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    @Override
    public void setProgramForTesting(Program program) {
        selectedProgram = program;
    }

    @Override
    public void clearOtherFiltersIfWebAppIsConfig() {
        List<FilterItem> filters = filterRepository.homeFilters();
        disableHomeFilters.execute(filters);
    }

    @Override
    public void populateList(List<FieldUiModel> list) {
        if (list != null) {
            view.setFabIcon(!list.isEmpty());
        }
        view.setFormData(list);
    }

    @Override
    public void setOrgUnitFilters(List<OrganisationUnit> selectedOrgUnits) {
        FilterManager.getInstance().addOrgUnits(selectedOrgUnits);
    }

    @Override
    public void checkFilters(boolean listResultIsOk) {
        boolean hasToShowFilters;
        if (currentProgram.blockingFirst().isEmpty()) {
            hasToShowFilters = !filterRepository.globalTrackedEntityFilters().isEmpty();
        } else {
            hasToShowFilters = !filterRepository
                    .programFilters(currentProgram.blockingFirst()).isEmpty();
        }

        if (listResultIsOk) {
            view.setFiltersVisibility(hasToShowFilters);
        } else if (!listResultIsOk && hasToShowFilters) {
            boolean filtersActive = FilterManager.getInstance().getTotalFilters() != 0;
            view.setFiltersVisibility(filtersActive);
        } else if (!listResultIsOk && !hasToShowFilters) {
            view.setFiltersVisibility(false);
        }
    }

    @Override
    public SearchTeiModel getTeiInfo(String teiUid) {
        return searchRepository.getTrackedEntityInfo(teiUid, selectedProgram, FilterManager.getInstance().getSortingItem());
    }

    @Override
    public EventUiComponentModel getEventInfo(String eventUid, String teiUid) {
        return eventToEventUiComponent.map(
                searchRepository.getEventInfo(eventUid),
                searchRepository.getTrackedEntityInfo(teiUid, selectedProgram, FilterManager.getInstance().getSortingItem())
        );
    }

    private void updateQueryData(RowAction data) {
        if (DhisTextUtils.Companion.isEmpty(data.getValue())
                || (data.getRequiresExactMatch() && data.getValue().equals("null_os_null"))) {
            queryData.remove(data.getId());
        } else {
            queryData.put(data.getId(), data.getValue());
        }
    }

    @Override
    public void setOpeningFilterToNone() {
        filterRepository.collapseAllFilters();
    }

    @Override
    public boolean selectedProgramMinNumberOfAttributesCheck() {
        if (selectedProgram == null) {
            return true;
        } else {
            int minAttributes = selectedProgram.minAttributesRequiredToSearch() != null ? selectedProgram.minAttributesRequiredToSearch() : 0;
            return minAttributes <= queryData.size();
        }
    }

    private boolean canSearchOnline() {
        return NetworkUtils.isOnline(view.getContext()) && selectedProgramMinNumberOfAttributesCheck();
    }
}
