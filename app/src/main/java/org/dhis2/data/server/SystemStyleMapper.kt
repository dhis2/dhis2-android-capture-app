package org.dhis2.data.server

import org.dhis2.R

object SystemStyleMapper {
    private const val SERVER_GREEN_THEME = "green"
    private const val SERVER_INDIA_THEME = "india"
    private const val SERVER_MYANMAR_THEME = "myanmar"

    operator fun invoke(serverStyle: String?): Int =
        when {
            serverStyle?.contains(SERVER_GREEN_THEME) == true -> R.style.GreenTheme
            serverStyle?.contains(SERVER_INDIA_THEME) == true -> R.style.OrangeTheme
            serverStyle?.contains(SERVER_MYANMAR_THEME) == true -> R.style.RedTheme
            else -> R.style.AppTheme
        }
}
