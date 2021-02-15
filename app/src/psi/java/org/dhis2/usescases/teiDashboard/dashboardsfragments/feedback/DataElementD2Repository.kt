package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import org.dhis2.core.functional.Either
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.attribute.Attribute
import org.hisp.dhis.android.core.attribute.AttributeValue

class DataElementD2Repository(private val d2: D2) : DataElementRepository {

    override fun getWithFeedbackOrderByProgramStage(programStageUid: String): Either<List<Validation.DataElementError>, List<DataElement>> {
        val validations = mutableListOf<Validation.DataElementError>()
        val feedbackOrderAttributeCode = "FeedbackOrder"

        val dataElementsWithFeedbackOrder =
            getDataElementsWithFeedbackOrder(
                d2.dataElementModule().dataElements().get().blockingGet(),
                feedbackOrderAttributeCode
            )

        val d2ProgramStageDataElements =
            d2.programModule().programStageDataElements().byProgramStage().eq(programStageUid)
                .blockingGet()

        val d2DataElements = d2ProgramStageDataElements.map { it.dataElement() }
            .filter { dataElementsWithFeedbackOrder.any { uid -> uid == it!!.uid() } }

        val dataElements = d2DataElements.map { d2DataElement ->
            val dataElementUid = d2DataElement!!.uid()
            val deAttributeValues = getDataElementAttributeValues(dataElementUid)

            val feedbackOrderAttValue = deAttributeValues.firstOrNull {
                it.attribute().code() == feedbackOrderAttributeCode
            }

            if (feedbackOrderAttValue != null) {
                val feedbackOrder = try {
                    FeedbackOrder(feedbackOrderAttValue.value())
                } catch (e: Exception) {
                    validations.add(
                        Validation.DataElementError(dataElementUid, FeedbackOrderInvalid)
                    )
                    null
                }

                DataElement(dataElementUid, feedbackOrder)
            } else {
                DataElement(dataElementUid, null)
            }
        }

        return if (validations.isNotEmpty()) {
            Either.Left(validations)
        } else {
            Either.Right(dataElements)
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

    private fun getDataElementsWithFeedbackOrder(
        dataElements: List<org.hisp.dhis.android.core.dataelement.DataElement>,
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
}