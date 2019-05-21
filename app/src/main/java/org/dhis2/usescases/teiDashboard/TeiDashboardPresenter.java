package org.dhis2.usescases.teiDashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.TEIDataFragment;
import org.dhis2.usescases.teiDashboard.eventDetail.EventDetailActivity;
import org.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailActivity;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.EventCreationType;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.Calendar;
import java.util.List;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Flowable;
import io.reactivex.Observable;
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
    private final RuleEngineRepository ruleRepository;
    private TeiDashboardContracts.View view;

    private String teUid;
    private String teType;
    private String programUid;
    private boolean programWritePermission;

    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardProgramModel;

    private MutableLiveData<DashboardProgramModel> dashboardProgramModelLiveData = new MutableLiveData<>();

    TeiDashboardPresenter(D2 d2, DashboardRepository dashboardRepository, MetadataRepository metadataRepository, RuleEngineRepository formRepository) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
        this.ruleRepository = formRepository;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public LiveData<DashboardProgramModel> observeDashboardModel() {
        return dashboardProgramModelLiveData;
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
                    metadataRepository.getTeiOrgUnit(teUid, programUid),
                    metadataRepository.getTeiActivePrograms(teUid, false),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                                this.dashboardProgramModelLiveData.setValue(dashboardModel);
                                this.programWritePermission = dashboardProgramModel.getCurrentProgram().accessDataWrite();
                                this.teType = dashboardProgramModel.getTei().trackedEntityType();
                                view.setData(dashboardProgramModel);
                            },
                            Timber::e
                    )
            );

        else {
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teUid),
                    metadataRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, teUid),
                    metadataRepository.getTeiOrgUnit(teUid),
                    metadataRepository.getTeiActivePrograms(teUid, true),
                    metadataRepository.getTEIEnrollments(teUid),
                    DashboardProgramModel::new)
                    .flatMap(dashboardProgramModel1 -> metadataRepository.getObjectStylesForPrograms(dashboardProgramModel1.getEnrollmentProgramModels())
                            .map(stringObjectStyleMap -> {
                                dashboardProgramModel1.setProgramsObjectStyles(stringObjectStyleMap);
                                return dashboardProgramModel1;
                            }))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                                this.teType = dashboardProgramModel.getTei().trackedEntityType();
                                view.setDataWithOutProgram(dashboardProgramModel);
                            },
                            Timber::e)
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
                                teiFragment.setEvents(),
                                Timber::d
                        )
        );
    }


    @Override
    public void areEventsCompleted(TEIDataFragment teiDataFragment) {
        compositeDisposable.add(
                dashboardRepository.getEnrollmentEventsWithDisplay(programUid, teUid)
                        .flatMap(events -> events.isEmpty() ? dashboardRepository.getTEIEnrollmentEvents(programUid, teUid) : Observable.just(events))
                        .map(events -> Observable.fromIterable(events).all(event -> event.status() == EventStatus.COMPLETED))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                teiDataFragment.areEventsCompleted(),
                                Timber::d
                        )
        );
    }

 /*   @Override
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
    }*/

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


 /*   @Override
    public void onShareClick(View mView) {
        PopupMenu menu = new PopupMenu(view.getContext(), mView);

        menu.getMenu().add(Menu.NONE, Menu.NONE, 0, "QR");
        //menu.getMenu().add(Menu.NONE, Menu.NONE, 1, "SMS"); TODO: When SMS is ready, reactivate option

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
    }*/

    @Override
    public void onEnrollmentSelectorClick() {
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teUid);
        view.goToEnrollmentList(extras);
    }

   /* @Override
    public void onShareQRClick() {
        view.showQR();
    }*/

    @Override
    public void setProgram(ProgramModel program) {
        this.programUid = program.uid();
        view.restoreAdapter(programUid);
        getData();
    }

    /*@Override
    public void seeDetails(View sharedView, DashboardProgramModel dashboardProgramModel) {
        Fragment teiFragment = TEIDataFragment.getInstance();
        Intent intent = new Intent(view.getContext(), TeiDataDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teUid);
        extras.putString("PROGRAM_UID", programUid);
        if (dashboardProgramModel.getCurrentEnrollment() != null)
            extras.putString("ENROLLMENT_UID", dashboardProgramModel.getCurrentEnrollment().uid());
        intent.putExtras(extras);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "user_info");
        teiFragment.startActivityForResult(intent, TEIDataFragment.getDetailsRequestCode(), options.toBundle());
    }*/

   /* @Override
    public void onEventSelected(String uid, View sharedView) {
        Fragment teiFragment = TEIDataFragment.getInstance();
        if (teiFragment != null && teiFragment.getContext() != null && teiFragment.isAdded()) {
            Intent intent = new Intent(teiFragment.getContext(), EventInitialActivity.class);
            intent.putExtras(EventInitialActivity.getBundle(
                    programUid, uid, EventCreationType.DEFAULT.name(), teUid, null, null, null, dashboardProgramModel.getCurrentEnrollment().uid(), 0, dashboardProgramModel.getCurrentEnrollment().enrollmentStatus()
            ));
            teiFragment.startActivityForResult(intent, TEIDataFragment.getEventRequestCode(), null);
        }
    }*/

    /*@Override
    public void onScheduleSelected(String uid, View sharedView) {
        Fragment teiFragment = TEIDataFragment.getInstance();
        if (teiFragment != null && teiFragment.getContext() != null && teiFragment.isAdded()) {
            Intent intent = new Intent(teiFragment.getContext(), EventDetailActivity.class);
            Bundle extras = new Bundle();
            extras.putString("EVENT_UID", uid);
            extras.putString("TOOLBAR_TITLE", view.getToolbarTitle());
            extras.putString("TEI_UID", teUid);
            intent.putExtras(extras);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "shared_view");
            teiFragment.startActivityForResult(intent, TEIDataFragment.getEventRequestCode(), options.toBundle());
        }
    }*/

    /*@Override
    public void onFollowUp(DashboardProgramModel dashboardProgramModel) {
        boolean followup = dashboardRepository.setFollowUp(dashboardProgramModel.getCurrentEnrollment().uid());


        view.showToast(followup ?
                view.getContext().getString(R.string.follow_up_enabled) :
                view.getContext().getString(R.string.follow_up_disabled));

        TEIDataFragment.getInstance().switchFollowUp(followup);


    }*/

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

   /* @Override
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
    }*/

    @Override
    public void showDescription(String description) {
        view.showDescription(description);
    }

    /*public void getCatComboOptions(EventModel event) {
        compositeDisposable.add(
                dashboardRepository.catComboForProgram(event.program())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(catCombo -> {
                                    for (ProgramStageModel programStage : dashboardProgramModel.getProgramStages()) {
                                        if (event.programStage().equals(programStage.uid()))
                                            view.showCatComboDialog(event.uid(), catCombo);
                                    }
                                },
                                Timber::e));
    }*/

   /* @Override
    public void changeCatOption(String eventUid, String catOptionComboUid) {
        metadataRepository.saveCatOption(eventUid, catOptionComboUid);
    }*/

   /* @Override
    public void setDefaultCatOptCombToEvent(String eventUid) {
        dashboardRepository.setDefaultCatOptCombToEvent(eventUid);
    }*/

}