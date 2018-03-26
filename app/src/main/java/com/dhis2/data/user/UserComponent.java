package com.dhis2.data.user;

import com.dhis2.data.dagger.PerUser;
import com.dhis2.data.forms.FormComponent;
import com.dhis2.data.forms.FormModule;
import com.dhis2.data.service.ServiceComponent;
import com.dhis2.data.service.ServiceModule;
import com.dhis2.usescases.appInfo.InfoComponent;
import com.dhis2.usescases.appInfo.InfoModule;
import com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialComponent;
import com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialModule;
import com.dhis2.usescases.main.MainComponent;
import com.dhis2.usescases.main.MainModule;
import com.dhis2.usescases.main.program.ProgramComponent;
import com.dhis2.usescases.main.program.ProgramModule;
import com.dhis2.usescases.programDetail.ProgramDetailComponent;
import com.dhis2.usescases.programDetail.ProgramDetailModule;
import com.dhis2.usescases.programDetailTablet.ProgramDetailTabletComponent;
import com.dhis2.usescases.programDetailTablet.ProgramDetailTabletModule;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailComponent;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailModule;
import com.dhis2.usescases.searchTrackEntity.SearchTEComponent;
import com.dhis2.usescases.searchTrackEntity.SearchTEModule;
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

    
    MainComponent plus(MainModule mainModule);

    
    ProgramDetailComponent plus(ProgramDetailModule programDetailContractModule);

    
    ProgramEventDetailComponent plus(ProgramEventDetailModule programEventDetailModule);

    
    ProgramDetailTabletComponent plus(ProgramDetailTabletModule programDetailModule);

    
    SearchTEComponent plus(SearchTEModule searchTEModule);

    
    TeiDashboardComponent plus(TeiDashboardModule dashboardModule);

    
    ServiceComponent plus(ServiceModule serviceModule);

    
    TeiDataDetailComponent plus(TeiDataDetailModule dataDetailModule);

    
    EventDetailComponent plus(EventDetailModule eventDetailModule);

    
    TeiProgramListComponent plus(TeiProgramListModule teiProgramListModule);

    
    FormComponent plus(FormModule enrollmentModule);

    
    ProgramComponent plus(ProgramModule programModule);

    
    InfoComponent plus(InfoModule infoModule);

    
    EventInitialComponent plus(EventInitialModule eventInitialModule);
}
