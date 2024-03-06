package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.R
import org.dhis2.utils.JsonCheckResult
import org.dhis2.utils.JsonChecker
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.attribute.Attribute
import org.hisp.dhis.android.core.attribute.AttributeValue
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.legendset.Legend
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import timber.log.Timber
import java.util.Locale

class ValuesD2Repository(
    private val d2: D2,
    private val context: Context
) : ValuesRepository {
    val dataElements: List<DataElement> = d2.dataElementModule().dataElements().get().blockingGet()

    override fun getByEvent(eventUid: String): List<Value> {
        val feedbackOrderAttributeCode = "FeedbackOrder"
        val feedbackTextAttributeCode = "FeedbackText"
        val hnqis2MetadataAttributeCode = "PCA Metadata"
        val failLegendSuffix = "FAIL"

        val teiDataValues =
            d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid)
                .get().blockingGet().filter { it.value() != null }

        val dataElementsWithFeedbackOrder =
            getDataElementsWithFeedbackOrder(feedbackOrderAttributeCode)

        return teiDataValues.filter { dataElementsWithFeedbackOrder.keys.contains(it.dataElement()) }
            .map { teiValue ->
                val dataElement =
                    dataElements.first { it.uid() == teiValue.dataElement() }
                val assignedLegend =
                    getAssignedLegend(teiValue.value() ?: "", teiValue.dataElement()!!)
                val deAttributeValues = getDataElementAttributeValues(teiValue.dataElement()!!)

                val deFeedbackHelpRaw = deAttributeValues.firstOrNull {
                    it.attribute().code() == feedbackTextAttributeCode
                }

                val deFeedbackHelp = parseFeedbackHelp(deFeedbackHelpRaw, dataElement.uid())

                val deName: String =
                    if (dataElement.displayFormName() == null) dataElement.displayName()!! else dataElement.displayFormName()!!

                val deFeedbackOrder = deAttributeValues.firstOrNull {
                    it.attribute().code() == feedbackOrderAttributeCode
                }

                val dataElementMetadataRaw = deAttributeValues.firstOrNull {
                    it.attribute().code() == hnqis2MetadataAttributeCode
                }

                val critical = parseCriticalMetadata(dataElementMetadataRaw, dataElement.uid())

                Value(
                    teiValue.dataElement()!!,
                    deName,
                    teiValue.userFriendlyValue(d2) ?: "",
                    FeedbackOrder(deFeedbackOrder!!.value()),
                    assignedLegend?.color(),
                    deFeedbackHelp,
                    assignedLegend?.name()?.split("_")?.last() != failLegendSuffix,
                    critical,
                    eventUid,
                    isNumeric(teiValue)
                )
            }
    }

    private fun isNumeric(teiValue: TrackedEntityDataValue): Boolean {
        val dataElement = d2.dataElementModule().dataElements()
            .uid(teiValue.dataElement())
            .blockingGet()

        dataElement.optionSet()?.let {
            return false
        } ?: return dataElement.valueType()!!.isNumeric
    }

    private fun parseFeedbackHelp(feedbackHelpRaw: AttributeValue?, dataElement: String): String? {
        if (feedbackHelpRaw == null) return null

        val malformedJsonError = context.getString(R.string.feedback_malformed_error, dataElement)

        when (JsonChecker().check(feedbackHelpRaw.value())) {
            is JsonCheckResult.Json -> {
                val gson = Gson()
                val language = getCurrentLocale()?.language

                try {
                    val type = object : TypeToken<List<MultilingualFeedback>>() {}.type
                    val multilingualFeedbackList =
                        gson.fromJson<List<MultilingualFeedback>>(feedbackHelpRaw.value(), type)

                    return if (multilingualFeedbackList.isEmpty()) {
                        null
                    } else {
                        val feedbackCurrentLocale =
                            multilingualFeedbackList.firstOrNull { f -> f.locale == language }

                        feedbackCurrentLocale?.text ?: multilingualFeedbackList[0].text
                    }
                } catch (e: Exception) {
                    return malformedJsonError
                }
            }
            is JsonCheckResult.MalformedJson -> return malformedJsonError
            is JsonCheckResult.Text -> return feedbackHelpRaw.value()
        }
    }

    private fun parseCriticalMetadata(metadataRaw: AttributeValue?, dataElement: String): Boolean {
        if (metadataRaw == null) return false
        val gson = Gson()

        return try {
            val dataElementMetadata =
                gson.fromJson(metadataRaw.value(), Hnqis2Metadata::class.java)

            dataElementMetadata.isCritical.toLowerCase(Locale.getDefault()) == "yes"
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    private fun getDataElementsWithFeedbackOrder(feedbackOrderAttributeCode: String): Map<String, String> {
        var result = mutableMapOf<String, String>()

        val select = "SELECT dataElement, value  FROM DataElementAttributeValueLink  \n" +
            "LEFT JOIN Attribute ON DataElementAttributeValueLink.Attribute = Attribute.uid\n" +
            "where Attribute.code = ?"

        d2.databaseAdapter().rawQuery(select, feedbackOrderAttributeCode)
            .use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        result[cursor.getString(0)] = cursor.getString(1)
                    }
                }
            }

        return result
    }

    private fun getAssignedLegend(value: String, dataElementUid: String): Legend? {
        var legend: Legend? = null

        return try {
            val dataElement = d2.dataElementModule().dataElements()
                .byUid().eq(dataElementUid)
                .withLegendSets()
                .one().blockingGet()

            if (dataElement != null && dataElement.valueType()!!.isNumeric &&
                dataElement.legendSets() != null && dataElement.legendSets()!!.isNotEmpty()
            ) {
                val legendSet = dataElement.legendSets()!![0]
                val legends = d2.legendSetModule().legends().byStartValue().smallerThan(
                    java.lang.Double.valueOf(value)
                ).byEndValue().biggerThan(java.lang.Double.valueOf(value))
                    .byLegendSet().eq(legendSet.uid()).blockingGet()

                if (legends.size > 0) {
                    legend = legends[0]
                }
            }
            legend
        } catch (e: Exception) {
            legend
        }
    }

    private fun getDataElementAttributeValues(dataElementUid: String): List<AttributeValue> {
        val attributes = getDataElementAttributes()

        val attributesValues = mutableListOf<AttributeValue>()
        val attributeValueSelect = "SELECT attribute, value FROM DataElementAttributeValueLink " +
            "Where dataElement = ?"

        d2.databaseAdapter().rawQuery(attributeValueSelect, dataElementUid)
            .use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val attribute = attributes.first { it.uid() == cursor.getString(0) }
                        attributesValues.add(
                            AttributeValue.builder()
                                .attribute(attribute)
                                .value(cursor.getString(1)).build()
                        )
                    }
                }
            }


        return attributesValues;
    }

    private fun getDataElementAttributes(): List<Attribute> {
        val attributes = mutableListOf<Attribute>()
        val attributeSelect = "SELECT * FROM Attribute WHERE dataElementAttribute = 1";

        d2.databaseAdapter().rawQuery(attributeSelect).use { cursor ->
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    attributes.add(Attribute.create(cursor))
                }
            }
        }

        return attributes
    }

    private fun getCurrentLocale(): Locale? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }
}