package org.dhis2.usescases.about.ui.state

import org.dhis2.usescases.about.model.AboutInfo

/**
 * @param info install facts, null until they have been read. The rest of the screen is static, so
 * only the "This install" values wait for it.
 */
data class AboutUiState(
    val info: AboutInfo? = null,
)
