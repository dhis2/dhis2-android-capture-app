package org.dhis2.usescases.teiDashboard;

import androidx.lifecycle.LiveData;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.Program;

public class TeiDashboardContracts {

    public interface View extends AbstractActivityContracts.View {

        void goToEnrollmentList();

        void restoreAdapter(String programUid);

        void handleTeiDeletion();

        void authorityErrorMessage();

        void updateNoteBadge(int numberOfNotes);

        void hideTabsAndDisableSwipe();

        void showTabsAndEnableSwipe();

        void displayStatusError(StatusChangeResultCode statusCode);
    }

    public interface Presenter {

        void showDescription(String description);

        void onBackPressed();

        void onEnrollmentSelectorClick();

        void setProgram(Program program);

        void onDettach();

        String getProgramUid();

        void deleteTei();

        void initNoteCounter();

        void refreshTabCounters();

        void prefSaveCurrentProgram(String programUid);

        void handleShowHideFilters(boolean showFilter);

        EnrollmentStatus getEnrollmentStatus(String enrollmentUid);

        void updateEnrollmentStatus(String enrollmentUid, EnrollmentStatus status);

        String getTEType();

        void trackDashboardAnalytics();

        void trackDashboardRelationships();

        void trackDashboardNotes();

        Boolean checkIfTEICanBeDeleted();

        Boolean checkIfEnrollmentCanBeDeleted(String enrollmentUid);
    }
}
