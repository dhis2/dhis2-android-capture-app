package org.dhis2.mobile.aggregates.domain

import dhis2_android_capture_app.aggregates.generated.resources.Res
import dhis2_android_capture_app.aggregates.generated.resources.default_column_label
import org.jetbrains.compose.resources.getString

internal class ResourceManager {
    suspend fun defaultHeaderLabel() = getString(Res.string.default_column_label)
}
