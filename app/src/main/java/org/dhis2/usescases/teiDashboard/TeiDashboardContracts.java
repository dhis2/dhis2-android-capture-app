package org.dhis2.usescases.teiDashboard;

import android.os.Bundle;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.program.Program;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardContracts {

    public interface View extends AbstractActivityContracts.View {

        void init(String teUid, String programUid);

        void setData(DashboardProgramModel program);

        void setDataWithOutProgram(DashboardProgramModel programModel);

        void goToEnrollmentList(Bundle extras);

        void restoreAdapter(String programUid);

        void handleTEIdeletion();

        void handleEnrollmentDeletion(Boolean hasMoreEnrollments);
    }

    public interface Presenter {

        void init(View view, String uid, String programUid);

        void showDescription(String description);

        void onBackPressed();

        void onEnrollmentSelectorClick();

        void setProgram(Program program);

        void onDettach();

        void getData();

        String getProgramUid();

        void deleteTei();

        void deleteEnrollment();

    }
}
