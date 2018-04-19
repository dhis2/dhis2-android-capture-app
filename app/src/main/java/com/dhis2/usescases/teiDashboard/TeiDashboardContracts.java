package com.dhis2.usescases.teiDashboard;

import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import com.dhis2.usescases.teiDashboard.dashboardfragments.IndicatorsFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.NotesFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.ScheduleFragment;

import org.hisp.dhis.android.core.program.ProgramModel;

import io.reactivex.Flowable;

/**
 * Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardContracts {

    public interface View extends AbstractActivityContracts.View {

        void init(String teUid, String programUid);

        void setData(DashboardProgramModel program);

        void setDataWithOutProgram(DashboardProgramModel programModel);

        String getToolbarTitle();

        DashboardPagerAdapter getAdapter();
    }

    public interface Presenter {
        void init(View view, String uid, String programUid);

        void onBackPressed();

        void onEnrollmentSelectorClick();

        void setProgram(ProgramModel program);

        void seeDetails(android.view.View view, DashboardProgramModel dashboardProgramModel);

        void onEventSelected(String uid, android.view.View view);

        void onFollowUp(DashboardProgramModel dashboardProgramModel);

        void onDettach();

        void getData();

        //Data Fragment

        //RelationshipFragment

        //IndicatorsFragment
        void subscribeToIndicators(IndicatorsFragment indicatorsFragment);

        //ScheduleFragment
        void subscribeToScheduleEvents(ScheduleFragment scheduleFragment);

        //NoteFragment
        void setNoteProcessor(Flowable<Pair<String, Boolean>> noteProcessor);

        void subscribeToNotes(NotesFragment notesFragment);

        String getTeUid();

        String getProgramUid();

        Boolean hasProgramWritePermission();
    }
}
