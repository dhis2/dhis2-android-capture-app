package com.dhis2.data.user;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerUser;
import com.dhis2.data.forms.FormComponent;
import com.dhis2.data.forms.FormModule;
import com.dhis2.data.service.ServiceComponent;
import com.dhis2.data.service.ServiceModule;
import com.dhis2.usescases.appInfo.InfoComponent;
import com.dhis2.usescases.appInfo.InfoModule;
import com.dhis2.usescases.eventDetail.EventDetailComponent;
import com.dhis2.usescases.eventDetail.EventDetailModule;
import com.dhis2.usescases.main.MainComponent;
import com.dhis2.usescases.main.MainModule;
import com.dhis2.usescases.main.program.ProgramComponent;
import com.dhis2.usescases.main.program.ProgramModule;
import com.dhis2.usescases.programDetail.ProgramDetailComponent;
import com.dhis2.usescases.programDetail.ProgramDetailModule;
import com.dhis2.usescases.programDetailTablet.ProgramDetailTabletComponent;
import com.dhis2.usescases.programDetailTablet.ProgramDetailTabletModule;
import com.dhis2.usescases.searchTrackEntity.SearchTEComponent;
import com.dhis2.usescases.searchTrackEntity.SearchTEModule;
import com.dhis2.usescases.teiDashboard.TeiDashboardComponent;
import com.dhis2.usescases.teiDashboard.TeiDashboardModule;
import com.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailComponent;
import com.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailModule;

import dagger.Subcomponent;

@PerUser
@Subcomponent(modules = UserModule.class)
public interface UserComponent {

    @NonNull
    MainComponent plus(@NonNull MainModule mainModule);

    @NonNull
    ProgramDetailComponent plus(@NonNull ProgramDetailModule programDetailContractModule);

    @NonNull
    ProgramDetailTabletComponent plus(@NonNull ProgramDetailTabletModule programDetailModule);

    @NonNull
    SearchTEComponent plus(@NonNull SearchTEModule searchTEModule);

    @NonNull
    TeiDashboardComponent plus(@NonNull TeiDashboardModule dashboardModule);

    @NonNull
    ServiceComponent plus(@NonNull ServiceModule serviceModule);

    @NonNull
    TeiDataDetailComponent plus(@NonNull TeiDataDetailModule dataDetailModule);

    @NonNull
    EventDetailComponent plus(@NonNull EventDetailModule eventDetailModule);

    @NonNull
    FormComponent plus(@NonNull FormModule enrollmentModule);

    @NonNull
    ProgramComponent plus(@NonNull ProgramModule programModule);

    @NonNull
    InfoComponent plus(@NonNull InfoModule infoModule);
}
