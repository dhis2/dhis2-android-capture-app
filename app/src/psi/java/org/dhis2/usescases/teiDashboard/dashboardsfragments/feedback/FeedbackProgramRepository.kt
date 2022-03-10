package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import org.dhis2.core.functional.Either
import org.hisp.dhis.android.core.D2
import java.lang.Exception

interface FeedbackProgramRepository {
    fun get(uid: String): Either<FeedbackProgramFailure, FeedbackProgram>
}

sealed class FeedbackProgramFailure {
    data class ConfigurationError(val programUid: String) : FeedbackProgramFailure()
    data class UnexpectedError(val exception: Exception) : FeedbackProgramFailure()
}

class D2FeedbackProgramRepository(private val d2: D2) : FeedbackProgramRepository {
    override fun get(uid: String): Either<FeedbackProgramFailure, FeedbackProgram> {
        return try {
            val programTypeText = getProgramTypeAttValue(uid)

            val programType =
                ProgramType.values().find { programTypeText.toUpperCase() == it.name }

            if (programType != null) {
                Either.Right(
                    FeedbackProgram(
                        uid,
                        programType
                    )
                )
            } else {
                Either.Left(FeedbackProgramFailure.ConfigurationError(uid))
            }
        } catch (e: Exception) {
            Either.Left(FeedbackProgramFailure.UnexpectedError(e))
        }
    }

    private fun getProgramTypeAttValue(programUid: String): String {

        val attributeUid = getProgramTypeAttributeUid(d2)

        if (attributeUid.isNotBlank()) {
            val attributeValueSelect =
                "SELECT value FROM ProgramAttributeValueLink \n" +
                    "WHERE attribute = ? AND program = ?"

            d2.databaseAdapter().rawQuery(attributeValueSelect, attributeUid, programUid)
                .use { cursor ->
                    if (cursor != null && cursor.moveToFirst() && cursor.count > 0) {
                        return cursor.getString(0)
                    }
                }
        }

        return ""
    }

    private fun getProgramTypeAttributeUid(d2: D2): String {
        val attributeCode = "PROGRAM_TYPE"
        val attributeSelect = "SELECT uid FROM Attribute WHERE code = '${attributeCode}'";

        d2.databaseAdapter().rawQuery(attributeSelect).use { cursor ->
            if (cursor != null && cursor.moveToFirst() && cursor.count > 0) {
                return cursor.getString(0)
            }
        }

        return ""
    }
}