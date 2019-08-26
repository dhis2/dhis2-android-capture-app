package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;

import org.dhis2.R;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.sharedPreferences.SharePreferencesProvider;
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
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
class TEIDataPresenterImpl implements TEIDataContracts.Presenter {

    private final D2 d2;
    private final DashboardRepository dashboardRepository;
    private String programUid;
    private final String teiUid;
    private final MetadataRepository metadataRepository;
    private TEIDataContracts.View view;
    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardModel;
    private SharePreferencesProvider provider;

    public TEIDataPresenterImpl(D2 d2, DashboardRepository dashboardRepository, MetadataRepository metadataRepository,
                                String programUid, String teiUid, SharePreferencesProvider provider) {
        this.d2 = d2;
        this.metadataRepository = metadataRepository;
        this.dashboardRepository = dashboardRepository;
        this.programUid = programUid;
        this.teiUid = teiUid;
        this.provider = provider;
    }

    @Override
    public void init(TEIDataContracts.View view) {
        this.view = view;
        view.setPreference(provider);
        this.compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(
                Observable.fromCallable(() -> {

                    Iterator<TrackedEntityAttribute> iterator = d2.trackedEntityModule().trackedEntityAttributes.byValueType().eq(ValueType.IMAGE).get().iterator();
                    List<String> attrUids = new ArrayList<>();
                    while (iterator.hasNext())
                        attrUids.add(iterator.next().uid());

                    return d2.trackedEntityModule().trackedEntityAttributeValues
                            .byTrackedEntityInstance().eq(teiUid)
                            .byTrackedEntityAttribute().in(attrUids)
                            .one().get();
                }).map(attrValue -> teiUid + "_" + attrValue.trackedEntityAttribute() + ".png")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
                            for (EventModel eventModel : eventModels) {
                                if (eventModel.status() == EventStatus.SCHEDULE && eventModel.dueDate() != null && eventModel.dueDate().before(DateUtils.getInstance().getToday())) { //If a schedule event dueDate is before today the event is skipped
                                    dashboardRepository.updateState(eventModel, EventStatus.SKIPPED);
                                }
                            }
                            return eventModels;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.setEvents(),
                                Timber::d
                        )
        );
    }

    @Override
    public void getCatComboOptions(EventModel event) {
        compositeDisposable.add(
                dashboardRepository.catComboForProgram(event.program())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(catCombo -> {
                                    for (ProgramStageModel programStage : dashboardModel.getProgramStages()) {
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
        metadataRepository.saveCatOption(eventUid, catOptionComboUid);
    }

    @Override
    public void areEventsCompleted() {
        compositeDisposable.add(
                dashboardRepository.getEnrollmentEventsWithDisplay(programUid, teiUid)
                        .flatMap(events -> events.isEmpty() ? dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid) : Observable.just(events))
                        .map(events -> Observable.fromIterable(events).all(event -> event.status() == EventStatus.COMPLETED))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.displayGenerateEvent(),
                                Timber::d
                        )
        );
    }

    @Override
    public void completeEnrollment() {
        if (d2.programModule().programs.uid(programUid).withAllChildren().get().access().data().write()) {
            Flowable<Long> flowable;
            EnrollmentStatus newStatus = EnrollmentStatus.COMPLETED;

            flowable = dashboardRepository.updateEnrollmentStatus(dashboardModel.getCurrentEnrollment().uid(), newStatus);
            compositeDisposable.add(flowable
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
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
                    Intent intent = new Intent(view.getContext(), QrActivity.class);
                    intent.putExtra("TEI_UID", teiUid);
                    view.showQR(intent);
                    return true;
                case 1:
                    Activity activity = view.getAbstractActivity();
                    Intent i = new Intent(activity, SmsSubmitActivity.class);
                    Bundle args = new Bundle();
                    InputArguments.setEnrollmentData(args, dashboardModel.getCurrentEnrollment().uid());
                    i.putExtras(args);
                    activity.startActivity(i);
                    return true;
                case 2:
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
        view.seeDetails(intent, options.toBundle());
    }

    @Override
    public void onScheduleSelected(String uid, View sharedView) {
        String title = String.format("%s %s - %s",
                dashboardModel.getTrackedEntityAttributeValueBySortOrder(1) != null ? dashboardModel.getTrackedEntityAttributeValueBySortOrder(1) : "",
                dashboardModel.getTrackedEntityAttributeValueBySortOrder(2) != null ? dashboardModel.getTrackedEntityAttributeValueBySortOrder(2) : "",
                dashboardModel.getCurrentProgram() != null ? dashboardModel.getCurrentProgram().displayName() : view.getContext().getString(R.string.dashboard_overview)
        );

        Intent intent = new Intent(view.getContext(), EventDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString("EVENT_UID", uid);
        extras.putString("TOOLBAR_TITLE", title);
        extras.putString("TEI_UID", teiUid);
        intent.putExtras(extras);
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
            Event event = d2.eventModule().events.uid(uid).get();
            Intent intent = new Intent(view.getContext(), EventInitialActivity.class);
            intent.putExtras(EventInitialActivity.getBundle(
                    programUid, uid, EventCreationType.DEFAULT.name(), teiUid, null, event.organisationUnit(), event.programStage(), dashboardModel.getCurrentEnrollment().uid(), 0, dashboardModel.getCurrentEnrollment().enrollmentStatus()
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
    public void setProgram(ProgramModel program) {
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
