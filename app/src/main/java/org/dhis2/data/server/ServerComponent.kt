package org.dhis2.data.server

import org.dhis2.data.dagger.PerServer
import org.dhis2.data.user.UserComponent
import org.dhis2.data.user.UserModule

import dagger.Subcomponent

@PerServer
@Subcomponent(modules = [ServerModule::class])
interface ServerComponent {

    fun userManager(): UserManager

    operator fun plus(userModule: UserModule): UserComponent
}
