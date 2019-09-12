package org.dhis2.usescases.teiDashboard.teiDataDetail;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;

import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 12/13/2017.
 */

public class TeiDataDetailPresenter implements TeiDataDetailContracts.Presenter {

    private final DashboardRepository dashboardRepository;
    private final CompositeDisposable disposable;
    private final EnrollmentStatusStore enrollmentStore;
    private TeiDataDetailContracts.View view;
    private FusedLocationProviderClient mFusedLocationClient;

    TeiDataDetailPresenter(DashboardRepository dashboardRepository, EnrollmentStatusStore enrollmentStatusStore) {
        this.dashboardRepository = dashboardRepository;
        this.enrollmentStore = enrollmentStatusStore;
        disposable = new CompositeDisposable();
    }

    @Override
    public void init(TeiDataDetailContracts.View view, String uid, String programUid, String enrollmentUid) {
        this.view = view;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(view.getContext());

        if (programUid != null) {
            disposable.add(Observable.zip(
                    dashboardRepository.getTrackedEntityInstance(uid),
                    dashboardRepository.getEnrollment(programUid, uid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, uid),
                    dashboardRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, uid),
                    dashboardRepository.getTeiOrgUnits(uid, programUid),
                    dashboardRepository.getTeiActivePrograms(uid, false),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            view::setData,
                            Timber::d)
            );

            disposable.add(
                    enrollmentStore.enrollmentStatus(enrollmentUid)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    view.handleStatus(),
                                    Timber::d)

            );


            disposable.add(view.reportCoordinatesChanged()
                    .filter(geometry -> geometry != null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(enrollmentStore.storeCoordinates(), Timber::e));

            disposable.add(view.teiCoordinatesChanged()
                    .filter(geometry -> geometry != null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(enrollmentStore.storeTeiCoordinates(), Timber::e));

            disposable.add(view.reportCoordinatesCleared()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(enrollmentStore.clearCoordinates(), Timber::e));

            disposable.add(view.teiCoordinatesCleared()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(enrollmentStore.clearTeiCoordinates(), Timber::e));


        } else {
            //TODO: PROGRAM NOT SELECTED
            disposable.add(Observable.zip(
                    dashboardRepository.getTrackedEntityInstance(uid),
                    dashboardRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, uid),
                    dashboardRepository.getTeiOrgUnits(uid, null),
                    dashboardRepository.getTeiActivePrograms(uid, false),
                    dashboardRepository.getTEIEnrollments(uid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(view::setData,
                            Timber::d)
            );
        }

        disposable.add(dashboardRepository.getAttributeImage(uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::showTeiImage,
                        Timber::e
                )
        );

    }

    @Override
    public void checkTeiCoordinates() {
        disposable.add(enrollmentStore.captureTeiCoordinates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view.renderTeiCoordinates(), Timber::e)
        );
    }

    @Override
    public void onBackPressed() {
        view.getAbstracContext().onBackPressed();
    }

    @Override
    public void onDeactivate(DashboardProgramModel dashboardProgramModel) {
        if (dashboardProgramModel.getCurrentProgram().access().data().write())
            disposable.add(enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.CANCELLED)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(result -> EnrollmentStatus.CANCELLED)
                    .subscribe(
                            view.handleStatus(),
                            Timber::d)
            );
        else
            view.displayMessage(null);

    }

    @Override
    public void onReOpen(DashboardProgramModel dashboardProgramModel) {
        if (dashboardProgramModel.getCurrentProgram().access().data().write())
            disposable.add(enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.ACTIVE)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(result -> EnrollmentStatus.ACTIVE)
                    .subscribe(
                            view.handleStatus(),
                            Timber::d)
            );
        else
            view.displayMessage(null);
    }

    @Override
    public void onComplete(DashboardProgramModel dashboardProgramModel) {
        if (dashboardProgramModel.getCurrentProgram().access().data().write())
            disposable.add(enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.COMPLETED)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(result -> EnrollmentStatus.COMPLETED)
                    .subscribe(
                            view.handleStatus(),
                            Timber::d)
            );
        else
            view.displayMessage(null);
    }

    @Override
    public void onActivate(DashboardProgramModel dashboardProgramModel) {
        if (dashboardProgramModel.getCurrentProgram().access().data().write())
            disposable.add(enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.ACTIVE)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(result -> EnrollmentStatus.ACTIVE)
                    .subscribe(
                            view.handleStatus(),
                            Timber::d)
            );
        else
            view.displayMessage(null);
    }

    @Override
    public void onDestroy() {
        disposable.clear();
    }


    @Override
    public void onIncidentDateClick(Date date) {
        view.showCustomIncidentCalendar(date);
    }

    @Override
    public void onEnrollmentDateClick(Date date) {
        view.showCustomEnrollmentCalendar(date);
    }

    @Override
    public void updateIncidentDate(Date date) {
        disposable.add(
                enrollmentStore.saveIncidentDate(DateUtils.databaseDateFormat().format(date))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                },
                                Timber::e
                        )
        );
    }

    @Override
    public void updateEnrollmentDate(Date date) {
        disposable.add(
                enrollmentStore.saveEnrollmentDate(DateUtils.databaseDateFormat().format(date))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                },
                                Timber::e
                        )
        );
    }
}
