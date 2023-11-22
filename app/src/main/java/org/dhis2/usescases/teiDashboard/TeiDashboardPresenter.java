package org.dhis2.usescases.teiDashboard;

import static androidx.core.content.ContextCompat.getString;

import com.google.gson.reflect.TypeToken;

import org.dhis2.R;
import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.utils.AuthorityException;
import org.dhis2.commons.Constants;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.Program;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

import static org.dhis2.commons.matomo.Actions.OPEN_NOTES;
import static org.dhis2.commons.matomo.Actions.OPEN_RELATIONSHIPS;
import static org.dhis2.utils.analytics.AnalyticsConstants.ACTIVE_FOLLOW_UP;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_ENROLL;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_TEI;
import static org.dhis2.commons.matomo.Actions.OPEN_ANALYTICS;
import static org.dhis2.commons.matomo.Categories.DASHBOARD;
import static org.dhis2.utils.analytics.AnalyticsConstants.FOLLOW_UP;

public class TeiDashboardPresenter implements TeiDashboardContracts.Presenter {

    private final DashboardRepository dashboardRepository;
    private final SchedulerProvider schedulerProvider;
    private final AnalyticsHelper analyticsHelper;
    private final PreferenceProvider preferenceProvider;
    private final TeiDashboardContracts.View view;

    private String teiUid;
    public String programUid;

    public CompositeDisposable compositeDisposable;
    public DashboardProgramModel dashboardProgramModel;
    private PublishProcessor<Unit> notesCounterProcessor;
    private MatomoAnalyticsController matomoAnalyticsController;

    public TeiDashboardPresenter(
            TeiDashboardContracts.View view,
            String teiUid, String programUid,
            DashboardRepository dashboardRepository,
            SchedulerProvider schedulerProvider,
            AnalyticsHelper analyticsHelper,
            PreferenceProvider preferenceProvider,
            MatomoAnalyticsController matomoAnalyticsController
    ) {
        this.view = view;
        this.teiUid = teiUid;
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
    public void init() {
        if (programUid != null)
            compositeDisposable.add(Observable.zip(
                    dashboardRepository.getTrackedEntityInstance(teiUid),
                    dashboardRepository.getEnrollment(),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid),
                    dashboardRepository.getAttributesMap(programUid, teiUid),
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
    public void prefSaveCurrentProgram(String programUid) {
        preferenceProvider.setValue(Constants.PREVIOUS_DASHBOARD_PROGRAM, programUid);
    }

    @Override
    public Boolean getProgramGrouping() {
        if (programUid != null) {
            return getGrouping().containsKey(programUid) ? getGrouping().get(programUid) : true;
        } else {
            return false;
        }
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
                            if (statusCode == StatusChangeResultCode.CHANGED) {
                                view.updateStatus();
                            } else {
                                view.displayStatusError(statusCode);
                            }
                        }, Timber::e)
        );
    }

    private Map<String, Boolean> getGrouping() {
        TypeToken<HashMap<String, Boolean>> typeToken =
                new TypeToken<HashMap<String, Boolean>>() {
                };
        return preferenceProvider.getObjectFromJson(
                Preference.GROUPING,
                typeToken,
                new HashMap<>());
    }
}
