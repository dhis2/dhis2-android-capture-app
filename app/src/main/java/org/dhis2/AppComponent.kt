package org.dhis2

import org.dhis2.data.database.DbModule
import org.dhis2.data.metadata.MetadataModule
import org.dhis2.data.schedulers.SchedulerModule
import org.dhis2.data.server.ServerComponent
import org.dhis2.data.server.ServerModule
import org.dhis2.usescases.login.LoginComponent
import org.dhis2.usescases.login.LoginModule
import org.dhis2.usescases.splash.SplashComponent
import org.dhis2.usescases.splash.SplashModule
import org.dhis2.usescases.sync.SyncComponent
import org.dhis2.usescases.sync.SyncModule
import org.dhis2.utils.UtilsModule

import javax.inject.Singleton

import dagger.Component

/**
 * Created by ppajuelo on 10/10/2017.
 */
@Singleton
@Component(modules = [AppModule::class, DbModule::class, SchedulerModule::class, UtilsModule::class, MetadataModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        fun appModule(appModule: AppModule): Builder

        fun dbModule(dbModule: DbModule): Builder

        fun schedulerModule(schedulerModule: SchedulerModule): Builder

        fun utilModule(utilsModule: UtilsModule): Builder

        fun metadataModule(metadataModule: MetadataModule): Builder

        fun build(): AppComponent
        //ter
    }

    //injection targets
    fun inject(app: App)

    //sub-components
    operator fun plus(serverModule: ServerModule): ServerComponent

    operator fun plus(module: SplashModule): SplashComponent

    operator fun plus(loginContractsModule: LoginModule): LoginComponent

    operator fun plus(syncModule: SyncModule): SyncComponent

}
