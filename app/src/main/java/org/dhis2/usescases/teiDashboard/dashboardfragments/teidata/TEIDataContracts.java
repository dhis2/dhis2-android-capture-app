package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.dhis2.commons.data.EventViewModel;
import org.dhis2.commons.data.StageSection;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class TEIDataContracts {

    public interface View extends AbstractActivityContracts.View {

        void hideDueDate();

        void setEvents(List<EventViewModel> events, boolean canAddEvents);

        Consumer<ProgramStage> displayGenerateEvent();

        Consumer<Single<Boolean>> areEventsCompleted();

        Consumer<EnrollmentStatus> enrollmentCompleted();

        void showCatComboDialog(String eventUid, Date eventDate, String categoryComboUid);

        void switchFollowUp(boolean followUp);

        void displayGenerateEvent(String eventUid);

        void restoreAdapter(String programUid, String teiUid, String enrollmentUid);

        void seeDetails(Intent intent, Bundle bundle);

        void openEventDetails(Intent intent, Bundle bundle);

        void openEventInitial(Intent intent);

        void openEventCapture(Intent intent);

        void showTeiImage(String fileName, String defaultIcon);

        void setFilters(List<FilterItem> filterItems);

        void hideFilters();

        Flowable<StageSection> observeStageSelection(Program currentProgram, Enrollment currentEnrollment);

        void showNewEventOptions(android.view.View view, ProgramStage stageUid);

        void setEnrollmentData(Program program, Enrollment enrollment);

        void setTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, OrganisationUnit organisationUnit);

        void showPeriodRequest(FilterManager.PeriodRequest periodRequest);

        void openOrgUnitTreeSelector(String programUid);

        void setEnrollment(Enrollment enrollment);

        void showSyncDialog(String uid);
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

        void seeDetails(android.view.View sharedView, DashboardProgramModel dashboardProgramModel);

        void onScheduleSelected(String uid, android.view.View sharedView);

        void onEventSelected(String uid, EventStatus eventStatus, android.view.View sharedView);

        void setDashboardProgram(DashboardProgramModel dashboardModel);

        void setProgram(Program program, String enrollmentUid);

        void showDescription(String description);

        void onGroupingChanged(Boolean shouldGroup);

        void onAddNewEvent(@NonNull android.view.View anchor, @NonNull ProgramStage programStage);

        void getEnrollment(String enrollmentUid);

        boolean hasAssignment();

        void onSyncDialogClick();

        boolean enrollmentOrgUnitInCaptureScope(String enrollmentOrgUnit);

        void setOpeningFilterToNone();

        void setOrgUnitFilters(List<OrganisationUnit> selectedOrgUnits);
    }

}
