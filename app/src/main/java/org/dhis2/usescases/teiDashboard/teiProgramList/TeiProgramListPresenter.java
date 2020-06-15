package org.dhis2.usescases.teiDashboard.teiProgramList;

import org.dhis2.R;
import org.dhis2.usescases.main.program.ProgramViewModel;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DESELECT_ENROLLMENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.ENROLL_FROM_LIST;

/**
 * QUADRAM. Created by Cristian on 06/03/2018.
 */

public class TeiProgramListPresenter implements TeiProgramListContract.Presenter {

    private TeiProgramListContract.View view;
    private final TeiProgramListContract.Interactor interactor;
    private String teiUid;

    TeiProgramListPresenter(TeiProgramListContract.Interactor interactor, String trackedEntityId) {
        this.interactor = interactor;
        this.teiUid = trackedEntityId;

    }

    @Override
    public void init(TeiProgramListContract.View view) {
        this.view = view;
        interactor.init(view, teiUid);
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onEnrollClick(ProgramViewModel program) {
        if (program.accessDataWrite()) {
            view.analyticsHelper().setEvent(ENROLL_FROM_LIST, CLICK, ENROLL_FROM_LIST);
            interactor.enroll(program.id(), teiUid);
        }
        else
            view.displayMessage(view.getContext().getString(R.string.search_access_error));
    }

    @Override
    public void onActiveEnrollClick(EnrollmentViewModel enrollmentModel) {
        view.changeCurrentProgram(enrollmentModel.programUid());
    }

    @Override
    public String getProgramColor(String programUid) {
        return interactor.getProgramColor(programUid);
    }

    @Override
    public void onUnselectEnrollment() {
        view.analyticsHelper().setEvent(DESELECT_ENROLLMENT, CLICK, DESELECT_ENROLLMENT);
        view.changeCurrentProgram(null);
    }

    @Override
    public void onDettach() {
        interactor.onDettach();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }
}
