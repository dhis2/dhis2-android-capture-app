package org.dhis2.mobile.commons.providers

sealed interface CustomLabelContext {
    data class Program(
        val programUid: String,
    ) : CustomLabelContext

    data class ProgramStage(
        val programStageUid: String,
        val programUid: String,
    ) : CustomLabelContext
}
