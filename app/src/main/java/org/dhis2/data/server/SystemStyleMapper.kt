package org.dhis2.data.server

import javax.inject.Inject
import org.dhis2.R

class SystemStyleMapper @Inject constructor() {

    private val serverGreenTheme = "green"
    private val serverIndiaTheme = "india"
    private val serverMyanmarTheme = "myanmar"

    fun map(serverStyle: String?): Int {
        return when {
            serverStyle?.contains(serverGreenTheme) == true -> R.style.GreenTheme
            serverStyle?.contains(serverIndiaTheme) == true -> R.style.OrangeTheme
            serverStyle?.contains(serverMyanmarTheme) == true -> R.style.RedTheme
            else -> R.style.AppTheme
        }
    }
}
