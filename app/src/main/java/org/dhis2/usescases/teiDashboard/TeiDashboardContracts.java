package org.dhis2.usescases.teiDashboard;

import android.os.Bundle;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.program.ProgramModel;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardContracts {

    public interface View extends AbstractActivityContracts.View {

        void init(String teUid, String programUid);

        void setData(DashboardProgramModel program);

        void setDataWithOutProgram(DashboardProgramModel programModel);

        String getToolbarTitle();

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

        void setProgram(ProgramModel program);

        void onDettach();

        void getData();

        String getProgramUid();

        void deteleteTei();

        void deleteEnrollment();

    }
}
