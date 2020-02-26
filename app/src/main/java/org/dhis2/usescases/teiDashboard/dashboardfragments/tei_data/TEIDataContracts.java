package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.tei_events.EventViewModel;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class TEIDataContracts {

    public interface View extends AbstractActivityContracts.View {

        Consumer<List<EventViewModel>> setEvents();

        Consumer<ProgramStage> displayGenerateEvent();

        Consumer<Single<Boolean>> areEventsCompleted();

        Consumer<EnrollmentStatus> enrollmentCompleted();

        void showCatComboDialog(String eventId, CategoryCombo categoryCombo, List<CategoryOptionCombo> categoryOptionCombos);

        void switchFollowUp(boolean followUp);

        void displayGenerateEvent(String eventUid);

        void restoreAdapter(String programUid, String teiUid, String enrollmentUid);

        void seeDetails(Intent intent, Bundle bundle);

        void showQR(Intent intent);

        void openEventDetails(Intent intent, Bundle bundle);

        void openEventInitial(Intent intent);

        void openEventCapture(Intent intent);

        void showTeiImage(String fileName, String defaultIcon);

        Flowable<String> observeStageSelection(Program currentProgram, Enrollment currentEnrollment);

        void showNewEventOptions(android.view.View view, ProgramStage stageUid);

        void setEnrollmentData(Program program, Enrollment enrollment);

        void setTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, OrganisationUnit organisationUnit);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void init();

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

        void setProgram(Program program, String enrollmentUid);

        void showDescription(String description);

        void onGroupingChanged(Boolean shouldGroup);

        void onAddNewEvent(@NonNull android.view.View anchor, @NonNull ProgramStage programStage);
    }

}
