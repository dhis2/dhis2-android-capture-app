package org.dhis2.usescases.teiDashboard;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.sharedPreferences.SharePreferencesProvider;
import org.dhis2.utils.AuthorityException;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.ProgramModel;

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
    private final SharePreferencesProvider provider;
    private TeiDashboardContracts.View view;

    private String teUid;
    private String programUid;
    private boolean programWritePermission;

    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardProgramModel;

    private MutableLiveData<DashboardProgramModel> dashboardProgramModelLiveData = new MutableLiveData<>();

    TeiDashboardPresenter(D2 d2, DashboardRepository dashboardRepository, MetadataRepository metadataRepository, RuleEngineRepository formRepository, SharePreferencesProvider provider) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
        compositeDisposable = new CompositeDisposable();
        this.provider = provider;
    }

    @Override
    public SharePreferencesProvider callPreference() {
        return provider;
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
    public String getProgramUid() {
        return programUid;
    }

    @Override
    public void deteleteTei() {
        compositeDisposable.add(
                canDeleteTEI()
                        .flatMap(canDelete -> {
                            if (canDelete)
                                return Single.fromCallable(() -> {
                                    d2.trackedEntityModule().trackedEntityInstances.uid(teUid)
                                            .delete();
                                    return true;
                                });
                            else
                                return Single.error(new AuthorityException(view.getContext().getString(R.string.delete_authority_error)));
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                canDelete -> view.handleTEIdeletion(),
                                error -> {
                                    if (error instanceof AuthorityException)
                                        view.displayMessage(error.getMessage());
                                    else
                                        Timber.e(error);
                                }
                        )
        );
    }

    @Override
    public void deleteEnrollment() {
        compositeDisposable.add(
                canDeleteEnrollment()
                        .flatMap(canDelete ->
                        {
                            if (canDelete)
                                return Single.fromCallable(() -> {
                                    EnrollmentObjectRepository enrollmentObjectRepository = d2.enrollmentModule().enrollments.uid(dashboardProgramModel.getCurrentEnrollment().uid());
                                    enrollmentObjectRepository.setStatus(enrollmentObjectRepository.get().status());
                                    enrollmentObjectRepository.delete();
                                    return !d2.enrollmentModule().enrollments.byTrackedEntityInstance().eq(teUid)
                                            .byState().notIn(State.TO_DELETE)
                                            .byStatus().eq(EnrollmentStatus.ACTIVE).get().isEmpty();
                                });
                            else
                                return Single.error(new AuthorityException(view.getContext().getString(R.string.delete_authority_error)));
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                hasMoreEnrollments -> view.handleEnrollmentDeletion(hasMoreEnrollments),
                                error -> {
                                    if (error instanceof AuthorityException)
                                        view.displayMessage(error.getMessage());
                                    else
                                        Timber.e(error);
                                }
                        )
        );
    }

    private Single<Boolean> canDeleteTEI() {
        return Single.defer(() -> Single.fromCallable(() -> {
                    boolean local = d2.trackedEntityModule().trackedEntityInstances.uid(
                            teUid).get().state() == State.TO_POST;
                    boolean hasAuthority = d2.userModule().authorities
                            .byName().eq("F_TEI_CASCADE_DELETE").one().exists();
                    return local || hasAuthority;
                }
        ));
    }

    private Single<Boolean> canDeleteEnrollment() {
        return Single.defer(() -> Single.fromCallable(() -> {
                    boolean local = d2.enrollmentModule().enrollments.uid(
                            dashboardProgramModel.getCurrentEnrollment().uid()).get().state() == State.TO_POST;
                    boolean hasAuthority = d2.userModule().authorities
                            .byName().eq("F_ENROLLMENT_CASCADE_DELETE").one().exists();
                    return local || hasAuthority;
                }
        ));
    }

    @Override
    public void showDescription(String description) {
        view.showDescription(description);
    }

}