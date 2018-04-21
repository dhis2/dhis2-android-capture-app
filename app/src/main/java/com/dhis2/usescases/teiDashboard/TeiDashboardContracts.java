package com.dhis2.usescases.teiDashboard;

import android.widget.TextView;

import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import com.dhis2.usescases.teiDashboard.dashboardfragments.IndicatorsFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.NotesFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.ScheduleFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;

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

        DashboardProgramModel getDashBoardData();

        //Data Fragment

        //RelationshipFragment
        void subscribeToRelationships(RelationshipFragment relationshipFragment);

        void goToAddRelationship();

        void addRelationship(String trackEntityInstance_A, String relationshipType);

        void deleteRelationship(long relationshipId);

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

        void subscribeToMainAttr(String teiUid, TextView textView);
    }
}
