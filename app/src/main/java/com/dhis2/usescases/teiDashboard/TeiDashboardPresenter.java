package com.dhis2.usescases.teiDashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.qr.QRInterface;
import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import com.dhis2.usescases.teiDashboard.adapters.ScheduleAdapter;
import com.dhis2.usescases.teiDashboard.dashboardfragments.IndicatorsFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.NotesFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.ScheduleFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;
import com.dhis2.usescases.teiDashboard.eventDetail.EventDetailActivity;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import com.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailActivity;
import com.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListActivity;
import com.dhis2.utils.OnErrorHandler;

import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardPresenter implements TeiDashboardContracts.Presenter {

    private final DashboardRepository dashboardRepository;
    private final MetadataRepository metadataRepository;
    private final QRInterface qrInterface;
    private TeiDashboardContracts.View view;

    private String teUid;
    private String teType;
    private String programUid;
    private boolean programWritePermission;

    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardProgramModel;
    private TEIDataFragment teiDataFragment;
    private String eventUid;

    TeiDashboardPresenter(DashboardRepository dashboardRepository, MetadataRepository metadataRepository, QRInterface qrInterface) {
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
        this.qrInterface = qrInterface;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(TeiDashboardContracts.View view, String teiUid, String programUid) {
        this.view = view;
        this.teUid = teiUid;
        this.programUid = programUid;

        dashboardRepository.setDashboardDetails(teiUid, programUid);

        getData();
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @Override
    public void getData() {
        if (programUid != null)
            Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teUid),
                    dashboardRepository.getEnrollment(programUid, teUid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, teUid),
                    metadataRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, teUid),
                    metadataRepository.getTeiOrgUnit(teUid),
                    metadataRepository.getTeiActivePrograms(teUid),
                    dashboardRepository.getRelationships(programUid, teUid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            (dashboardProgramModel) -> {
                                this.dashboardProgramModel = dashboardProgramModel;
                                this.programWritePermission = dashboardProgramModel.getCurrentProgram().accessDataWrite();
                                this.teType = dashboardProgramModel.getTei().trackedEntityType();
                                view.setData(dashboardProgramModel);
                            },
                            throwable -> Log.d("ERROR", throwable.getMessage())
                    );

        else {
            //TODO: NO SE HA SELECCIONADO PROGRAMA
            Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teUid),
                    metadataRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, teUid),
                    metadataRepository.getTeiOrgUnit(teUid),
                    metadataRepository.getTeiActivePrograms(teUid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            view::setDataWithOutProgram,
                            throwable -> Log.d("ERROR", throwable.getMessage()));
        }
    }

    @Override
    public DashboardProgramModel getDashBoardData() {
        return dashboardProgramModel;
    }

    @Override
    public void getTEIEvents(TEIDataFragment teiFragment) {
        compositeDisposable.add(
                dashboardRepository.getTEIEnrollmentEvents(programUid, teUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                teiFragment.setEvents(),
                                OnErrorHandler.create()
                        )
        );
    }

    @Override
    public void areEventsCompleted(TEIDataFragment teiDataFragment) {
        compositeDisposable.add(
                dashboardRepository.getEnrollmentEventsWithDisplay(programUid, teUid)
                        .flatMap(events -> events.isEmpty() ? dashboardRepository.getTEIEnrollmentEvents(programUid, teUid) : Observable.empty())
                        .map(events -> Observable.fromIterable(events).all(event -> event.status() == EventStatus.COMPLETED))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                teiDataFragment.areEventsCompleted(),
                                Timber::d
                        )
        );
    }

    @Override
    public void displayGenerateEvent(TEIDataFragment teiDataFragment, String eventUid) {
        compositeDisposable.add(
                dashboardRepository.displayGenerateEvent(eventUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                teiDataFragment.displayGenerateEvent(),
                                OnErrorHandler.create())
        );
    }

    @Override
    public void generateEvent(String lastModifiedEventUid, Integer standardInterval) {
        compositeDisposable.add(
                dashboardRepository.generateNewEvent(lastModifiedEventUid, standardInterval)
                        .take(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> view.displayMessage(result), OnErrorHandler.create())
        );
    }

    @Override
    public void onShareClick(View mView) {
        PopupMenu menu = new PopupMenu(view.getContext(), mView);

        menu.getMenu().add(Menu.NONE, Menu.NONE, 0, "QR");
        menu.getMenu().add(Menu.NONE, Menu.NONE, 1, "SMS");

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getOrder()) {
                case 0:
                    qrInterface.teiQRs(teUid)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(view.showQR(), Timber::d);
                    return true;
                case 1:
                    view.displayMessage("This functionality is not ready yet.");
                    return true;
                default:
                    return true;

            }
        });

        menu.show();
    }

    @Override
    public void nextQr() {
        view.nextQR();
    }

    @Override
    public void onEnrollmentSelectorClick() {
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teUid);
        view.startActivity(TeiProgramListActivity.class, extras, false, false, null);
    }

    @Override
    public void onShareQRClick() {
        view.showQR();
    }

    @Override
    public void setProgram(ProgramModel program) {
        this.programUid = program.uid();
        getData();
    }

    @Override
    public void seeDetails(View sharedView, DashboardProgramModel dashboardProgramModel) {
        Fragment teiFragment = view.getAdapter().getItem(0);
        Intent intent = new Intent(view.getContext(), TeiDataDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teUid);
        extras.putString("PROGRAM_UID", programUid);
        if (dashboardProgramModel.getCurrentEnrollment() != null)
            extras.putString("ENROLLMENT_UID", dashboardProgramModel.getCurrentEnrollment().uid());
        intent.putExtras(extras);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "user_info");
        teiFragment.startActivityForResult(intent, TEIDataFragment.getDetailsRequestCode(), options.toBundle());
    }

    @Override
    public void onEventSelected(String uid, View sharedView) {
        Fragment teiFragment = view.getAdapter().getItem(0);
        Intent intent = new Intent(view.getContext(), EventDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString("EVENT_UID", uid);
        extras.putString("TOOLBAR_TITLE", view.getToolbarTitle());
        intent.putExtras(extras);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "shared_view");
        teiFragment.startActivityForResult(intent, TEIDataFragment.getEventRequestCode(), options.toBundle());
    }

    @Override
    public void onFollowUp(DashboardProgramModel dashboardProgramModel) {
        int success = dashboardRepository.setFollowUp(dashboardProgramModel.getCurrentEnrollment().uid(),
                dashboardProgramModel.getCurrentEnrollment().followUp() == null || !dashboardProgramModel.getCurrentEnrollment().followUp());
        if (success > 0) {
            view.showToast(!dashboardProgramModel.getCurrentEnrollment().followUp() ?
                    view.getContext().getString(R.string.follow_up_enabled) :
                    view.getContext().getString(R.string.follow_up_disabled));
            getData();
        }
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    public Observable<List<TrackedEntityAttributeValueModel>> getTEIMainAttributes(String teiUid) {
        return dashboardRepository.mainTrackedEntityAttributes(teiUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    @Override
    public void goToAddRelationship() {
        if (programWritePermission) {
            Fragment relationshipFragment = RelationshipFragment.getInstance();
            Intent intent = new Intent(view.getContext(), SearchTEActivity.class);
            Bundle extras = new Bundle();
            extras.putBoolean("FROM_RELATIONSHIP", true);
            extras.putString("TRACKED_ENTITY_UID", teType);
            extras.putString("PROGRAM_UID", programUid);
            intent.putExtras(extras);
            relationshipFragment.startActivityForResult(intent, RelationshipFragment.REQ_ADD_RELATIONSHIP);
        } else
            view.displayMessage("You don't have the required permission for this action");
    }

    @Override
    public void addRelationship(String trackEntityInstance_A, String relationshipType) {
        if (!trackEntityInstance_A.equals(teUid))
            dashboardRepository.saveRelationship(trackEntityInstance_A, teUid, relationshipType);
        else
            view.displayMessage("It's not possible to add this relationship");
    }

    @Override
    public void deleteRelationship(long relationshipId) {
        dashboardRepository.deleteRelationship(relationshipId);
    }

    @Override
    public void subscribeToRelationships(RelationshipFragment relationshipFragment) {
        compositeDisposable.add(
                dashboardRepository.getRelationships(programUid, teUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                relationshipFragment.setRelationships(),
                                Timber::d
                        )
        );
    }

    @Override
    public void subscribeToIndicators(IndicatorsFragment indicatorsFragment) {
        compositeDisposable.add(dashboardRepository.getIndicators(programUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        indicatorsFragment.swapIndicators(),
                        OnErrorHandler.create()
                )
        );
    }


    @Override
    public void subscribeToScheduleEvents(ScheduleFragment scheduleFragment) {
        compositeDisposable.add(
                scheduleFragment.filterProcessor()
                        .startWith(ScheduleAdapter.Filter.ALL)
                        .map(filter -> {
                            if (filter == ScheduleAdapter.Filter.SCHEDULE)
                                return EventStatus.SCHEDULE.name();
                            else if (filter == ScheduleAdapter.Filter.OVERDUE)
                                return EventStatus.SKIPPED.name();
                            else
                                return EventStatus.SCHEDULE.name() + "," + EventStatus.SKIPPED.name();
                        })
                        .flatMap(filter -> dashboardRepository.getScheduleEvents(programUid, teUid, filter))
                        .map(eventModels -> {
                            for (EventModel eventModel : eventModels) {
                                if (DateUtils.isToday(eventModel.dueDate().getTime())) { //If a event dueDate is Today, mark as Active
                                    dashboardRepository.updateState(eventModel, EventStatus.ACTIVE);
                                } else if (eventModel.dueDate().before(Calendar.getInstance().getTime()) && eventModel.status() != EventStatus.SKIPPED) { //If a event dueDate is before today and its status is not skipped, the event is skipped
                                    dashboardRepository.updateState(eventModel, EventStatus.SKIPPED);
                                }
                            }
                            return eventModels;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                scheduleFragment.swapEvents(),
                                Timber::d
                        )
        );
    }


    @Override
    public void setNoteProcessor(Flowable<Pair<String, Boolean>> noteProcessor) {
        compositeDisposable.add(noteProcessor
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dashboardRepository.handleNote(), OnErrorHandler.create()));
    }

    @Override
    public void subscribeToNotes(NotesFragment notesFragment) {
        compositeDisposable.add(dashboardRepository.getNotes(programUid, teUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        notesFragment.swapNotes(),
                        OnErrorHandler.create()
                )
        );
    }

    @Override
    public void openDashboard(String teiUid) {
        Intent intent = new Intent(view.getContext(), TeiDashboardMobileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", teiUid);
        bundle.putString("PROGRAM_UID", programUid);
        intent.putExtras(bundle);
        view.getAbstractActivity().startActivity(intent);
        //view.startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
    }

    @Override
    public void subscribeToMainAttr(String teiUid, TextView textView) {

    }

    @Override
    public void onBackPressed() {
        view.back();
    }

    @Override
    public String getTeUid() {
        return teUid;
    }

    @Override
    public String getProgramUid() {
        return programUid;
    }

    @Override
    public Boolean hasProgramWritePermission() {
        return programWritePermission;
    }

    @Override
    public void completeEnrollment(TEIDataFragment teiDataFragment) {
        if (programWritePermission) {
            Flowable<Long> flowable = null;
            EnrollmentStatus newStatus = EnrollmentStatus.COMPLETED;

            flowable = dashboardRepository.updateEnrollmentStatus(dashboardProgramModel.getCurrentEnrollment().uid(), newStatus);
            compositeDisposable.add(flowable
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(result -> newStatus)
                    .subscribe(
                            teiDataFragment.enrollmentCompleted(),
                            Timber::d)
            );
        } else
            view.displayMessage(null);
    }
}