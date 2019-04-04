package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import org.dhis2.R;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailActivity;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

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

public class TEIDataPresenterImpl implements TEIDataPresenter {

    private final DashboardRepository dashboardRepository;
    private final MetadataRepository metadataRepository;
    private TeiDashboardContracts.View view;

    private String teiUid;
    private String programUid;
    private boolean programWritePermission;

    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardProgramModel;

    private MutableLiveData<DashboardProgramModel> dashboardProgramModelLiveData = new MutableLiveData<>();

    public TEIDataPresenterImpl(DashboardRepository dashboardRepository,
                                MetadataRepository metadataRepository,
                                String teiUid, String programUid) {
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
        this.teiUid = teiUid;
        this.programUid = programUid;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public LiveData<DashboardProgramModel> observeDashboardModel() {
        return dashboardProgramModelLiveData;
    }

    @Override
    public String getTeiUid() {
        return teiUid;
    }

    @Override
    public void init(TeiDashboardContracts.View view) {
        this.view = view;
        dashboardRepository.setDashboardDetails(teiUid, programUid);
        getData();
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @SuppressLint({"CheckResult"})
    @Override
    public void getData() {
        if (programUid != null)
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teiUid),
                    dashboardRepository.getEnrollment(programUid, teiUid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid),
                    metadataRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, teiUid),
                    metadataRepository.getTeiOrgUnit(teiUid, programUid),
                    metadataRepository.getTeiActivePrograms(teiUid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                                this.dashboardProgramModelLiveData.setValue(dashboardModel);
                                this.programWritePermission = dashboardProgramModel.getCurrentProgram().accessDataWrite();
                                view.setData(dashboardProgramModel);
                            },
                            Timber::e
                    )
            );

        else {
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teiUid),
                    metadataRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, teiUid),
                    metadataRepository.getTeiOrgUnit(teiUid),
                    metadataRepository.getTeiActivePrograms(teiUid),
                    metadataRepository.getTEIEnrollments(teiUid),
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
                                view.setData(dashboardProgramModel);
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
                                teiFragment.setEvents(),
                                Timber::d
                        )
        );
    }


    @Override
    public void areEventsCompleted(TEIDataFragment teiDataFragment) {
        compositeDisposable.add(
                dashboardRepository.getEnrollmentEventsWithDisplay(programUid, teiUid)
                        .flatMap(events -> events.isEmpty() ? dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid) : Observable.just(events))
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
    }

    @Override
    public void seeDetails(View sharedView, DashboardProgramModel dashboardProgramModel) {
        Fragment teiFragment = TEIDataFragment.getInstance();
        Intent intent = new Intent(view.getContext(), TeiDataDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teiUid);
        extras.putString("PROGRAM_UID", programUid);
        if (dashboardProgramModel.getCurrentEnrollment() != null)
            extras.putString("ENROLLMENT_UID", dashboardProgramModel.getCurrentEnrollment().uid());
        intent.putExtras(extras);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "user_info");
        teiFragment.startActivityForResult(intent, TEIDataFragment.getDetailsRequestCode(), options.toBundle());
    }

    @Override
    public void onFollowUp(DashboardProgramModel dashboardProgramModel) {
        boolean followup = dashboardRepository.setFollowUp(dashboardProgramModel.getCurrentEnrollment().uid());


        view.showToast(followup ?
                view.getContext().getString(R.string.follow_up_enabled) :
                view.getContext().getString(R.string.follow_up_disabled));

        TEIDataFragment.getInstance().switchFollowUp(followup);


    }

    public Observable<List<TrackedEntityAttributeValueModel>> getTEIMainAttributes(String teiUid) {
        return dashboardRepository.mainTrackedEntityAttributes(teiUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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

    public void getCatComboOptions(EventModel event) {
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

       /* compositeDisposable.add(
                Observable.zip(
                        metadataRepository.getCategoryComboOptions(dashboardProgramModel.getCurrentProgram().categoryCombo()),
                        metadataRepository.getCategoryFromCategoryCombo(dashboardProgramModel.getCurrentProgram().categoryCombo()),
                        Pair::create
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                                    for (ProgramStageModel programStage : dashboardProgramModel.getProgramStages()) {
                                        if (event.programStage().equals(programStage.uid()))
                                            view.showCatComboDialog(event.uid(), pair.val1().displayName(), pair.val0(), programStage.displayName());
                                    }
                                },
                                Timber::e));*/
    }

    @Override
    public void setDefaultCatOptCombToEvent(String eventUid) {
        dashboardRepository.setDefaultCatOptCombToEvent(eventUid);
    }
}