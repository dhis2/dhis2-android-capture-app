package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import org.dhis2.Bindings.userFriendlyValue
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.attribute.Attribute
import org.hisp.dhis.android.core.attribute.AttributeValue
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.legendset.Legend

class ValuesD2Repository(private val d2: D2) : ValuesRepository {
    val dataElements: List<DataElement> = d2.dataElementModule().dataElements().get().blockingGet()

    override fun getByEvent(eventUid: String): List<Value> {
        val feedbackOrderAttributeCode = "FeedbackOrder"
        val feedbackTextAttributeCode = "FeedbackText"
        val failLegendSuffix = "FAIL"

        val teiDataValues =
            d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid)
                .get().blockingGet()

        val dataElementsWithFeedbackOrder =
            getDataElementsWithFeedbackOrder(dataElements, feedbackOrderAttributeCode)

        val dataElementsWithMandatory =
            getDataElementsWithMandatoryFilter(eventUid, dataElementsWithFeedbackOrder)

        return teiDataValues.filter { dataElementsWithFeedbackOrder.contains(it.dataElement()) }
            .map { teiValue ->
                val dataElement =
                    dataElements.first { it.uid() == teiValue.dataElement() }
                val assignedLegend = getAssignedLegend(teiValue.value()!!, teiValue.dataElement()!!)
                val deAttributeValues = getDataElementAttributeValues(teiValue.dataElement()!!)

                val deFeedbackHelp = deAttributeValues.firstOrNull {
                    it.attribute().code() == feedbackTextAttributeCode
                }

                val deFeedbackOrder = deAttributeValues.firstOrNull {
                    it.attribute().code() == feedbackOrderAttributeCode
                }

                val deName: String =
                    if (dataElement.displayFormName() == null) dataElement.displayName()!! else dataElement.displayFormName()!!

                Value(
                    teiValue.dataElement()!!,
                    deName,
                    teiValue.userFriendlyValue(d2)!!,
                    FeedbackOrder(deFeedbackOrder!!.value()),
                    assignedLegend?.color(),
                    deFeedbackHelp?.value(),
                    assignedLegend?.name()?.split("_")?.last() != failLegendSuffix,
                    dataElementsWithMandatory.contains(teiValue.dataElement()!!),
                    eventUid
                )
            }
    }

    private fun getDataElementsWithFeedbackOrder(
        dataElements: List<DataElement>,
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

    private fun getDataElementsWithMandatoryFilter(
        eventUid: String,
        dataElementsWithFeedbackOrder: List<String>
    ): List<String> {
        val event = d2.eventModule().events().byUid().eq(eventUid)
            .one().blockingGet()

        val stageDataElements = d2.programModule().programStageDataElements()
            .byProgramStage().eq(event.programStage()).byCompulsory().eq(true)
            .blockingGet()

        return stageDataElements.map { programStageDE ->
            d2.dataElementModule().dataElements()
                .uid(programStageDE.dataElement()?.uid()).blockingGet().uid()
        }.filter { deUid ->
            dataElementsWithFeedbackOrder.contains(deUid)
        }
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
}