package com.dhis2.usescases.teiDashboard;

import android.os.Bundle;
import android.widget.TextView;

import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import com.dhis2.usescases.teiDashboard.dashboardfragments.IndicatorsFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.NotesFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.ScheduleFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

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

        void showQR();

        void goToEnrollmentList(Bundle extras);
    }

    public interface Presenter {
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

        //RelationshipFragment
        Observable<List<TrackedEntityAttributeValueModel>> getTEIMainAttributes(String teiUid);

        void subscribeToRelationships(RelationshipFragment relationshipFragment);

        void goToAddRelationship();

        void addRelationship(String trackEntityInstance_A, String relationshipType);

        void deleteRelationship(Relationship relationshipModel);

        //IndicatorsFragment
        void subscribeToIndicators(IndicatorsFragment indicatorsFragment);

        void onDescriptionClick(String description);

        //ScheduleFragment
        void subscribeToScheduleEvents(ScheduleFragment scheduleFragment);

        //NoteFragment
        void setNoteProcessor(Flowable<Pair<String, Boolean>> noteProcessor);

        void subscribeToNotes(NotesFragment notesFragment);

        String getTeUid();

        String getProgramUid();

        Boolean hasProgramWritePermission();

        void openDashboard(String teiUid);

        void subscribeToRelationshipLabel(RelationshipModel relationship, TextView textView);

        void completeEnrollment(TEIDataFragment teiDataFragment);

        void displayGenerateEvent(TEIDataFragment teiDataFragment, String eventUid);

        void generateEvent(String lastModifiedEventUid, Integer integer);

        void generateEventFromDate(String lastModifiedEventUid, Calendar chosenDate);
    }
}
