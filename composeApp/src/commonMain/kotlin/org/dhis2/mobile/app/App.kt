package org.dhis2.mobile.app

import androidx.compose.runtime.Composable
import org.dhis2.mobile.app.di.appModules
import org.dhis2.mobile.app.navigation.AppNavigation
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = { modules(appModules) }) {
        DHIS2Theme {
            AppNavigation()
        }
    }
}
