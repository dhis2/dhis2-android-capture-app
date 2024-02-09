package org.dhis2.usescases.teiDashboard;

import static org.dhis2.commons.matomo.Actions.OPEN_ANALYTICS;
import static org.dhis2.commons.matomo.Actions.OPEN_NOTES;
import static org.dhis2.commons.matomo.Actions.OPEN_RELATIONSHIPS;
import static org.dhis2.commons.matomo.Categories.DASHBOARD;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_TEI;

import org.dhis2.commons.Constants;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.Program;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

public class TeiDashboardPresenter implements TeiDashboardContracts.Presenter {

    private final DashboardRepository dashboardRepository;
    private final SchedulerProvider schedulerProvider;
    private final AnalyticsHelper analyticsHelper;
    private final PreferenceProvider preferenceProvider;
    private final TeiDashboardContracts.View view;

    public String programUid;

    public CompositeDisposable compositeDisposable;
    private PublishProcessor<Unit> notesCounterProcessor;
    private MatomoAnalyticsController matomoAnalyticsController;

    public TeiDashboardPresenter(
            TeiDashboardContracts.View view,
            String programUid,
            DashboardRepository dashboardRepository,
            SchedulerProvider schedulerProvider,
            AnalyticsHelper analyticsHelper,
            PreferenceProvider preferenceProvider,
            MatomoAnalyticsController matomoAnalyticsController
    ) {
        this.view = view;
        this.programUid = programUid;
        this.analyticsHelper = analyticsHelper;
        this.dashboardRepository = dashboardRepository;
        this.schedulerProvider = schedulerProvider;
        this.preferenceProvider = preferenceProvider;
        this.matomoAnalyticsController = matomoAnalyticsController;
        compositeDisposable = new CompositeDisposable();
        notesCounterProcessor = PublishProcessor.create();
    }

    @Override
    public String getTEType() {
        return dashboardRepository.getTETypeName();
    }

    @Override
    public void trackDashboardAnalytics() {
        matomoAnalyticsController.trackEvent(DASHBOARD, OPEN_ANALYTICS, CLICK);
    }

    @Override
    public void trackDashboardRelationships() {
        matomoAnalyticsController.trackEvent(DASHBOARD, OPEN_RELATIONSHIPS, CLICK);
    }

    @Override
    public void trackDashboardNotes() {
        matomoAnalyticsController.trackEvent(DASHBOARD, OPEN_NOTES, CLICK);
    }

    @Override
    public Boolean checkIfTEICanBeDeleted() {
        return dashboardRepository.checkIfDeleteTeiIsPossible();
    }

    @Override
    public Boolean checkIfEnrollmentCanBeDeleted(String enrollmentUid) {
        return dashboardRepository.checkIfDeleteEnrollmentIsPossible(enrollmentUid);
    }

    @Override
    public void onEnrollmentSelectorClick() {
        view.goToEnrollmentList();
    }

    @Override
    public void setProgram(Program program) {
        this.programUid = program.uid();
        view.restoreAdapter(programUid);
    }

    @Override
    public void deleteTei() {
        compositeDisposable.add(
                dashboardRepository.deleteTei()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                canDelete -> {
                                    if (canDelete) {
                                        analyticsHelper.setEvent(DELETE_TEI, CLICK, DELETE_TEI);
                                        view.handleTeiDeletion();
                                    } else {
                                        view.authorityErrorMessage();
                                    }
                                },
                                Timber::e
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
    public void prefSaveCurrentProgram(String programUid) {
        preferenceProvider.setValue(Constants.PREVIOUS_DASHBOARD_PROGRAM, programUid);
    }

    @Override
    public void handleShowHideFilters(boolean showFilters) {
        if (showFilters) {
            view.hideTabsAndDisableSwipe();
        } else {
            view.showTabsAndEnableSwipe();
        }
    }

    @Override
    public EnrollmentStatus getEnrollmentStatus(String enrollmentUid) {
        return dashboardRepository.getEnrollmentStatus(enrollmentUid);
    }

    @Override
    public void updateEnrollmentStatus(String enrollmentUid, EnrollmentStatus status) {
        compositeDisposable.add(
                dashboardRepository.updateEnrollmentStatus(enrollmentUid, status)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(statusCode -> {
                            if (statusCode != StatusChangeResultCode.CHANGED) {
                                view.displayStatusError(statusCode);
                            }
                        }, Timber::e)
        );
    }
}
