package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import org.dhis2.Bindings.userFriendlyValue
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.attribute.Attribute
import org.hisp.dhis.android.core.attribute.AttributeValue
import org.hisp.dhis.android.core.dataelement.DataElement

class ValuesD2Repository(private val d2: D2) : ValuesRepository {
    val dataElements: List<DataElement> = d2.dataElementModule().dataElements().get().blockingGet()

    override fun getByEvent(eventUid: String): List<Value> {
        val feedbackOrderAttributeCode = "FeedbackOrder"
        val feedbackTextAttributeCode = "FeedbackText"

        val teiDataValues =
            d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid).get()
                .blockingGet()

        val dataElements =
            d2.dataElementModule().dataElements().get().blockingGet()

        val dataElementsWithFeedbackOrder =
            getDataElementsWithFeedbackOrder(dataElements, feedbackOrderAttributeCode)

        return teiDataValues.filter { dataElementsWithFeedbackOrder.contains(it.dataElement()) }
            .map { teiValue ->
                val dataElement =
                    dataElements.first { it.uid() == teiValue.dataElement() }
                val colorByLegend = getColorByLegend(teiValue.value()!!, teiValue.dataElement()!!)
                val deAttributeValues = getDataElementAttributeValues(teiValue.dataElement()!!)

                val deFeedbackHelp = deAttributeValues.firstOrNull {
                    it.attribute().code() == feedbackTextAttributeCode
                }

                val deFeedbackOrder = deAttributeValues.firstOrNull {
                    it.attribute().code() == feedbackOrderAttributeCode
                }

                val deName: String =
                    if (dataElement.formName() == null) dataElement.displayName()!! else dataElement.formName()!!

                Value(
                    teiValue.dataElement()!!,
                    deName,
                    teiValue.userFriendlyValue(d2)!!,
                    FeedbackOrder(deFeedbackOrder!!.value()),
                    colorByLegend,
                    deFeedbackHelp?.value()
                )
            }
    }

    private fun getDataElementsWithFeedbackOrder(
        dataElements: MutableList<DataElement>,
        feedbackOrderAttributeCode: String
    ): List<String> {
        return dataElements.map {
            val deAttributeValues = getDataElementAttributeValues(it.uid())

            it.toBuilder().attributeValues(deAttributeValues).build()
        }.filter {
            if (it.attributeValues() == null) false else it.attributeValues()!!
                .any { attributeValue ->
                    attributeValue.attribute().code() == feedbackOrderAttributeCode
                }
        }.map { it.uid() }
    }

    private fun getColorByLegend(value: String, dataElementUid: String): String? {
        var color: String? = null

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
                    color = legends[0].color()
                }
            }
            color
        } catch (e: Exception) {
            color
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
}