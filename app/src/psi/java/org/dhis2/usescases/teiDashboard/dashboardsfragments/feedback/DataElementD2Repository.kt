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
            getDataElementsWithFeedbackOrder(feedbackOrderAttributeCode)

        val d2ProgramStageDataElements =
            d2.programModule().programStageDataElements().byProgramStage().eq(programStageUid)
                .blockingGet()

        val d2DataElements = d2ProgramStageDataElements.map { it.dataElement() }
            .filter { dataElementsWithFeedbackOrder.any { entry -> entry.key == it!!.uid() } }

        val dataElements = d2DataElements.map { d2DataElement ->
            val dataElementUid = d2DataElement!!.uid()

            val feedbackOrder = dataElementsWithFeedbackOrder[dataElementUid]

            if (feedbackOrder != null) {
                val feedbackOrder = try {
                    FeedbackOrder(feedbackOrder)
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
}