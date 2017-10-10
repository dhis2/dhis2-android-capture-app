package com.data.user;

import android.support.annotation.NonNull;

import com.data.dagger.PerUser;

import dagger.Subcomponent;

@PerUser
@Subcomponent(modules = UserModule.class)
public interface UserComponent {

//    @NonNull
//    MainComponent plus(@NonNull MainModule mainModule);
//
//    // Todo: remove this
//    @NonNull
//    HomeComponent plus(@NonNull HomeModule homeModule);
//
//    @NonNull
//    SearchComponent plus(@NonNull SearchModule searchModule);
//
//    @NonNull
//    ReportsComponent plus(@NonNull ReportsModule reportsModule);
//
//    @NonNull
//    ServiceComponent plus(@NonNull ServiceModule serviceModule);
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
