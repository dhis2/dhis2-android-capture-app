package org.dhis2.mobile.extension.web.customform

import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.extension.host.model.DataSetInstanceRef
import org.hisp.dhis.android.core.D2
import kotlin.coroutines.cancellation.CancellationException

/**
 * What a custom form reads: only its own data set instance. The HTML comes from `extension-host`
 * and values are written through the data set screen's save, so this repository never writes.
 */
class CustomFormDataRepository(
    private val d2: D2,
    private val dispatcher: Dispatcher,
) {
    /** What [html] needs to render for [instance]. */
    suspend fun formData(
        instance: DataSetInstanceRef,
        html: String,
    ): Result<CustomFormData> =
        io {
            val dataElements = dataElements(instance.dataSetUid)
            val values =
                d2
                    .dataValueModule()
                    .dataValues()
                    .byPeriod()
                    .eq(instance.periodId)
                    .byOrganisationUnitUid()
                    .eq(instance.organisationUnitUid)
                    .byAttributeOptionComboUid()
                    .eq(instance.attributeOptionComboUid)
                    .byDataElementUid()
                    .`in`(dataElements.map { it.uid })
                    .byDeleted()
                    .isFalse
                    .blockingGet()
                    .mapNotNull { value ->
                        val dataElement = value.dataElement() ?: return@mapNotNull null
                        val categoryOptionCombo = value.categoryOptionCombo() ?: return@mapNotNull null
                        value.value()?.let { CustomFormFieldId(dataElement, categoryOptionCombo) to it }
                    }.toMap()
            CustomFormData(html, dataElements, values, evaluateIndicators(instance))
        }

    /** The data set's indicators for [instance], recalculated; asked again after each save. */
    suspend fun indicators(instance: DataSetInstanceRef): Result<Map<String, String>> = io { evaluateIndicators(instance) }

    private fun dataElements(dataSetUid: String): List<CustomFormDataElement> {
        val uids =
            d2
                .dataSetModule()
                .dataSets()
                .withDataSetElements()
                .uid(dataSetUid)
                .blockingGet()
                ?.dataSetElements()
                .orEmpty()
                .map { it.dataElement().uid() }
        return d2
            .dataElementModule()
            .dataElements()
            .byUid()
            .`in`(uids)
            .blockingGet()
            .map { dataElement ->
                val options =
                    dataElement.optionSetUid()?.let { optionSetUid ->
                        d2
                            .optionModule()
                            .options()
                            .byOptionSetUid()
                            .eq(optionSetUid)
                            .blockingGet()
                            .sortedBy { it.sortOrder() }
                            .map { CustomFormOption(it.code().orEmpty(), it.displayName() ?: it.name().orEmpty()) }
                    }
                CustomFormDataElement(
                    uid = dataElement.uid(),
                    name = dataElement.displayFormName() ?: dataElement.displayName() ?: dataElement.uid(),
                    valueType = dataElement.valueType()?.name ?: "TEXT",
                    options = options.orEmpty(),
                )
            }
    }

    private fun evaluateIndicators(instance: DataSetInstanceRef): Map<String, String> =
        d2
            .indicatorModule()
            .indicators()
            .byDataSetUid(instance.dataSetUid)
            .blockingGet()
            .associate { indicator ->
                indicator.uid() to
                    d2
                        .indicatorModule()
                        .dataSetIndicatorEngine()
                        .blockingEvaluate(
                            indicator.uid(),
                            instance.dataSetUid,
                            instance.periodId,
                            instance.organisationUnitUid,
                            instance.attributeOptionComboUid,
                        ).toString()
            }

    private suspend fun <T> io(block: suspend () -> T): Result<T> =
        withContext(dispatcher.io) {
            try {
                Result.success(block())
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
}
