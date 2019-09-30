package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import android.content.Intent;
import android.os.Bundle;

import org.dhis2.data.sharedPreferences.SharePreferencesProvider;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class TEIDataContracts {

    public interface View extends AbstractActivityContracts.View {

        Consumer<List<Event>> setEvents();

        Consumer<ProgramStage> displayGenerateEvent();

        Consumer<Single<Boolean>> areEventsCompleted();

        Consumer<EnrollmentStatus> enrollmentCompleted();

        void showCatComboDialog(String eventId, CategoryCombo categoryCombo);

        void switchFollowUp(boolean followUp);

        void displayGenerateEvent(String eventUid);

        void restoreAdapter(String programUid);

        void seeDetails(Intent intent, Bundle bundle);

        void showQR(Intent intent);

        void openEventDetails(Intent intent, Bundle bundle);

        void openEventInitial(Intent intent);

        void openEventCapture(Intent intent);

        void showTeiImage(String fileName);

        void setPreference(SharePreferencesProvider provider);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void init(View view);

        void getTEIEvents();

        void getCatComboOptions(Event event);

        void setDefaultCatOptCombToEvent(String eventUid);

        void changeCatOption(String eventUid, String catOptionComboUid);

        void areEventsCompleted();

        void displayGenerateEvent(String eventUid);

        void completeEnrollment();

        void onFollowUp(DashboardProgramModel dashboardProgramModel);

        void onShareClick(android.view.View mView);

        void seeDetails(android.view.View sharedView, DashboardProgramModel dashboardProgramModel);

        void onScheduleSelected(String uid, android.view.View sharedView);

        void onEventSelected(String uid, EventStatus eventStatus, android.view.View sharedView);

        void setDashboardProgram(DashboardProgramModel dashboardModel);

        void setProgram(Program program);

        void showDescription(String description);
    }

}
