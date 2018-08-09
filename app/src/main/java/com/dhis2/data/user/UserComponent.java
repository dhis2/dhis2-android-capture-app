package com.dhis2.data.user;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerUser;
import com.dhis2.data.forms.FormComponent;
import com.dhis2.data.forms.FormModule;
import com.dhis2.data.service.DataServiceComponent;
import com.dhis2.data.service.DataServiceModule;
import com.dhis2.data.service.MetadataServiceComponent;
import com.dhis2.data.service.MetadataServiceModule;
import com.dhis2.data.service.ServiceComponent;
import com.dhis2.data.service.ServiceModule;
import com.dhis2.usescases.about.AboutComponent;
import com.dhis2.usescases.about.AboutModule;
import com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialComponent;
import com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialModule;
import com.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryComponent;
import com.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryModule;
import com.dhis2.usescases.jira.JiraComponent;
import com.dhis2.usescases.jira.JiraModule;
import com.dhis2.usescases.main.MainComponent;
import com.dhis2.usescases.main.MainModule;
import com.dhis2.usescases.main.program.ProgramComponent;
import com.dhis2.usescases.main.program.ProgramModule;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailComponent;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailModule;
import com.dhis2.usescases.programStageSelection.ProgramStageSelectionComponent;
import com.dhis2.usescases.programStageSelection.ProgramStageSelectionModule;
import com.dhis2.usescases.qrCodes.QrComponent;
import com.dhis2.usescases.qrCodes.QrModule;
import com.dhis2.usescases.qrCodes.eventsworegistration.QrEventsWORegistrationComponent;
import com.dhis2.usescases.qrCodes.eventsworegistration.QrEventsWORegistrationModule;
import com.dhis2.usescases.qrReader.QrReaderComponent;
import com.dhis2.usescases.qrReader.QrReaderModule;
import com.dhis2.usescases.searchTrackEntity.SearchTEComponent;
import com.dhis2.usescases.searchTrackEntity.SearchTEModule;
import com.dhis2.usescases.syncManager.SyncManagerComponent;
import com.dhis2.usescases.syncManager.SyncManagerModule;
import com.dhis2.usescases.teiDashboard.TeiDashboardComponent;
import com.dhis2.usescases.teiDashboard.TeiDashboardModule;
import com.dhis2.usescases.teiDashboard.eventDetail.EventDetailComponent;
import com.dhis2.usescases.teiDashboard.eventDetail.EventDetailModule;
import com.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailComponent;
import com.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailModule;
import com.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListComponent;
import com.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListModule;

import dagger.Subcomponent;

@PerUser
@Subcomponent(modules = UserModule.class)
public interface UserComponent {

    @NonNull
    MainComponent plus(@NonNull MainModule mainModule);


    @NonNull
    ProgramEventDetailComponent plus(@NonNull ProgramEventDetailModule programEventDetailModule);


    @NonNull
    SearchTEComponent plus(@NonNull SearchTEModule searchTEModule);

    @NonNull
    TeiDashboardComponent plus(@NonNull TeiDashboardModule dashboardModule);

    @NonNull
    ServiceComponent plus(@NonNull ServiceModule serviceModule);

    @NonNull
    QrComponent plus(@NonNull QrModule qrModule);

    @NonNull
    QrEventsWORegistrationComponent plus(@NonNull QrEventsWORegistrationModule qrModule);

    @NonNull
    MetadataServiceComponent plus(@NonNull MetadataServiceModule serviceModule);

    @NonNull
    DataServiceComponent plus(@NonNull DataServiceModule serviceModule);

    @NonNull
    TeiDataDetailComponent plus(@NonNull TeiDataDetailModule dataDetailModule);

    @NonNull
    EventDetailComponent plus(@NonNull EventDetailModule eventDetailModule);

    @NonNull
    TeiProgramListComponent plus(@NonNull TeiProgramListModule teiProgramListModule);

    @NonNull
    FormComponent plus(@NonNull FormModule enrollmentModule);

    @NonNull
    ProgramComponent plus(@NonNull ProgramModule programModule);

    @NonNull
    EventInitialComponent plus(EventInitialModule eventInitialModule);

    @NonNull
    EventSummaryComponent plus(EventSummaryModule eventInitialModule);

    @NonNull
    SyncManagerComponent plus(SyncManagerModule syncManagerModule);

    @NonNull
    ProgramStageSelectionComponent plus(ProgramStageSelectionModule programStageSelectionModule);

    @NonNull
    QrReaderComponent plus(QrReaderModule qrReaderModule);

    @NonNull
    JiraComponent plus(JiraModule jiraModule);

    @NonNull
    AboutComponent plus(AboutModule aboutModule);
}
