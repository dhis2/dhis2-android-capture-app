package org.dhis2

import org.dhis2.data.forms.FormComponent
import org.dhis2.data.forms.FormModule
import org.dhis2.data.server.ServerComponent
import org.dhis2.data.user.UserComponent

import org.dhis2.usescases.login.LoginComponent
import org.dhis2.usescases.sync.SyncComponent
import org.hisp.dhis.android.core.configuration.Configuration
import org.hisp.dhis.android.core.configuration.ConfigurationModel

interface Components {

    fun appComponent(): AppComponent

    ///////////////////////////////////////////////////////////////////
    // Login component
    ///////////////////////////////////////////////////////////////////


    fun createLoginComponent(): LoginComponent

    fun loginComponent(): LoginComponent?

    fun releaseLoginComponent()


    ///////////////////////////////////////////////////////////////////
    // Synchronization component
    ///////////////////////////////////////////////////////////////////


    fun createSyncComponent(): SyncComponent

    fun syncComponent(): SyncComponent?

    fun releaseSyncComponent()


    ////////////////////////////////////////////////////////////////////
    // Server component
    ////////////////////////////////////////////////////////////////////

    fun createServerComponent(configuration: Configuration): ServerComponent

    fun serverComponent(): ServerComponent?

    fun releaseServerComponent()

    ////////////////////////////////////////////////////////////////////
    // User component
    ////////////////////////////////////////////////////////////////////

    fun createUserComponent(): UserComponent

    fun userComponent(): UserComponent?

    fun releaseUserComponent()

    ////////////////////////////////////////////////////////////////////
    // Form component
    ////////////////////////////////////////////////////////////////////

    fun createFormComponent(formModule: FormModule): FormComponent

    fun formComponent(): FormComponent?

    fun releaseFormComponent()
}
