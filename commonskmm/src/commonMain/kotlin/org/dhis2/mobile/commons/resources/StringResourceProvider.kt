package org.dhis2.mobile.commons.resources

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class StringResourceProvider {
    suspend fun provideString(
        resource: StringResource,
        number: Int,
    ): String = getString(resource, number)
}
