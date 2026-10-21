package org.dhis2.mobile.plugin.data

import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.validation.validators.FieldMaskValidator
import org.dhis2.mobile.plugin.domain.model.CustomFormContext
import org.dhis2.mobile.plugin.domain.model.CustomFormData
import org.dhis2.mobile.plugin.domain.model.CustomFormDataElement
import org.dhis2.mobile.plugin.domain.model.CustomFormFieldId
import org.dhis2.mobile.plugin.domain.model.CustomFormOption
import org.dhis2.mobile.plugin.domain.model.parseCustomForms
import org.hisp.dhis.android.core.D2
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import org.hisp.dhis.android.core.arch.helpers.Result as SdkResult

private const val HTML_EXTENSION = ".html"

/**
 * Data set custom forms: the HTML the web Data Entry app renders, and the data they read and write.
 *
 * The SDK does not sync custom forms, so [refresh] downloads them into [directory] while online and
 * the cached copy is used offline. Values are read and written through the SDK as the native data
 * set table does, so they are validated the same way and synced with the rest of the data.
 */
class CustomFormRepository(
    private val d2: D2,
    private val dispatcher: Dispatcher,
    private val directory: File,
) {
    /** Downloads the custom form of every data set the user can capture, replacing the cached ones. */
    suspend fun refresh(): Result<Unit> =
        io {
            val dataSetUids =
                d2
                    .dataSetModule()
                    .dataSets()
                    .blockingGetUids()
            val response =
                d2.httpServiceClient().get<String> {
                    url("dataSets")
                    parameters {
                        attribute("fields", "id,formType,dataEntryForm[htmlCode]")
                        attribute("filter", "id:in:[${dataSetUids.joinToString(",")}]")
                        attribute("paging", "false")
                    }
                }
            val forms = parseCustomForms(response)
            directory.mkdirs()
            directory.listFiles()?.filter { it.name.removeSuffix(HTML_EXTENSION) !in forms }?.forEach(File::delete)
            forms.forEach { (dataSetUid, html) -> File(directory, dataSetUid + HTML_EXTENSION).writeText(html) }
        }

    /** The cached custom form HTML of [dataSetUid], or null if the data set has none. */
    suspend fun html(dataSetUid: String): String? =
        withContext(dispatcher.io) {
            File(directory, dataSetUid + HTML_EXTENSION).takeIf(File::isFile)?.readText()
        }

    /** What [html] needs to render for the instance in [context]. */
    suspend fun formData(
        context: CustomFormContext,
        html: String,
    ): Result<CustomFormData> =
        io {
            val dataElements = dataElements(context.dataSetUid)
            val values =
                d2
                    .dataValueModule()
                    .dataValues()
                    .byPeriod()
                    .eq(context.periodId)
                    .byOrganisationUnitUid()
                    .eq(context.orgUnitUid)
                    .byAttributeOptionComboUid()
                    .eq(context.attributeOptionComboUid)
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
            CustomFormData(html, dataElements, values, indicators(context))
        }

    /**
     * Saves [value] in [field], or clears it when null, validated like the native data set table:
     * by value type, then by field mask. Returns the data set's indicators recalculated.
     */
    suspend fun save(
        context: CustomFormContext,
        field: CustomFormFieldId,
        value: String?,
    ): Result<Map<String, String>> =
        io {
            val repository =
                d2
                    .dataValueModule()
                    .dataValues()
                    .value(
                        period = context.periodId,
                        organisationUnit = context.orgUnitUid,
                        dataElement = field.dataElement,
                        categoryOptionCombo = field.categoryOptionCombo,
                        attributeOptionCombo = context.attributeOptionComboUid,
                        sourceDataSet = context.dataSetUid,
                    )
            if (value == null) {
                repository.blockingDeleteIfExist()
            } else {
                val dataElement =
                    d2
                        .dataElementModule()
                        .dataElements()
                        .uid(field.dataElement)
                        .blockingGet()
                val validValue =
                    dataElement
                        ?.valueType()
                        ?.validator
                        ?.validate(value)
                        ?.getOrThrow() ?: value
                dataElement?.fieldMask()?.let { mask ->
                    val validation = FieldMaskValidator(mask).validate(value)
                    if (validation is SdkResult.Failure) throw validation.failure
                }
                repository.blockingSet(validValue.toString())
            }
            indicators(context)
        }

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

    private fun indicators(context: CustomFormContext): Map<String, String> =
        d2
            .indicatorModule()
            .indicators()
            .byDataSetUid(context.dataSetUid)
            .blockingGet()
            .associate { indicator ->
                indicator.uid() to
                    d2
                        .indicatorModule()
                        .dataSetIndicatorEngine()
                        .blockingEvaluate(
                            indicator.uid(),
                            context.dataSetUid,
                            context.periodId,
                            context.orgUnitUid,
                            context.attributeOptionComboUid,
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
