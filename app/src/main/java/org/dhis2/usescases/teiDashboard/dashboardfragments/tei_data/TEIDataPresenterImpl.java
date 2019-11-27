package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import android.content.Intent;
import android.view.View;

import androidx.core.app.ActivityOptionsCompat;

import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.R;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.events.ScheduledEventActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.qrCodes.QrActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.ACTIVE_FOLLOW_UP;
import static org.dhis2.utils.analytics.AnalyticsConstants.FOLLOW_UP;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHARE_TEI;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_QR;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_SHARE;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
class TEIDataPresenterImpl implements TEIDataContracts.Presenter {

    private final D2 d2;
    private final DashboardRepository dashboardRepository;
    private final SchedulerProvider schedulerProvider;
    private final AnalyticsHelper analyticsHelper;
    private String programUid;
    private final String teiUid;
    private TEIDataContracts.View view;
    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardModel;

    public TEIDataPresenterImpl(D2 d2, DashboardRepository dashboardRepository,
                                String programUid, String teiUid, SchedulerProvider schedulerProvider,
                                AnalyticsHelper analyticsHelper) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.programUid = programUid;
        this.teiUid = teiUid;
        this.schedulerProvider = schedulerProvider;
        this.analyticsHelper = analyticsHelper;
    }

    @Override
    public void init(TEIDataContracts.View view) {
        this.view = view;
        this.compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(
                d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).get()
                        .map(tei -> {
                                    String defaultIcon = d2.trackedEntityModule().trackedEntityTypes().uid(tei.trackedEntityType()).blockingGet().style().icon();
                                    return Pair.create(
                                            ExtensionsKt.profilePicturePath(tei, d2, programUid),
                                            defaultIcon != null ? defaultIcon : ""
                                    );
                                }
                        )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                fileNameAndDefault -> view.showTeiImage(
                                        fileNameAndDefault.val0(),
                                        fileNameAndDefault.val1()
                                ),
                                Timber::e
                        )
        );

    }


    @Override
    public void getTEIEvents() {
        compositeDisposable.add(
                dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid)
                        .map(eventModels -> {
                            for (Event eventModel : eventModels) {
                                if (eventModel.status() == EventStatus.SCHEDULE && eventModel.dueDate() != null && eventModel.dueDate().before(DateUtils.getInstance().getToday())) { //If a schedule event dueDate is before today the event is skipped
                                    dashboardRepository.updateState(eventModel, EventStatus.SKIPPED);
                                }
                            }
                            return eventModels;
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view.setEvents(),
                                Timber::d
                        )
        );
    }

    @Override
    public void getCatComboOptions(Event event) {
        compositeDisposable.add(
                dashboardRepository.catComboForProgram(event.program())
                        .flatMap(categoryCombo -> dashboardRepository.catOptionCombos(categoryCombo.uid()),
                                Pair::create
                        )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(categoryComboListPair -> {
                                    for (ProgramStage programStage : dashboardModel.getProgramStages()) {
                                        if (event.programStage().equals(programStage.uid()))
                                            view.showCatComboDialog(event.uid(), categoryComboListPair.val0(), categoryComboListPair.val1());
                                    }
                                },
                                Timber::e));
    }

    @Override
    public void setDefaultCatOptCombToEvent(String eventUid) {
        dashboardRepository.setDefaultCatOptCombToEvent(eventUid);
    }

    @Override
    public void changeCatOption(String eventUid, String catOptionComboUid) {
        dashboardRepository.saveCatOption(eventUid, catOptionComboUid);
    }

    @Override
    public void areEventsCompleted() {
        compositeDisposable.add(
                dashboardRepository.getEnrollmentEventsWithDisplay(programUid, teiUid)
                        .flatMap(events -> events.isEmpty() ? dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid) : Observable.just(events))
                        .map(events -> Observable.fromIterable(events).all(event -> event.status() == EventStatus.COMPLETED))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view.areEventsCompleted(),
                                Timber::d
                        )
        );
    }

    @Override
    public void displayGenerateEvent(String eventUid) {
        compositeDisposable.add(
                dashboardRepository.displayGenerateEvent(eventUid)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view.displayGenerateEvent(),
                                Timber::d
                        )
        );
    }

    @Override
    public void completeEnrollment() {
        if (d2.programModule().programs().uid(programUid).blockingGet().access().data().write()) {
            Flowable<Long> flowable;
            EnrollmentStatus newStatus = EnrollmentStatus.COMPLETED;

            flowable = dashboardRepository.updateEnrollmentStatus(dashboardModel.getCurrentEnrollment().uid(), newStatus);
            compositeDisposable.add(flowable
                    .subscribeOn(schedulerProvider.computation())
                    .observeOn(schedulerProvider.ui())
                    .map(result -> newStatus)
                    .subscribe(
                            view.enrollmentCompleted(),
                            Timber::d
                    )
            );
        } else
            view.displayMessage(null);
    }

    @Override
    public void onFollowUp(DashboardProgramModel dashboardProgramModel) {
        boolean followup = dashboardRepository.setFollowUp(dashboardProgramModel.getCurrentEnrollment().uid());
        analyticsHelper.setEvent(ACTIVE_FOLLOW_UP, Boolean.toString(followup), FOLLOW_UP);
        view.showToast(followup ?
                view.getContext().getString(R.string.follow_up_enabled) :
                view.getContext().getString(R.string.follow_up_disabled));

        view.switchFollowUp(followup);

    }

    @Override
    public void onShareClick(View mView) {
        analyticsHelper.setEvent(TYPE_SHARE, TYPE_QR, SHARE_TEI);
        Intent intent = new Intent(view.getContext(), QrActivity.class);
        intent.putExtra("TEI_UID", teiUid);
        view.showQR(intent);
    }

    @Override
    public void seeDetails(View sharedView, DashboardProgramModel dashboardProgramModel) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "user_info");
        view.seeDetails(EnrollmentActivity.Companion.getIntent(view.getContext(),
                dashboardProgramModel.getCurrentEnrollment().uid(),
                dashboardProgramModel.getCurrentProgram().uid(),
                EnrollmentActivity.EnrollmentMode.CHECK), options.toBundle());
    }

    @Override
    public void onScheduleSelected(String uid, View sharedView) {
        Intent intent = ScheduledEventActivity.Companion.getIntent(view.getContext(), uid);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "shared_view");
        view.openEventDetails(intent, options.toBundle());
    }

    @Override
    public void onEventSelected(String uid, EventStatus eventStatus, View sharedView) {
        if (eventStatus == EventStatus.ACTIVE || eventStatus == EventStatus.COMPLETED) {
            Intent intent = new Intent(view.getContext(), EventCaptureActivity.class);
            intent.putExtras(EventCaptureActivity.getActivityBundle(uid, programUid));
            view.openEventCapture(intent);
        } else {
            Event event = d2.eventModule().events().uid(uid).blockingGet();
            Intent intent = new Intent(view.getContext(), EventInitialActivity.class);
            intent.putExtras(EventInitialActivity.getBundle(
                    programUid, uid, EventCreationType.DEFAULT.name(), teiUid, null, event.organisationUnit(), event.programStage(), dashboardModel.getCurrentEnrollment().uid(), 0, dashboardModel.getCurrentEnrollment().status()
            ));
            view.openEventInitial(intent);
        }
    }

    @Override
    public void setDashboardProgram(DashboardProgramModel dashboardModel) {
        this.dashboardModel = dashboardModel;
        this.programUid = dashboardModel.getCurrentProgram().uid();
    }

    @Override
    public void setProgram(Program program) {
        this.programUid = program.uid();
        view.restoreAdapter(programUid);
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
    public void showDescription(String description) {
        view.showDescription(description);
    }
}
