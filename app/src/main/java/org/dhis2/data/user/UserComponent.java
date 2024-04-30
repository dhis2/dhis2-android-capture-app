package org.dhis2.data.user;

import androidx.annotation.NonNull;

import org.dhis2.commons.di.dagger.PerUser;
import org.dhis2.commons.dialogs.calendarpicker.di.CalendarPickerComponent;
import org.dhis2.commons.dialogs.calendarpicker.di.CalendarPickerModule;
import org.dhis2.commons.featureconfig.di.FeatureConfigActivityComponent;
import org.dhis2.commons.featureconfig.di.FeatureConfigActivityModule;
import org.dhis2.commons.filters.data.FilterPresenter;
import org.dhis2.data.service.ReservedValuesWorkerComponent;
import org.dhis2.data.service.ReservedValuesWorkerModule;
import org.dhis2.data.service.SyncDataWorkerComponent;
import org.dhis2.data.service.SyncDataWorkerModule;
import org.dhis2.data.service.SyncGranularRxComponent;
import org.dhis2.data.service.SyncGranularRxModule;
import org.dhis2.data.service.SyncInitWorkerComponent;
import org.dhis2.data.service.SyncInitWorkerModule;
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
import org.dhis2.usescases.events.ScheduledEventComponent;
import org.dhis2.usescases.events.ScheduledEventModule;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureModule;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsModule;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialModule;
import org.dhis2.usescases.main.MainComponent;
import org.dhis2.usescases.main.MainModule;
import org.dhis2.usescases.main.program.ProgramComponent;
import org.dhis2.usescases.main.program.ProgramModule;
import org.dhis2.usescases.notes.NotesComponent;
import org.dhis2.usescases.notes.NotesModule;
import org.dhis2.usescases.notes.noteDetail.NoteDetailComponent;
import org.dhis2.usescases.notes.noteDetail.NoteDetailModule;
import org.dhis2.usescases.programEventDetail.ProgramEventDetailComponent;
import org.dhis2.usescases.programEventDetail.ProgramEventDetailModule;
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionInjector;
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionModule;
import org.dhis2.usescases.qrCodes.QrComponent;
import org.dhis2.usescases.qrCodes.QrModule;
import org.dhis2.usescases.qrCodes.eventsworegistration.QrEventsWORegistrationComponent;
import org.dhis2.usescases.qrCodes.eventsworegistration.QrEventsWORegistrationModule;
import org.dhis2.usescases.qrReader.QrReaderComponent;
import org.dhis2.usescases.qrReader.QrReaderModule;
import org.dhis2.usescases.qrScanner.ScanComponent;
import org.dhis2.usescases.qrScanner.ScanModule;
import org.dhis2.usescases.reservedValue.ReservedValueComponent;
import org.dhis2.usescases.reservedValue.ReservedValueModule;
import org.dhis2.usescases.searchTrackEntity.SearchTEComponent;
import org.dhis2.usescases.searchTrackEntity.SearchTEModule;
import org.dhis2.usescases.settings.SyncManagerComponent;
import org.dhis2.usescases.settings.SyncManagerModule;
import org.dhis2.usescases.settingsprogram.ProgramSettingsComponent;
import org.dhis2.usescases.settingsprogram.SettingsProgramModule;
import org.dhis2.usescases.sms.SmsComponent;
import org.dhis2.usescases.sms.SmsModule;
import org.dhis2.usescases.sync.SyncComponent;
import org.dhis2.usescases.sync.SyncModule;
import org.dhis2.usescases.teiDashboard.TeiDashboardComponent;
import org.dhis2.usescases.teiDashboard.TeiDashboardModule;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipComponent;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipModule;
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListComponent;
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListModule;
import org.dhis2.utils.optionset.OptionSetComponent;
import org.dhis2.utils.optionset.OptionSetModule;
import org.dhis2.utils.session.PinModule;
import org.dhis2.utils.session.SessionComponent;

import dagger.Subcomponent;
import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentComponent;
import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentModule;

@PerUser
@Subcomponent(modules = UserModule.class)
public interface UserComponent extends UserComponentFlavor{

    FilterPresenter filterPresenter();

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
    TeiProgramListComponent plus(@NonNull TeiProgramListModule teiProgramListModule);

    @NonNull
    ProgramComponent plus(@NonNull ProgramModule programModule);

    @NonNull
    EventInitialComponent plus(EventInitialModule eventInitialModule);

    @NonNull
    SyncManagerComponent plus(SyncManagerModule syncManagerModule);

    @NonNull
    ProgramStageSelectionInjector plus(ProgramStageSelectionModule programStageSelectionModule);

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
    SmsComponent plus(SmsModule smsModule);

    @NonNull
    SyncGranularRxComponent plus(SyncGranularRxModule syncGranularRxModule);

    @NonNull
    SyncComponent plus(SyncModule syncModule);

    @NonNull
    SyncInitWorkerComponent plus(SyncInitWorkerModule syncInitWorkerModule);

    @NonNull
    EnrollmentComponent plus(EnrollmentModule enrollmentModule);

    @NonNull
    ScheduledEventComponent plus(ScheduledEventModule scheduledEventModule);

    @NonNull
    OptionSetComponent plus(OptionSetModule optionSetModule);

    @NonNull
    NotesComponent plus(NotesModule notesModule);

    @NonNull
    NoteDetailComponent plus(NoteDetailModule noteDetailModule);

    @NonNull
    ProgramSettingsComponent plus(SettingsProgramModule settingsProgramModule);

    @NonNull
    ScanComponent plus(ScanModule scanModule);

    @NonNull
    FeatureConfigActivityComponent plus(FeatureConfigActivityModule featureModule);

    @NonNull
    CalendarPickerComponent plus(CalendarPickerModule calendarPickerModule);

    @NonNull
    AnalyticsFragmentComponent plus(AnalyticsFragmentModule analyticsFragmentModule);

    @NonNull
    RelationshipComponent plus(RelationshipModule relationshipModule);

    @NonNull
    EventDetailsComponent plus(EventDetailsModule eventDetailsModule);

    @NonNull
    SessionComponent plus(PinModule pinModule);
}
