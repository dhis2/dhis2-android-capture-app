package org.dhis2.data.server

import org.dhis2.R
import javax.inject.Inject

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
