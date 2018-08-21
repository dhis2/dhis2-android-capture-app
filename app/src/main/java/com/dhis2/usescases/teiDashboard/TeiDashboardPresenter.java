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
import com.dhis2.utils.Constants;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardPresenter implements TeiDashboardContracts.Presenter {

    private final DashboardRepository dashboardRepository;
    private final MetadataRepository metadataRepository;
    private final D2 d2;
    private TeiDashboardContracts.View view;

    private String teUid;
    private String teType;
    private String programUid;
    private boolean programWritePermission;

    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardProgramModel;
    private TEIDataFragment teiDataFragment;
    private String eventUid;

    TeiDashboardPresenter(D2 d2, DashboardRepository dashboardRepository, MetadataRepository metadataRepository) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
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

    @SuppressLint({"CheckResult"})
    @Override
    public void getData() {
        if (programUid != null)
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teUid),
                    dashboardRepository.getEnrollment(programUid, teUid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, teUid),
                    metadataRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, teUid),
                    metadataRepository.getTeiOrgUnit(teUid),
                    metadataRepository.getTeiActivePrograms(teUid),
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
                    )
            );

        else {
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teUid),
                    metadataRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, teUid),
                    metadataRepository.getTeiOrgUnit(teUid),
                    metadataRepository.getTeiActivePrograms(teUid),
                    metadataRepository.getTEIEnrollments(teUid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            view::setDataWithOutProgram,
                            throwable -> Log.d("ERROR", throwable.getMessage()))
            );
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
                                Timber::d
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
                                Timber::d
                        )
        );
    }

    @Override
    public void generateEvent(String lastModifiedEventUid, Integer standardInterval) {
        compositeDisposable.add(
                dashboardRepository.generateNewEvent(lastModifiedEventUid, standardInterval)
                        .take(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> view.displayMessage(result),
                                Timber::d
                        )
        );
    }

    @Override
    public void generateEventFromDate(String lastModifiedEventUid, Calendar chosenDate) {
        compositeDisposable.add(
                dashboardRepository.generateNewEventFromDate(lastModifiedEventUid, chosenDate)
                        .take(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> view.displayMessage(result),
                                Timber::d
                        )
        );
    }

    @Override
    public void onShareClick(View mView) {
        PopupMenu menu = new PopupMenu(view.getContext(), mView);

        menu.getMenu().add(Menu.NONE, Menu.NONE, 0, "QR");
        //menu.getMenu().add(Menu.NONE, Menu.NONE, 1, "SMS");

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getOrder()) {
                case 0:
                    view.showQR();
                    return true;
                case 1:
                    view.displayMessage(view.getContext().getString(R.string.feature_unavaible));
                    return true;
                default:
                    return true;
            }
        });

        menu.show();
    }

    @Override
    public void onEnrollmentSelectorClick() {
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teUid);
        view.goToEnrollmentList(extras);
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
        if (teiFragment != null && teiFragment.getContext() != null && teiFragment.isAdded()) {
            Intent intent = new Intent(teiFragment.getContext(), EventDetailActivity.class);
            Bundle extras = new Bundle();
            extras.putString("EVENT_UID", uid);
            extras.putString("TOOLBAR_TITLE", view.getToolbarTitle());
            intent.putExtras(extras);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "shared_view");
            teiFragment.startActivityForResult(intent, TEIDataFragment.getEventRequestCode(), options.toBundle());
        }
    }

    @Override
    public void onFollowUp(DashboardProgramModel dashboardProgramModel) {
        int success = dashboardRepository.setFollowUp(dashboardProgramModel.getCurrentEnrollment().program(), dashboardProgramModel.getCurrentEnrollment().uid(),
                dashboardProgramModel.getCurrentEnrollment().followUp() == null || !dashboardProgramModel.getCurrentEnrollment().followUp());
        if (success > 0) {

            boolean followUp = false;
            if (dashboardProgramModel.getCurrentEnrollment() != null && dashboardProgramModel.getCurrentEnrollment().followUp() != null){
                followUp = dashboardProgramModel.getCurrentEnrollment().followUp();
            }

            view.showToast(!followUp ?
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
            relationshipFragment.startActivityForResult(intent, Constants.REQ_ADD_RELATIONSHIP);
        } else
            view.displayMessage(view.getContext().getString(R.string.search_access_error));
    }

    @Override
    public void addRelationship(String trackEntityInstance_A, String trackEntityInstance_B, String relationshipType) {
        if (trackEntityInstance_A != null) {
            if (!trackEntityInstance_A.equals(teUid))
//                dashboardRepository.saveRelationship(trackEntityInstance_A, teUid, relationshipType);
                d2.relationshipModule().relationship.createTEIRelationship(relationshipType, trackEntityInstance_A, teUid);
            else
                view.displayMessage(view.getContext().getString(R.string.add_relationship_error));
        } else {
            if (!trackEntityInstance_B.equals(teUid))
//                dashboardRepository.saveRelationship(teUid, trackEntityInstance_B, relationshipType);
                d2.relationshipModule().relationship.createTEIRelationship(relationshipType, teUid, trackEntityInstance_B);
            else
                view.displayMessage(view.getContext().getString(R.string.add_relationship_error));
        }
    }

    @Override
    public void deleteRelationship(Relationship relationship) {
//        dashboardRepository.deleteRelationship(relationshipModel); TODO: HOW TO DELETE NOW?
        view.showInfoDialog("Info", view.getContext().getString(R.string.feature_unavaible));
    }

    @Override
    public void subscribeToRelationships(RelationshipFragment relationshipFragment) {
        compositeDisposable.add(
                Observable.just(d2.relationshipModule().relationship.getRelationshipsByTEI(teUid))
                        .flatMapIterable(list -> list)
                        .map(relationship -> {
                            RelationshipType relationshipType = null;
                            for (RelationshipType type : d2.relationshipModule().relationshipType.getAll())
                                if (type.uid().equals(relationship.relationshipType()))
                                    relationshipType = type;
                            return Pair.create(relationship, relationshipType); //TODO: relationshipType is never going to be null. right?
                        }).toList()
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
                .map(indicators ->
                        Observable.fromIterable(indicators)
                                .filter(indicator -> indicator.displayInForm())
                                .map(indicator -> {
                                    String indcatorValue = d2.evaluateProgramIndicator(
                                            dashboardProgramModel.getCurrentEnrollment().uid(),
                                            null,
                                            indicator.uid());
                                    return Pair.create(indicator, indcatorValue == null ? "" : indcatorValue);
                                })
                                .filter(pair -> !pair.val1().isEmpty())
                                .flatMap(pair -> dashboardRepository.getLegendColorForIndicator(pair.val0(), pair.val1()))
                                .toList()
                )
                .flatMap(Single::toFlowable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(data -> {
                    Log.d("INDICATOR SIZE", "IS" + data.size());
                    return data;
                })
                .subscribe(
                        indicatorsFragment.swapIndicators(),
                        Timber::d
                )
        );
    }

    @Override
    public void onDescriptionClick(String description) {
        view.showDescription(description);
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
                .subscribe(
                        dashboardRepository.handleNote(),
                        Timber::d
                ));
    }

    @Override
    public void subscribeToNotes(NotesFragment notesFragment) {
        compositeDisposable.add(dashboardRepository.getNotes(programUid, teUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        notesFragment.swapNotes(),
                        Timber::d
                )
        );
    }

    @Override
    public void openDashboard(String teiUid) {
        Intent intent = new Intent(view.getContext(), TeiDashboardMobileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", teiUid);
        bundle.putString("PROGRAM_UID", null);
        intent.putExtras(bundle);
        view.getAbstractActivity().startActivity(intent);
    }

    @Override
    public void subscribeToRelationshipLabel(RelationshipModel relationship, TextView textView) {

       /*
        compositeDisposable.add(
                metadataRepository.getRelationshipTypeList()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMapIterable(data -> data)
                        .filter(relationshipTypeModel -> relationshipTypeModel.uid().equals(relationship.relationshipType()))
                        .map(item -> {
                            if (teUid.equals(relationship.trackedEntityInstanceA()))
                                return item.bIsToA();
                            else
                                return item.aIsToB();
                        })
                        .subscribe(
                                textView::setText,
                                t -> view.displayMessage(view.getContext().getString(R.string.relationship_label_error)))

        );*/
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
                            Timber::d
                    )
            );
        } else
            view.displayMessage(null);
    }

    @Override
    public void showDescription(String description) {
        view.showDescription(description);
    }
}