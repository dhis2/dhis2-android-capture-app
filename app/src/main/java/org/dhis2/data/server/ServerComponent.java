package org.dhis2.data.server;

import androidx.annotation.NonNull;

import org.dhis2.commons.di.dagger.PerServer;
import org.dhis2.data.dhislogic.DhisPeriodUtils;
import org.dhis2.data.user.UserComponent;
import org.dhis2.data.user.UserModule;
import org.dhis2.commons.orgunitselector.OUTreeComponent;
import org.dhis2.commons.orgunitselector.OUTreeModule;
import org.dhis2.usescases.development.ProgramRulesValidationsComponent;
import org.dhis2.usescases.development.ProgramRulesValidationsModule;
import org.dhis2.usescases.login.accounts.AccountsComponent;
import org.dhis2.usescases.login.accounts.AccountsModule;
import org.dhis2.utils.category.CategoryDialogComponent;
import org.dhis2.utils.category.CategoryDialogModule;
import org.dhis2.utils.customviews.CategoryComboDialogComponent;
import org.dhis2.utils.customviews.CategoryComboDialogModule;
import org.dhis2.utils.granularsync.GranularSyncComponent;
import org.dhis2.utils.granularsync.GranularSyncModule;

import dagger.Subcomponent;
import dhis2.org.analytics.charts.Charts;

@PerServer
@Subcomponent(modules = {ServerModule.class})
public interface ServerComponent extends Charts.Dependencies {

    @NonNull
    UserManager userManager();

    @NonNull
    OpenIdSession openIdSession();

    @NonNull
    UserComponent plus(@NonNull UserModule userModule);

    @NonNull
    GranularSyncComponent plus(@NonNull GranularSyncModule granularSyncModule);

    @NonNull
    CategoryComboDialogComponent plus(@NonNull CategoryComboDialogModule categoryComboDialogModule);

    @NonNull
    CategoryDialogComponent plus(@NonNull CategoryDialogModule categoryDialogModule);

    @NonNull
    OUTreeComponent plus(@NonNull OUTreeModule ouTreeModule);

    @NonNull
    DhisPeriodUtils dhisPeriodUtils();

    ProgramRulesValidationsComponent plus(@NonNull ProgramRulesValidationsModule module);

    AccountsComponent plus(@NonNull AccountsModule module);
}
