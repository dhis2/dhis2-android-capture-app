package org.dhis2.usescases.searchTrackEntity;

import static android.app.Activity.RESULT_OK;
import static org.dhis2.commons.matomo.Actions.MAP_VISUALIZATION;
import static org.dhis2.commons.matomo.Actions.OPEN_ANALYTICS;
import static org.dhis2.commons.matomo.Actions.SYNC_TEI;
import static org.dhis2.commons.matomo.Categories.SEARCH;
import static org.dhis2.commons.matomo.Categories.TRACKER_LIST;
import static org.dhis2.commons.matomo.Labels.CLICK;
import static org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment.TEI_A_UID;
import static org.dhis2.utils.analytics.AnalyticsConstants.ADD_RELATIONSHIP;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_ENROLL;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_RELATIONSHIP;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.content.res.AppCompatResources;

import org.dhis2.R;
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker;
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener;
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.data.FilterRepository;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.dhis2.commons.orgunitselector.OUTreeFragment;
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope;
import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.commons.resources.ObjectStyleUtils;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.service.SyncStatusController;
import org.dhis2.maps.model.StageStyle;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.FeatureType;
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
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import kotlin.Unit;
import timber.log.Timber;

public class SearchTEPresenter implements SearchTEContractsModule.Presenter {

    private static final Program ALL_TE_TYPES = null;
    private final SearchRepository searchRepository;
    private final D2 d2;
    private final SchedulerProvider schedulerProvider;
    private final SearchTEContractsModule.View view;
    private final AnalyticsHelper analyticsHelper;
    private final BehaviorSubject<String> currentProgram;
    private final PreferenceProvider preferences;
    private final FilterRepository filterRepository;
    private final ResourceManager resourceManager;
    private Program selectedProgram;

    private final CompositeDisposable compositeDisposable;
    private final TrackedEntityType trackedEntity;

    private final String trackedEntityType;

    private final DisableHomeFiltersFromSettingsApp disableHomeFilters;
    private final MatomoAnalyticsController matomoAnalyticsController;
    private final SyncStatusController syncStatusController;

    private final ColorUtils colorUtils;

    public SearchTEPresenter(SearchTEContractsModule.View view,
                             D2 d2,
                             SearchRepository searchRepository,
                             SchedulerProvider schedulerProvider,
                             AnalyticsHelper analyticsHelper,
                             @Nullable String initialProgram,
                             @NonNull String teTypeUid,
                             PreferenceProvider preferenceProvider,
                             FilterRepository filterRepository,
                             DisableHomeFiltersFromSettingsApp disableHomeFilters,
                             MatomoAnalyticsController matomoAnalyticsController,
                             SyncStatusController syncStatusController,
                             ResourceManager resourceManager,
                             ColorUtils colorUtils) {
        this.view = view;
        this.preferences = preferenceProvider;
        this.searchRepository = searchRepository;
        this.d2 = d2;
        this.schedulerProvider = schedulerProvider;
        this.analyticsHelper = analyticsHelper;
        this.filterRepository = filterRepository;
        this.disableHomeFilters = disableHomeFilters;
        this.matomoAnalyticsController = matomoAnalyticsController;
        this.syncStatusController = syncStatusController;
        this.resourceManager = resourceManager;
        compositeDisposable = new CompositeDisposable();
        selectedProgram = initialProgram != null ? d2.programModule().programs().uid(initialProgram).blockingGet() : null;
        currentProgram = BehaviorSubject.createDefault(initialProgram != null ? initialProgram : "");
        this.trackedEntityType = teTypeUid;
        this.trackedEntity = searchRepository.getTrackedEntityType(trackedEntityType).blockingFirst();
        this.colorUtils = colorUtils;
    }

    //-----------------------------------
    //region LIFECYCLE

