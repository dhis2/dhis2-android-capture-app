package com.dhis2.data.user;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerUser;
import com.dhis2.data.service.ServiceComponent;
import com.dhis2.data.service.ServiceModule;
import com.dhis2.usescases.main.MainComponent;
import com.dhis2.usescases.main.MainContractsModule;

import dagger.Subcomponent;
@PerUser
@Subcomponent(modules = UserModule.class)
public interface UserComponent {

    @NonNull
    MainComponent plus(@NonNull MainContractsModule mainModule);

//    // Todo: remove this
//    @NonNull
//    HomeComponent plus(@NonNull HomeModule homeModule);

//    @NonNull
//    SearchComponent plus(@NonNull SearchModule searchModule);
//
//    @NonNull
//    ReportsComponent plus(@NonNull ReportsModule reportsModule);
//

    @NonNull
    ServiceComponent plus(@NonNull ServiceModule serviceModule);
//
//    @NonNull
//    SelectionComponent plus(@NonNull SelectionModule selectionModule);
//
//    @NonNull
//    OptionSelectionComponent plus(@NonNull OptionSelectionModule optionSelectionModule,
//                                  @NonNull DataEntryStoreModule dataEntryStoreModule);
//
//    @NonNull
//    FormComponent plus(@NonNull FormModule formModule);
//
//    @NonNull
//    CreateItemsComponent plus(@NonNull CreateItemsModule createItemsModule);
//
//    NavigationComponent plus(@NonNull NavigationModule navigationModule);
}
