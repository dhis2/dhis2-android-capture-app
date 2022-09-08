package org.dhis2.usescases.general

import android.content.Context
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.data.server.ServerComponent

fun ActivityGlobalAbstract.wrappedContextForLanguage(
    serverComponent: ServerComponent?,
    newBaseContext: Context
): Context {
    return if (serverComponent?.getD2()?.userModule()?.blockingIsLogged() == true) {
        LocaleSelector(newBaseContext, serverComponent.getD2()).updateUiLanguage()
    } else {
        newBaseContext
    }
}
