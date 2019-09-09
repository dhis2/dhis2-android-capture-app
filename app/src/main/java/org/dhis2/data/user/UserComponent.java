package org.dhis2.data.user;

import org.dhis2.data.dagger.PerUser;
import org.dhis2.data.forms.FormComponent;
import org.dhis2.data.forms.FormModule;
import org.dhis2.data.service.ReservedValuesWorkerComponent;
import org.dhis2.data.service.ReservedValuesWorkerModule;
import org.dhis2.data.service.SyncDataWorkerComponent;
import org.dhis2.data.service.SyncDataWorkerModule;
import org.dhis2.data.service.SyncMetadataWorkerComponent;
import org.dhis2.data.service.SyncMetadataWorkerModule;
import org.dhis2.usescases.about.AboutComponent;
import org.dhis2.usescases.about.AboutModule;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableComponent;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModule;
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailComponent;
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModule;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialComponent;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialModule;
import org.dhis2.usescases.enrollment.EnrollmentComponent;
import org.dhis2.usescases.enrollment.EnrollmentModule;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureModule;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialModule;
import org.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryModule;
import org.dhis2.usescases.main.MainComponent;
import org.dhis2.usescases.main.MainModule;
import org.dhis2.usescases.main.program.ProgramComponent;
import org.dhis2.usescases.main.program.ProgramModule;
import org.dhis2.usescases.programEventDetail.ProgramEventDetailComponent;
import org.dhis2.usescases.programEventDetail.ProgramEventDetailModule;
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionComponent;
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionModule;
import org.dhis2.usescases.qrCodes.QrComponent;
import org.dhis2.usescases.qrCodes.QrModule;
import org.dhis2.usescases.qrCodes.eventsworegistration.QrEventsWORegistrationComponent;
import org.dhis2.usescases.qrCodes.eventsworegistration.QrEventsWORegistrationModule;
import org.dhis2.usescases.qrReader.QrReaderComponent;
import org.dhis2.usescases.qrReader.QrReaderModule;
import org.dhis2.usescases.reservedValue.ReservedValueComponent;
import org.dhis2.usescases.reservedValue.ReservedValueModule;
import org.dhis2.usescases.searchTrackEntity.SearchTEComponent;
import org.dhis2.usescases.searchTrackEntity.SearchTEModule;
import org.dhis2.usescases.settings.SyncManagerComponent;
import org.dhis2.usescases.settings.SyncManagerModule;
import org.dhis2.usescases.teiDashboard.TeiDashboardComponent;
import org.dhis2.usescases.teiDashboard.TeiDashboardModule;
import org.dhis2.usescases.teiDashboard.eventDetail.EventDetailComponent;
import org.dhis2.usescases.teiDashboard.eventDetail.EventDetailModule;
import org.dhis2.usescases.teiDashboard.nfc_data.NfcDataWriteComponent;
import org.dhis2.usescases.teiDashboard.nfc_data.NfcDataWriteModule;
import org.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailComponent;
import org.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailModule;
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListComponent;
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListModule;

import androidx.annotation.NonNull;
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
    QrComponent plus(@NonNull QrModule qrModule);

    @NonNull
    QrEventsWORegistrationComponent plus(@NonNull QrEventsWORegistrationModule qrModule);

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
    AboutComponent plus(AboutModule aboutModule);

    @NonNull
    DataSetDetailComponent plus(DataSetDetailModule dataSetDetailModel);

    @NonNull
    DataSetInitialComponent plus(DataSetInitialModule dataSetInitialModule);

    @NonNull
    DataSetTableComponent plus(DataSetTableModule dataSetTableModule);

    @NonNull
    ReservedValueComponent plus(ReservedValueModule reservedValueModule);

    @NonNull
    SyncDataWorkerComponent plus(SyncDataWorkerModule syncDataWorkerModule);

    @NonNull
    SyncMetadataWorkerComponent plus(SyncMetadataWorkerModule syncDataWorkerModule);

    @NonNull
    ReservedValuesWorkerComponent plus(ReservedValuesWorkerModule reservedValuesWorkerModule);

    @NonNull
    EventCaptureComponent plus(EventCaptureModule eventCaptureModule);

    @NonNull
    EnrollmentComponent plus(EnrollmentModule enrollmentModule);

    NfcDataWriteComponent plus(NfcDataWriteModule nfcModule);
}
