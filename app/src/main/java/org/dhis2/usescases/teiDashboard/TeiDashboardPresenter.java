package org.dhis2.usescases.teiDashboard;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;

import org.dhis2.R;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.AuthorityException;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.Program;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_ENROLL;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_TEI;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardPresenter implements TeiDashboardContracts.Presenter {

    private final DashboardRepository dashboardRepository;
    private final SchedulerProvider schedulerProvider;
    private final AnalyticsHelper analyticsHelper;
    private TeiDashboardContracts.View view;

    private String teiUid;
    private String programUid;

    public CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardProgramModel;

    private MutableLiveData<DashboardProgramModel> dashboardProgramModelLiveData = new MutableLiveData<>();

    public TeiDashboardPresenter(TeiDashboardContracts.View view,  String teiUid, String programUid, DashboardRepository dashboardRepository, SchedulerProvider schedulerProvider, AnalyticsHelper analyticsHelper) {
        this.view = view;
        this.teiUid = teiUid;
        this.programUid = programUid;
        this.analyticsHelper = analyticsHelper;
        this.dashboardRepository = dashboardRepository;
        this.schedulerProvider = schedulerProvider;
        compositeDisposable = new CompositeDisposable();
    }

    @SuppressLint({"CheckResult"})
    @Override
    public void init() {
        if (programUid != null)
            compositeDisposable.add(Observable.zip(
                    dashboardRepository.getTrackedEntityInstance(teiUid),
                    dashboardRepository.getEnrollment(programUid, teiUid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid),
                    dashboardRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, teiUid),
                    dashboardRepository.getTeiOrgUnits(teiUid, programUid),
                    dashboardRepository.getTeiActivePrograms(teiUid, false),
                    DashboardProgramModel::new)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                                this.dashboardProgramModelLiveData.setValue(dashboardModel);
                                view.setData(dashboardProgramModel);
                            },
                            Timber::e
                    )
            );

        else {
            compositeDisposable.add(Observable.zip(
                    dashboardRepository.getTrackedEntityInstance(teiUid),
                    dashboardRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, teiUid),
                    dashboardRepository.getTeiOrgUnits(teiUid, null),
                    dashboardRepository.getTeiActivePrograms(teiUid, true),
                    dashboardRepository.getTEIEnrollments(teiUid),
                    DashboardProgramModel::new)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
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
    public void onEnrollmentSelectorClick() {
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teiUid);
        view.goToEnrollmentList(extras);
    }

    @Override
    public void setProgram(Program program) {
        this.programUid = program.uid();
        view.restoreAdapter(programUid);
        init();
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
    public String getProgramUid() {
        return programUid;
    }

    @Override
    public void deleteTei() {
        compositeDisposable.add(
                dashboardRepository.deleteTeiIfPossible()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                canDelete -> {
                                    if(canDelete){
                                        analyticsHelper.setEvent(DELETE_TEI, CLICK, DELETE_TEI);
                                        view.handleTEIdeletion();
                                    } else {
                                        view.authorityErrorMessage();
                                    }
                                },
                                Timber::e
                        )
        );
    }

    @Override
    public void deleteEnrollment() {
        compositeDisposable.add(
                dashboardRepository.deleteEnrollmentIfPossible(
                        dashboardProgramModel.getCurrentEnrollment().uid()
                )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                hasMoreEnrollments -> {
                                    analyticsHelper.setEvent(DELETE_ENROLL, CLICK, DELETE_ENROLL);
                                    view.handleEnrollmentDeletion(hasMoreEnrollments);
                                },
                                error -> {
                                    if (error instanceof AuthorityException)
                                        view.authorityErrorMessage();
                                    else
                                        Timber.e(error);
                                }
                        )
        );
    }

    @Override
    public void showDescription(String description) {
        view.showDescription(description);
    }

}