package org.dhis2.mobile.app.di

import org.dhis2.mobile.app.splash.di.splashModule
import org.dhis2.mobile.commons.di.commonsModule
import org.dhis2.mobile.login.main.di.fakeLoginDataModule
import org.dhis2.mobile.login.main.di.loginFeatureModule

/**
 * Koin modules of the app. The login data layer is faked on every target, Android included,
 * until the app is wired to the DHIS2 SDK.
 */
val appModules =
    listOf(
        commonsModule,
        loginFeatureModule,
        fakeLoginDataModule,
        splashModule,
    )
