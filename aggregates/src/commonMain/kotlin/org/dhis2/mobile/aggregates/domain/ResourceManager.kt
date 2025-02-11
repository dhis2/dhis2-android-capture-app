package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.resources.Res
import org.dhis2.mobile.aggregates.resources.default_column_label
import org.jetbrains.compose.resources.getString

internal class ResourceManager {
    suspend fun defaultHeaderLabel() = getString(Res.string.default_column_label)
}
