package org.dhis2.usescases.teiDashboard;

import android.os.Bundle;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.teiDashboard.dashboardfragments.notes.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Calendar;

import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.LiveData;
import io.reactivex.Flowable;

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
        LiveData<DashboardProgramModel> observeDashboardModel();

        void init(View view, String uid, String programUid);

        void showDescription(String description);

        void onBackPressed();

        void onEnrollmentSelectorClick();

        void onShareQRClick();

        void setProgram(ProgramModel program);

        void seeDetails(android.view.View view, DashboardProgramModel dashboardProgramModel);

        void onEventSelected(String uid, android.view.View view);

        void onFollowUp(DashboardProgramModel dashboardProgramModel);

        void onDettach();

        void getData();

        DashboardProgramModel getDashBoardData();

        void getTEIEvents(TEIDataFragment teiDataFragment);

        void areEventsCompleted(TEIDataFragment teiDataFragment);

        //Data Fragment
        void onShareClick(android.view.View view);

        //NoteFragment
       /* void setNoteProcessor(Flowable<Pair<String, Boolean>> noteProcessor);

        void subscribeToNotes(NotesFragment notesFragment);*/

        String getTeUid();

        String getProgramUid();

        Boolean hasProgramWritePermission();

        void completeEnrollment(TEIDataFragment teiDataFragment);

        void displayGenerateEvent(TEIDataFragment teiDataFragment, String eventUid);

        void generateEvent(String lastModifiedEventUid, Integer integer);

        void generateEventFromDate(String lastModifiedEventUid, Calendar chosenDate);

        void onScheduleSelected(String uid, android.view.View sharedView);

        void getCatComboOptions(EventModel event);

        void changeCatOption(String eventUid, String catOptComboUid);

        void setDefaultCatOptCombToEvent(String eventUid);
    }
}
