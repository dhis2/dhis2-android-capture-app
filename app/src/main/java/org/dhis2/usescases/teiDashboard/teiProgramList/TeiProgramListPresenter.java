package org.dhis2.usescases.teiDashboard.teiProgramList;

import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.usescases.main.program.ProgramViewModel;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.enrollment.EnrollmentService;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DESELECT_ENROLLMENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.ENROLL_FROM_LIST;

public class TeiProgramListPresenter implements TeiProgramListContract.Presenter {

    private final PreferenceProvider preferences;
    private final AnalyticsHelper analytics;
    private final EnrollmentService enrollmentService;
    private final TeiProgramListContract.View view;
    private final TeiProgramListContract.Interactor interactor;
    private String teiUid;

    TeiProgramListPresenter(TeiProgramListContract.View view,
                            TeiProgramListContract.Interactor interactor,
                            String trackedEntityId,
                            PreferenceProvider preferenceProvider,
                            AnalyticsHelper analyticsHelper,
                            EnrollmentService enrollmentService) {
        this.view = view;
        this.interactor = interactor;
        this.teiUid = trackedEntityId;
        this.preferences = preferenceProvider;
        this.analytics = analyticsHelper;
        this.enrollmentService = enrollmentService;
    }

    @Override
    public void init() {
        interactor.init(view, teiUid);
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onEnrollClick(ProgramViewModel program) {
        switch (enrollmentService.blockingGetEnrollmentAccess(teiUid, program.getUid())) {
            case WRITE_ACCESS:
            default:
                analytics.setEvent(ENROLL_FROM_LIST, CLICK, ENROLL_FROM_LIST);
                preferences.removeValue(Preference.CURRENT_ORG_UNIT);
                interactor.enroll(program.getUid(), teiUid);
                break;
            case PROTECTED_PROGRAM_DENIED:
            case CLOSED_PROGRAM_DENIED:
                view.displayBreakGlassError(program.getTypeName());
                break;
            case READ_ACCESS:
            case NO_ACCESS:
                view.displayAccessError();
                break;
        }
    }

    @Override
    public void onActiveEnrollClick(EnrollmentViewModel enrollmentModel) {
        preferences.removeValue(Preference.CURRENT_ORG_UNIT);
        view.changeCurrentProgram(enrollmentModel.programUid(), enrollmentModel.uid());
    }

    @Override
    public String getProgramColor(String programUid) {
        return interactor.getProgramColor(programUid);
    }

    @Override
    public void onUnselectEnrollment() {
        analytics.setEvent(DESELECT_ENROLLMENT, CLICK, DESELECT_ENROLLMENT);
        preferences.removeValue(Preference.CURRENT_ORG_UNIT);
        view.changeCurrentProgram(null, null);
    }

    @Override
    public void onDettach() {
        interactor.onDettach();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    @Override
    public void refreshData() {
        interactor.refreshData();
    }
}
