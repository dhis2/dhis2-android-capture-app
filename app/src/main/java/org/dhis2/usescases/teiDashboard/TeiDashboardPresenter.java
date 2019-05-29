package org.dhis2.usescases.teiDashboard;

import android.annotation.SuppressLint;
import android.os.Bundle;

import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.TEIDataFragment;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
    private TeiDashboardContracts.View view;

    private String teUid;
    private String programUid;
    private boolean programWritePermission;

    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardProgramModel;

    private MutableLiveData<DashboardProgramModel> dashboardProgramModelLiveData = new MutableLiveData<>();

    TeiDashboardPresenter(D2 d2, DashboardRepository dashboardRepository, MetadataRepository metadataRepository, RuleEngineRepository formRepository) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
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
                    metadataRepository.getTeiOrgUnits(teUid, programUid),
                    metadataRepository.getTeiActivePrograms(teUid, false),
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
                                this.dashboardProgramModelLiveData.setValue(dashboardModel);
                                if (dashboardProgramModel.getCurrentProgram() != null)
                                    this.programWritePermission = dashboardProgramModel.getCurrentProgram().accessDataWrite();
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
                    metadataRepository.getTeiOrgUnits(teUid),
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
    public void onEnrollmentSelectorClick() {
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teUid);
        view.goToEnrollmentList(extras);
    }

    @Override
    public void setProgram(ProgramModel program) {
        this.programUid = program.uid();
        view.restoreAdapter(programUid);
        getData();
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
    public void showDescription(String description) {
        view.showDescription(description);
    }

}