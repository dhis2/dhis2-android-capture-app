package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;

import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.R;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.events.ScheduledEventActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.qrCodes.QrActivity;
import org.dhis2.usescases.sms.InputArguments;
import org.dhis2.usescases.sms.SmsSubmitActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.usescases.teiDashboard.eventDetail.EventDetailActivity;
import org.dhis2.usescases.teiDashboard.nfc_data.NfcDataWriteActivity;
import org.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailActivity;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.EventCreationType;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.utils.analytics.AnalyticsConstants.ACTIVE_FOLLOW_UP;
import static org.dhis2.utils.analytics.AnalyticsConstants.FOLLOW_UP;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHARE_TEI;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_NFC;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_QR;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_SHARE;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_SMS;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
class TEIDataPresenterImpl implements TEIDataContracts.Presenter {

    private final D2 d2;
    private final DashboardRepository dashboardRepository;
    private final SchedulerProvider schedulerProvider;
    private String programUid;
    private final String teiUid;
    private TEIDataContracts.View view;
    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardModel;

    public TEIDataPresenterImpl(D2 d2, DashboardRepository dashboardRepository,
                                String programUid, String teiUid, SchedulerProvider schedulerProvider) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.programUid = programUid;
        this.teiUid = teiUid;
        this.schedulerProvider = schedulerProvider;
    }

    @Override
    public void init(TEIDataContracts.View view) {
        this.view = view;
        this.compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(
                d2.trackedEntityModule().trackedEntityInstances.uid(teiUid).get()
                        .map(tei -> ExtensionsKt.profilePicturePath(tei, d2, programUid))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::showTeiImage,
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
                                /*TODO: CHECK HOW LONG IT TAKES FOR EVENTS WITH MANY RULES
                                if (eventModel.eventDate() != null) {
                                    RuleEffectResult effectResult = new RulesUtilsProviderImpl(new CodeGeneratorImpl()).evaluateEvent(eventModel.uid(),null);
                                    List<String> newMandatoryFields = effectResult.getMandatoryFields();
                                    List<ProgramStageDataElement> psDataElementList = d2.programModule().programStages().uid(eventModel.programStage())
                                            .withAllChildren().blockingGet().programStageDataElements();
                                    for (ProgramStageDataElement psDataElement : psDataElementList) {
                                        if (psDataElement.compulsory())
                                            newMandatoryFields.add(psDataElement.dataElement().uid());
                                    }
                                    boolean missingMandatories = !newMandatoryFields.isEmpty() && d2.trackedEntityModule().trackedEntityDataValues
                                            .byEvent().eq(eventModel.uid())
                                            .byDataElement().in(newMandatoryFields)
                                            .blockingCount() < newMandatoryFields.size();
                                    if (missingMandatories)
                                        Timber.tag("MISSING FIELDS").d("THERE ARE MISSING MANDATORY FIELDS IN EVENT %s", eventModel.uid());
                                }*/
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
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(catCombo -> {
                                    for (ProgramStage programStage : dashboardModel.getProgramStages()) {
                                        if (event.programStage().equals(programStage.uid()))
                                            view.showCatComboDialog(event.uid(), catCombo);
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
        view.analyticsHelper().setEvent(ACTIVE_FOLLOW_UP, Boolean.toString(followup), FOLLOW_UP);
        view.showToast(followup ?
                view.getContext().getString(R.string.follow_up_enabled) :
                view.getContext().getString(R.string.follow_up_disabled));

        view.switchFollowUp(followup);

    }

    @Override
    public void onShareClick(View mView) {
        PopupMenu menu = new PopupMenu(view.getContext(), mView);

        menu.getMenu().add(Menu.NONE, Menu.NONE, 0, "QR");
        if (mView.getResources().getBoolean(R.bool.sms_enabled)) {
            menu.getMenu().add(Menu.NONE, Menu.NONE, 2, "SMS");
        }
        menu.getMenu().add(Menu.NONE, Menu.NONE, 2, "NFC");

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getOrder()) {
                case 0:
                    view.analyticsHelper().setEvent(TYPE_SHARE, TYPE_QR, SHARE_TEI);
                    Intent intent = new Intent(view.getContext(), QrActivity.class);
                    intent.putExtra("TEI_UID", teiUid);
                    view.showQR(intent);
                    return true;
                case 1:
                    view.analyticsHelper().setEvent(TYPE_SHARE, TYPE_SMS, SHARE_TEI);
                    Activity activity = view.getAbstractActivity();
                    Intent i = new Intent(activity, SmsSubmitActivity.class);
                    Bundle args = new Bundle();
                    InputArguments.setEnrollmentData(args, dashboardModel.getCurrentEnrollment().uid());
                    i.putExtras(args);
                    activity.startActivity(i);
                    return true;
                case 2:
                    view.analyticsHelper().setEvent(TYPE_SHARE, TYPE_NFC, SHARE_TEI);
                    Intent intentNfc = new Intent(view.getContext(), NfcDataWriteActivity.class);
                    intentNfc.putExtra("TEI_UID", teiUid);
                    view.showQR(intentNfc);
                    return true;
                default:
                    return true;
            }
        });

        menu.show();
    }

    @Override
    public void seeDetails(View sharedView, DashboardProgramModel dashboardProgramModel) {
        Intent intent = new Intent(view.getContext(), TeiDataDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teiUid);
        extras.putString("PROGRAM_UID", programUid);
        if (dashboardProgramModel.getCurrentEnrollment() != null)
            extras.putString("ENROLLMENT_UID", dashboardProgramModel.getCurrentEnrollment().uid());
        intent.putExtras(extras);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "user_info");
//        view.seeDetails(intent, options.toBundle());

        view.seeDetails(EnrollmentActivity.Companion.getIntent(view.getContext(),
                dashboardProgramModel.getCurrentEnrollment().uid(),
                dashboardProgramModel.getCurrentProgram().uid(),
                EnrollmentActivity.EnrollmentMode.CHECK), options.toBundle());
    }

    @Override
    public void onScheduleSelected(String uid, View sharedView) {
        String title = String.format("%s %s - %s",
                dashboardModel.getTrackedEntityAttributeValueBySortOrder(1) != null ? dashboardModel.getTrackedEntityAttributeValueBySortOrder(1) : "",
                dashboardModel.getTrackedEntityAttributeValueBySortOrder(2) != null ? dashboardModel.getTrackedEntityAttributeValueBySortOrder(2) : "",
                dashboardModel.getCurrentProgram() != null ? dashboardModel.getCurrentProgram().displayName() : view.getContext().getString(R.string.dashboard_overview)
        );
        Intent intent = ScheduledEventActivity.Companion.getIntent(view.getContext(),uid);
        /*Intent intent = new Intent(view.getContext(), EventDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString("EVENT_UID", uid);
        extras.putString("TOOLBAR_TITLE", title);
        extras.putString("TEI_UID", teiUid);
        intent.putExtras(extras);*/
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
