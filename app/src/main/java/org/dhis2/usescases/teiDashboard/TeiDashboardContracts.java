package org.dhis2.usescases.teiDashboard;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.program.Program;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardContracts {

    public interface View extends AbstractActivityContracts.View {

        void setData(DashboardProgramModel program);

        void setDataWithOutProgram(DashboardProgramModel programModel);

        void goToEnrollmentList();

        void restoreAdapter(String programUid);

        void handleTEIdeletion();

        void handleEnrollmentDeletion(Boolean hasMoreEnrollments);

        void authorityErrorMessage();
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

    }
}
