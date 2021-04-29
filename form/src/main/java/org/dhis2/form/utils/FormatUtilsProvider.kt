package org.dhis2.form.utils

import java.util.Date

interface FormatUtilsProvider {

    fun stringToDate(string: String): Date
}