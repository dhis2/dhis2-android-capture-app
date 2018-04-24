package com.dhis2.usescases.teiDashboard.teiDataDetail;

import android.annotation.SuppressLint;
import android.util.Log;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.DashboardRepository;

import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by frodriguez on 12/13/2017.
 */

public class TeiDataDetailPresenter implements TeiDataDetailContracts.Presenter {

    private final DashboardRepository dashboardRepository;
    private final MetadataRepository metadataRepository;
    private final CompositeDisposable disposable;
    private final AttrEntryStore dataEntryStore;
    private final EnrollmentStatusStore enrollmentStore;
    private TeiDataDetailContracts.View view;

    TeiDataDetailPresenter(DashboardRepository dashboardRepository, MetadataRepository metadataRepository, AttrEntryStore dataEntryStore, EnrollmentStatusStore enrollmentStatusStore) {
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
        this.dataEntryStore = dataEntryStore;
        this.enrollmentStore = enrollmentStatusStore;
        disposable = new CompositeDisposable();
    }

    @SuppressLint("CheckResult")
    @Override
    public void init(TeiDataDetailContracts.View view, String uid, String programUid) {
        this.view = view;

        if (programUid != null)
            disposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(uid),
                    dashboardRepository.getEnrollment(programUid, uid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, uid),
                    metadataRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, uid),
                    metadataRepository.getTeiOrgUnit(uid),
                    metadataRepository.getTeiActivePrograms(uid),
                    dashboardRepository.getRelationships(programUid, uid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            view::setData,
                            throwable -> Log.d("ERROR", throwable.getMessage()))
            );
        else {
            //TODO: NO SE HA SELECCIONADO PROGRAMA
            disposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(uid),
                    metadataRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, uid),
                    metadataRepository.getTeiOrgUnit(uid),
                    metadataRepository.getTeiActivePrograms(uid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(view::setData,
                            throwable -> Log.d("ERROR", throwable.getMessage()))
            );
        }
    }

    @Override
    public void onBackPressed() {
        view.getAbstracContext().onBackPressed();
    }

    @Override
    public void editData() {
        view.setDataEditable();
    }

    @Override
    public void saveData(ProgramTrackedEntityAttributeModel programAttr, String s) {
        disposable.add(dataEntryStore.save(programAttr.trackedEntityAttribute(), s)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> Log.d("SAVE", "Saved " + data),
                        Timber::d
                ));
    }

    @Override
    public void onButtonActionClick(DashboardProgramModel dashboardProgramModel) {
        if (dashboardProgramModel.getCurrentProgram().accessDataWrite()) {
            Flowable<Long> flowable = null;
            switch (dashboardProgramModel.getCurrentEnrollment().enrollmentStatus()) {
                case ACTIVE:
                    flowable = enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.COMPLETED);//TODO: SET STATUS TO COMPLETED
                    break;
                case COMPLETED:
                    flowable = enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.ACTIVE);//TODO: SET STATUS TO ACTIVE
                    break;
                case CANCELLED:
                    flowable = enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.ACTIVE);//TODO: SET STATUS TO ACTIVE
                    break;
            }

            disposable.add(flowable
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> view.getAbstracContext().recreate(),
                            Timber::d)
            );
        } else
            view.displayMessage(null);
    }

    @Override
    public void onDeactivate(DashboardProgramModel dashboardProgramModel) {
        if (dashboardProgramModel.getCurrentProgram().accessDataWrite())
            disposable.add(enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.CANCELLED)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> view.getAbstracContext().recreate(),
                            Timber::d)
            );
        else
            view.displayMessage(null);

    }

}
