package org.dhis2.usescases.teiDashboard;

import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.AuthorityException;
import org.dhis2.utils.Constants;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.program.Program;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_ENROLL;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_TEI;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardPresenter implements TeiDashboardContracts.Presenter {

    private final DashboardRepository dashboardRepository;
    private final SchedulerProvider schedulerProvider;
    private final AnalyticsHelper analyticsHelper;
    private final PreferenceProvider preferenceProvider;
    private TeiDashboardContracts.View view;

    private String teiUid;
    public String programUid;

    public CompositeDisposable compositeDisposable;
    public DashboardProgramModel dashboardProgramModel;
    private PublishProcessor<Unit> notesCounterProcessor;


    public TeiDashboardPresenter(TeiDashboardContracts.View view, String teiUid, String programUid, DashboardRepository dashboardRepository, SchedulerProvider schedulerProvider, AnalyticsHelper analyticsHelper, PreferenceProvider preferenceProvider) {
        this.view = view;
        this.teiUid = teiUid;
        this.programUid = programUid;
        this.analyticsHelper = analyticsHelper;
        this.dashboardRepository = dashboardRepository;
        this.schedulerProvider = schedulerProvider;
        this.preferenceProvider = preferenceProvider;
        compositeDisposable = new CompositeDisposable();
        notesCounterProcessor = PublishProcessor.create();
    }

    @Override
    public void init() {
        if (programUid != null)
            compositeDisposable.add(Observable.zip(
                    dashboardRepository.getTrackedEntityInstance(teiUid),
                    dashboardRepository.getEnrollment(programUid, teiUid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid),
                    dashboardRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, teiUid),
                    dashboardRepository.getTeiOrgUnits(teiUid, programUid),
                    dashboardRepository.getTeiActivePrograms(teiUid, false),
                    DashboardProgramModel::new)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                                view.setData(dashboardModel);
                            },
                            Timber::e
                    )
            );

        else {
            compositeDisposable.add(Observable.zip(
                    dashboardRepository.getTrackedEntityInstance(teiUid),
                    dashboardRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, teiUid),
                    dashboardRepository.getTeiOrgUnits(teiUid, null),
                    dashboardRepository.getTeiActivePrograms(teiUid, true),
                    dashboardRepository.getTEIEnrollments(teiUid),
                    DashboardProgramModel::new)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                                view.setDataWithOutProgram(dashboardProgramModel);
                            },
                            Timber::e)
            );
        }

        compositeDisposable.add(
                FilterManager.getInstance().asFlowable()
                        .startWith(FilterManager.getInstance())
                .map(FilterManager::getTotalFilters)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        totalFilters-> view.updateTotalFilters(totalFilters),
                        Timber::e
                )
        );
    }

    @Override
    public void onEnrollmentSelectorClick() {
        view.goToEnrollmentList();
    }

    @Override
    public void setProgram(Program program) {
        this.programUid = program.uid();
        view.restoreAdapter(programUid);
        init();
    }

    @Override
    public void deleteTei() {
        compositeDisposable.add(
                dashboardRepository.deleteTeiIfPossible()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                canDelete -> {
                                    if (canDelete) {
                                        analyticsHelper.setEvent(DELETE_TEI, CLICK, DELETE_TEI);
                                        view.handleTEIdeletion();
                                    } else {
                                        view.authorityErrorMessage();
                                    }
                                },
                                Timber::e
                        )
        );
    }

    @Override
    public void deleteEnrollment() {
        compositeDisposable.add(
                dashboardRepository.deleteEnrollmentIfPossible(
                        dashboardProgramModel.getCurrentEnrollment().uid()
                )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                hasMoreEnrollments -> {
                                    analyticsHelper.setEvent(DELETE_ENROLL, CLICK, DELETE_ENROLL);
                                    view.handleEnrollmentDeletion(hasMoreEnrollments);
                                },
                                error -> {
                                    if (error instanceof AuthorityException)
                                        view.authorityErrorMessage();
                                    else
                                        Timber.e(error);
                                }
                        )
        );
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void onBackPressed() {
        view.back();
    }

    @Override
    public String getProgramUid() {
        return programUid;
    }

    @Override
    public void showDescription(String description) {
        view.showDescription(description);
    }

    @Override
    public void initNoteCounter() {
        if (!notesCounterProcessor.hasSubscribers()) {
            compositeDisposable.add(
                    notesCounterProcessor.startWith(new Unit())
                            .flatMapSingle(unit ->
                                    dashboardRepository.getNoteCount())
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    numberOfNotes ->
                                            view.updateNoteBadge(numberOfNotes),
                                    Timber::e
                            )
            );
        } else {
            notesCounterProcessor.onNext(new Unit());
        }
    }

    @Override
    public void refreshTabCounters() {
        initNoteCounter();
    }

    @Override
    public int getProgramTheme(int appTheme) {
        return preferenceProvider.getInt(Constants.PROGRAM_THEME, preferenceProvider.getInt(Constants.THEME, appTheme));
    }

    @Override
    public void prefSaveCurrentProgram(String programUid) {
        preferenceProvider.setValue(Constants.PREVIOUS_DASHBOARD_PROGRAM, programUid);
    }

    @Override
    public String getPreviousDashboard() {
        return preferenceProvider.getString(Constants.PREVIOUS_DASHBOARD_PROGRAM, null);
    }

    @Override
    public void saveProgramTheme(int programTheme) {
        preferenceProvider.setValue(Constants.PROGRAM_THEME, programTheme);
    }

    @Override
    public void removeProgramTheme() {
        preferenceProvider.removeValue(Constants.PROGRAM_THEME);
    }

    @Override
    public Boolean getProgramGrouping() {
        if (programUid != null) {
            return preferenceProvider.programHasGrouping(programUid);
        }else{
            return false;
        }
    }

    @Override
    public void showHideFilters() {
        view.setFiltersLayoutState();
    }
}