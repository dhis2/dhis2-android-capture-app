package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.plugin.domain.model.PluginFieldMetadata
import org.dhis2.mobile.plugin.domain.model.PluginOption
import org.dhis2.mobile.plugin.domain.model.PluginOptionSet
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import kotlin.coroutines.cancellation.CancellationException

/** The metadata of the fields a form field plugin is given, read from the SDK's local database. */
class PluginFieldMetadataRepository(
    private val d2: D2,
    private val dispatcher: Dispatcher,
) {
    /** Metadata of each data element or tracked entity attribute in [uids] that exists locally. */
    suspend fun metadata(uids: List<String>): Map<String, PluginFieldMetadata> =
        withContext(dispatcher.io) {
            try {
                uids.mapNotNull { uid -> fieldMetadata(uid)?.let { uid to it } }.toMap()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                emptyMap()
            }
        }

    private fun fieldMetadata(uid: String): PluginFieldMetadata? {
        d2
            .dataElementModule()
            .dataElements()
            .uid(uid)
            .blockingGet()
            ?.let { element ->
                return PluginFieldMetadata(
                    name = element.displayName() ?: uid,
                    shortName = element.displayShortName() ?: element.displayName() ?: uid,
                    formName = element.displayFormName() ?: element.displayName() ?: uid,
                    code = element.code(),
                    valueType = element.valueType()?.name ?: "TEXT",
                    description = element.displayDescription(),
                    optionSet = element.optionSet()?.uid()?.let(::optionSet),
                )
            }
        return d2
            .trackedEntityModule()
            .trackedEntityAttributes()
            .uid(uid)
            .blockingGet()
            ?.let { attribute ->
                PluginFieldMetadata(
                    name = attribute.displayName() ?: uid,
                    shortName = attribute.displayShortName() ?: attribute.displayName() ?: uid,
                    formName = attribute.displayFormName() ?: attribute.displayName() ?: uid,
                    code = attribute.code(),
                    valueType = attribute.valueType()?.name ?: "TEXT",
                    description = attribute.displayDescription(),
                    optionSet = attribute.optionSet()?.uid()?.let(::optionSet),
                )
            }
    }

    private fun optionSet(uid: String): PluginOptionSet? {
        val optionSet =
            d2
                .optionModule()
                .optionSets()
                .uid(uid)
                .blockingGet() ?: return null
        val options =
            d2
                .optionModule()
                .options()
                .byOptionSetUid()
                .eq(uid)
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()
                .mapNotNull { option ->
                    val code = option.code() ?: return@mapNotNull null
                    PluginOption(option.uid(), code, option.displayName() ?: code)
                }
        return PluginOptionSet(optionSet.code(), optionSet.displayName() ?: uid, options)
    }
}
