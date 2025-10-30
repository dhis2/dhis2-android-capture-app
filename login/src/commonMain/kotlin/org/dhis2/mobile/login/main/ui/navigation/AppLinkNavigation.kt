package org.dhis2.mobile.login.main.ui.navigation

import kotlinx.coroutines.flow.MutableSharedFlow

class AppLinkNavigation {
    val appLink = MutableSharedFlow<String>(replay = 1)
}
