package org.dhis2.usescases.general

import android.content.Context
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.data.server.ServerComponent

fun ActivityGlobalAbstract.wrappedContextForLanguage(
    serverComponent: ServerComponent?,
    newBaseContext: Context
): Context = serverComponent?.getD2()?.let {
    LocaleSelector(newBaseContext, it).updateUiLanguage()
} ?: newBaseContext
