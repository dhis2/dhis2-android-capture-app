package org.dhis2.usescases.teiDashboard;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.Program;

public class TeiDashboardContracts {

    public interface View extends AbstractActivityContracts.View {

        void setData(DashboardProgramModel program);

        void setDataWithOutProgram(DashboardProgramModel programModel);

        void goToEnrollmentList();

        void restoreAdapter(String programUid);

        void handleTeiDeletion();

        void handleEnrollmentDeletion(Boolean hasMoreEnrollments);

        void authorityErrorMessage();

        void updateNoteBadge(int numberOfNotes);

        void setFiltersLayoutState();

        void updateTotalFilters(Integer totalFilters);

        void hideTabsAndDisableSwipe();

        void showTabsAndEnableSwipe();

        void updateStatus();
    }

    public interface Presenter {

        void init();

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

        int getProgramTheme(int appTheme);

        void prefSaveCurrentProgram(String programUid);

        void saveProgramTheme(int programTheme);

        void removeProgramTheme();

        Boolean getProgramGrouping();

        void generalFiltersClick();

        void handleShowHideFilters(boolean showFilter);

        EnrollmentStatus getEnrollmentStatus(String enrollmentUid);

        void updateEnrollmentStatus(String enrollmentUid, EnrollmentStatus status);
    }
}
