package org.dhis2.usescases.teidashboard;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.Program;
import org.dhis2.form.model.RowAction;
import kotlin.Unit;

public class TeiDashboardContracts {

    public interface View extends AbstractActivityContracts.View {

        void setData(DashboardProgramModel program);

        void updateDashboardProgramModel (DashboardProgramModel program);

        void setDataWithOutProgram(DashboardProgramModel programModel);

        void goToEnrollmentList();

        void restoreAdapter(String programUid);


        Unit updateEnrollmentFields(RowAction action);

        void handleTeiDeletion();

        void handleEnrollmentDeletion(Boolean hasMoreEnrollments);

        void authorityErrorMessage();

        void updateNoteBadge(int numberOfNotes);

        void hideTabsAndDisableSwipe();

        void showTabsAndEnableSwipe();

        void updateStatus();

        void displayStatusError(StatusChangeResultCode statusCode);
    }

    public interface Presenter {

        void init();

        void getNewDashboardProgramModel();

        Unit updateEnrollmentFields(RowAction action);

        void showDescription(String description);

        void onBackPressed();

        void onEnrollmentSelectorClick();

        void setProgram(Program program);

        void onDettach();

        String getProgramUid();

        void deleteTei();

        void deleteEnrollment();

        void initNoteCounter();

        void refreshTabCounters();

        void prefSaveCurrentProgram(String programUid);

        Boolean getProgramGrouping();

        void handleShowHideFilters(boolean showFilter);

        EnrollmentStatus getEnrollmentStatus(String enrollmentUid);

        void updateEnrollmentStatus(String enrollmentUid, EnrollmentStatus status);

        String getTEType();

        void trackDashboardAnalytics();

        void trackDashboardRelationships();

        void trackDashboardNotes();
    }
}
