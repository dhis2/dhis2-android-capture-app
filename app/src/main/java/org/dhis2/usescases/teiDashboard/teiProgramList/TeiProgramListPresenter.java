package org.dhis2.usescases.teiDashboard.teiProgramList;

import org.dhis2.R;
import org.dhis2.data.prefs.Preference;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.usescases.main.program.ProgramViewModel;
import org.dhis2.utils.analytics.AnalyticsHelper;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DESELECT_ENROLLMENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.ENROLL_FROM_LIST;

/**
 * QUADRAM. Created by Cristian on 06/03/2018.
 */

public class TeiProgramListPresenter implements TeiProgramListContract.Presenter {

    private final PreferenceProvider preferences;
    private final AnalyticsHelper analytics;
    private TeiProgramListContract.View view;
    private final TeiProgramListContract.Interactor interactor;
    private String teiUid;

    TeiProgramListPresenter(TeiProgramListContract.Interactor interactor,
                            String trackedEntityId,
                            PreferenceProvider preferenceProvider, AnalyticsHelper analyticsHelper) {
        this.interactor = interactor;
        this.teiUid = trackedEntityId;
        this.preferences = preferenceProvider;
        this.analytics = analyticsHelper;

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
            analytics.setEvent(ENROLL_FROM_LIST, CLICK, ENROLL_FROM_LIST);
            preferences.removeValue(Preference.CURRENT_ORG_UNIT);
            interactor.enroll(program.id(), teiUid);
        } else
            view.displayMessage(view.getContext().getString(R.string.search_access_error));
    }

    @Override
    public void onActiveEnrollClick(EnrollmentViewModel enrollmentModel) {
        preferences.removeValue(Preference.CURRENT_ORG_UNIT);
        view.changeCurrentProgram(enrollmentModel.programUid());
    }

    @Override
    public String getProgramColor(String programUid) {
        return interactor.getProgramColor(programUid);
    }

    @Override
    public void onUnselectEnrollment() {
        analytics.setEvent(DESELECT_ENROLLMENT, CLICK, DESELECT_ENROLLMENT);
        preferences.removeValue(Preference.CURRENT_ORG_UNIT);
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