    @Override
    public void init() {
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
                            } else {
                                view.hideFilter();
                            }
                        }
                        , Timber::e
                )
        );

        compositeDisposable.add(
                searchRepository.programsWithRegistration(trackedEntityType)
                        .map(this::applySyncStatus)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(programs -> {
                                    Collections.sort(programs, (program1, program2) -> program1.getDisplayName().compareToIgnoreCase(program2.getDisplayName()));
                                    if (selectedProgram != null) {
                                        setProgram(selectedProgram);
                                    } else {
                                        setProgram(ALL_TE_TYPES);
                                    }
                                    view.setPrograms(programs);
                                }, Timber::d
                        ));

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
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
    }
    //endregion

    //------------------------------------------
    //region DATA

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
            String previousProgramUid = selectedProgram != null ? selectedProgram.uid() : "";
            String currentProgramUid = newProgramSelected.uid();
            if (isPreviousAndCurrentProgramTheSame(newProgramSelected,
                    previousProgramUid,
                    currentProgramUid))
                return;
        }

        boolean otherProgramSelected;
        if (newProgramSelected == null) {
            otherProgramSelected = selectedProgram != null;
        } else {
            otherProgramSelected = !newProgramSelected.equals(selectedProgram);
        }

        if (otherProgramSelected) {
            selectedProgram = newProgramSelected;
            view.clearList(newProgramSelected == null ? null : newProgramSelected.uid());
            preferences.removeValue(Preference.CURRENT_ORG_UNIT);
            searchRepository.setCurrentProgram(newProgramSelected != null ? newProgramSelected.uid() : null);
        }

        currentProgram.onNext(newProgramSelected != null ? newProgramSelected.uid() : "");
    }

    private boolean isPreviousAndCurrentProgramTheSame(Program programSelected, String previousProgramUid, String currentProgramUid) {
        return previousProgramUid != null && previousProgramUid.equals(currentProgramUid) ||
                programSelected == selectedProgram;
    }

    @Override
    public void onClearClick() {
        searchRepository.setCurrentProgram(selectedProgram != null ? selectedProgram.uid() : null);
        currentProgram.onNext(selectedProgram != null ? selectedProgram.uid() : "");
    }

    @Override
    public void onBackClick() {
        view.onBackClicked();
    }

    @Override
    public void onEnrollClick(HashMap<String, String> queryData) {
        if (selectedProgram != null)
            if (canCreateTei())
                enroll(selectedProgram.uid(), null, queryData);
            else
                view.displayMessage(view.getContext().getString(R.string.search_access_error));
        else
            view.displayMessage(view.getContext().getString(R.string.search_program_not_selected));
    }

    private boolean canCreateTei() {
        boolean programAccess = selectedProgram.access().data().write() != null && selectedProgram.access().data().write();
        boolean teTypeAccess = d2.trackedEntityModule().trackedEntityTypes().uid(
                Objects.requireNonNull(selectedProgram.trackedEntityType()).uid()
        ).blockingGet().access().data().write();
        return programAccess && teTypeAccess;
    }

    @Override
    public void enroll(String programUid, String uid, HashMap<String, String> queryData) {

        compositeDisposable.add(getOrgUnits()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        allOrgUnits -> {
                            if (allOrgUnits.size() > 1) {
                                new OUTreeFragment.Builder()
                                        .showAsDialog()
                                        .singleSelection()
                                        .onSelection(selectedOrgUnits -> {
                                            if (!selectedOrgUnits.isEmpty())
                                                enrollInOrgUnit(selectedOrgUnits.get(0).uid(), programUid, uid, queryData);
                                            return Unit.INSTANCE;
                                        })
                                        .orgUnitScope(new OrgUnitSelectorScope.ProgramCaptureScope(programUid))
                                        .build()
                                        .show(view.getAbstracContext().getSupportFragmentManager(), "OrgUnitEnrollment");
                            } else if (allOrgUnits.size() == 1)
                                enrollInOrgUnit(allOrgUnits.get(0).uid(), programUid, uid, queryData);
                        },
                        Timber::d
                )
        );
    }

    private void enrollInOrgUnit(String orgUnitUid, String programUid, String uid,  HashMap<String, String> queryData) {
        compositeDisposable.add(
                searchRepository.saveToEnroll(trackedEntity.uid(), orgUnitUid, programUid, uid, queryData, view.fromRelationshipTEI())
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
                        t -> {
                            if (t instanceof D2Error d2Error) {
                                switch (d2Error.errorCode()) {
                                    case OWNERSHIP_ACCESS_DENIED:
                                        view.showBreakTheGlass(teiUid, enrollmentUid);
                                        break;
                                    default:
                                        view.displayMessage(resourceManager.parseD2Error(t));
                                        break;
                                }
                            } else {
                                Timber.e(t);
                            }
                        },
                        () -> {
                            if (d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingExists()) {
                                if (teiHasEnrollmentInProgram(teiUid)) {
                                    openDashboard(teiUid, enrollmentUid);
                                } else if (canCreateTei()) {
                                    enroll(selectedProgram.uid(), teiUid, new HashMap<>());
                                }
                            } else {
                                view.couldNotDownload(trackedEntity.displayName());
                            }
                        })
        );
    }

    private boolean teiHasEnrollmentInProgram(String teiUid) {
        return !d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq(selectedProgram.uid())
                .blockingIsEmpty();
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
    public void onSyncIconClick(String teiUid) {
        matomoAnalyticsController.trackEvent(TRACKER_LIST, SYNC_TEI, CLICK);
        view.showSyncDialog(teiUid);
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
    public void getMapData() {
        FilterManager.getInstance().publishData();
    }


    @Override
    public Drawable getSymbolIcon() {
        TrackedEntityType teiType = d2.trackedEntityModule().trackedEntityTypes().withTrackedEntityTypeAttributes().uid(trackedEntityType).blockingGet();

        if (teiType.style() != null && teiType.style().icon() != null) {
            return
                    ObjectStyleUtils.getIconResource(view.getContext(), teiType.style().icon(), R.drawable.ic_default_icon, colorUtils);
        } else
            return AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_default_icon);
    }

    @Override
    public Drawable getEnrollmentSymbolIcon() {
        if (selectedProgram != null) {
            if (selectedProgram.style() != null && selectedProgram.style().icon() != null) {
                return ObjectStyleUtils.getIconResource(view.getContext(), selectedProgram.style().icon(), R.drawable.ic_default_outline, colorUtils);
            } else
                return AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_default_outline);
        }

        return null;
    }

    @Override
    public int getTEIColor() {
        TrackedEntityType teiType = d2.trackedEntityModule().trackedEntityTypes().withTrackedEntityTypeAttributes().uid(trackedEntityType).blockingGet();

        if (teiType.style() != null && teiType.style().color() != null) {
            return colorUtils.parseColor(Objects.requireNonNull(teiType.style().color()));
        } else
            return -1;
    }

    @Override
    public int getEnrollmentColor() {
        if (selectedProgram != null && selectedProgram.style() != null && selectedProgram.style().color() != null)
            return colorUtils.parseColor(Objects.requireNonNull(selectedProgram.style().color()));
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
                    color = colorUtils.parseColor(Objects.requireNonNull(stage.style().color()));
                } else {
                    color = -1;
                }
                if (stage.style() != null && stage.style().icon() != null) {
                    icon = ObjectStyleUtils.getIconResource(view.getContext(), stage.style().icon(), R.drawable.ic_clinical_f_outline, colorUtils);
                } else {
                    icon = AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_clinical_f_outline);
                }
                stagesStyleMap.put(stage.displayName(), new StageStyle(color, Objects.requireNonNull(icon)));
            }
        }
        return stagesStyleMap;
    }

    @Override
    public void deleteRelationship(String relationshipUid) {
        try {
            d2.relationshipModule().relationships().withItems().uid(relationshipUid).blockingDelete();
        } catch (D2Error error) {
            Timber.d(error);
        } finally {
            analyticsHelper.setEvent(DELETE_RELATIONSHIP, CLICK, DELETE_RELATIONSHIP);
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
    public void setOrgUnitFilters(List<OrganisationUnit> selectedOrgUnits) {
        FilterManager.getInstance().addOrgUnits(selectedOrgUnits);
    }

    @Override
    public void trackSearchAnalytics() {
        matomoAnalyticsController.trackEvent(SEARCH, OPEN_ANALYTICS, CLICK);
    }

    @Override
    public void trackSearchMapVisualization() {
        matomoAnalyticsController.trackEvent(SEARCH, MAP_VISUALIZATION, CLICK);
    }

    @Override
    public void setOpeningFilterToNone() {
        filterRepository.collapseAllFilters();
    }

    public List<ProgramSpinnerModel> applySyncStatus(List<Program> programs) {
        return programs.stream().map(program -> new ProgramSpinnerModel(
                program.uid(),
                program.displayName(),
                Objects.requireNonNull(
                        syncStatusController.observeDownloadProcess().getValue()
                ).isProgramDownloading(program.uid())
        )).collect(Collectors.toList());
    }
}
