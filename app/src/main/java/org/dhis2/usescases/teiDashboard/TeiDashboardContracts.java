package org.dhis2.usescases.teiDashboard;

import android.os.Bundle;
import android.widget.TextView;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.relationship.RelationshipModel;

import java.util.Calendar;

import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.LiveData;

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
        // TEI Data and Relationship
        LiveData<DashboardProgramModel> observeDashboardModel();

        String getTeUid();

        // Tei Dashboard

        void init(View view, String uid, String programUid);

        void showDescription(String description);

        void onBackPressed();

        void onEnrollmentSelectorClick();

        void onShareQRClick();

        void setProgram(ProgramModel program);

        void onEventSelected(String uid, android.view.View view);

        void onDettach();

        void onDescriptionClick(String description);

        String getProgramUid();

        void subscribeToRelationshipLabel(RelationshipModel relationship, TextView textView);

        void generateEvent(String lastModifiedEventUid, Integer integer);

        void generateEventFromDate(String lastModifiedEventUid, Calendar chosenDate);

        void onScheduleSelected(String uid, android.view.View sharedView);

        void changeCatOption(String eventUid, String catOptComboUid);
    }
}