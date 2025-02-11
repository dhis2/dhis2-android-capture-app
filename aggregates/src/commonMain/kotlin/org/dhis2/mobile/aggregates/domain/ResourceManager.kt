package org.dhis2.mobile.aggregates.domain

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

internal class ResourceManager {
    suspend fun get(stringResources: StringResource) = getString(stringResources)
    suspend inline fun <reified T> get(resource: T) = when (T::class) {
        StringResource::class -> getString(resource as StringResource)
        else -> throw IllegalArgumentException("Unsupported resource type")
    }
}
