package org.dhis2.usescases.teiDashboard;

import android.os.Bundle;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.program.ProgramModel;

import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardContracts {

    public interface View extends AbstractActivityContracts.View {

        void init(String teUid, String programUid);

        void setData(DashboardProgramModel program);

        void setDataWithOutProgram(DashboardProgramModel programModel);

        String getToolbarTitle();

        FragmentStatePagerAdapter getAdapter();

        void showQR();

        void goToEnrollmentList(Bundle extras);

        void restoreAdapter(String programUid);

        void showCatComboDialog(String eventId, CategoryCombo catCombo);
    }

    public interface Presenter {

        void onBackPressed();

        void onEnrollmentSelectorClick();

        void changeCatOption(String eventUid, String catOptComboUid);

        void init(View view, String uid, String programUid);

        void onDettach();

        void showDescription(String description);

        void onScheduleSelected(String uid, android.view.View sharedView);

        void onEventSelected(String uid, android.view.View view);

        String getProgramUid();

        void setProgram(ProgramModel program);

    }
}