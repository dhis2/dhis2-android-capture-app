package org.dhis2.usescases.teiDashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment;
import org.dhis2.usescases.teiDashboard.eventDetail.EventDetailActivity;
import org.dhis2.utils.EventCreationType;
import org.hisp.dhis.android.core.program.ProgramModel;

import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
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
    private TeiDashboardContracts.View view;

    private String teUid;
    private String programUid;

    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardProgramModel;

    public TeiDashboardPresenter(DashboardRepository dashboardRepository, MetadataRepository metadataRepository) {
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

    private void getData() {
        if (programUid != null)
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teUid),
                    dashboardRepository.getEnrollment(programUid, teUid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, teUid),
                    metadataRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, teUid),
                    metadataRepository.getTeiOrgUnit(teUid, programUid),
                    metadataRepository.getTeiActivePrograms(teUid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
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
                    metadataRepository.getTeiActivePrograms(teUid),
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
                                view.setData(dashboardProgramModel);
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
    public void onEventSelected(String uid, View sharedView) {
        Fragment teiFragment = TEIDataFragment.getInstance();
        if (teiFragment != null && teiFragment.getContext() != null && teiFragment.isAdded()) {
            Intent intent = new Intent(teiFragment.getContext(), EventInitialActivity.class);
            intent.putExtras(EventInitialActivity.getBundle(
                    programUid, uid, EventCreationType.DEFAULT.name(), teUid, null, null, null, dashboardProgramModel.getCurrentEnrollment().uid(), 0, dashboardProgramModel.getCurrentEnrollment().enrollmentStatus()
            ));
            teiFragment.startActivityForResult(intent, TEIDataFragment.getEventRequestCode(), null);
        }
    }

    @Override
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
    public void showDescription(String description) {
        view.showDescription(description);
    }

    @Override
    public void changeCatOption(String eventUid, String catOptionComboUid) {
        metadataRepository.saveCatOption(eventUid, catOptionComboUid);
    }
}