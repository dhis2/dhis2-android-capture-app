package org.dhis2.Bindings

import org.dhis2.form.utils.FormatUtilsProvider

class FormatUtilsProviderImpl : FormatUtilsProvider {

    override fun stringToDate(string: String) = string.toDate()
}
