package org.dhis2.usescases.development

import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.rules.models.Rule

data class ProgramRuleValidation(
    val programUid: String,
    val programName: String,
    val metadataIconData: MetadataIconData,
    val validations: List<RuleValidation>
)

data class RuleValidation(
    val rule: Rule? = null,
    val externalLink: String? = null,
    val conditionError: String? = null,
    val actionsError: List<String>? = null
) {
    fun uid() = rule?.uid()
    fun hasError() = conditionError != null || actionsError != null
    fun errors(): List<String> {
        return mutableListOf<String>().apply {
            conditionError?.let { add(it) }
            actionsError?.let { addAll(it) }
        }
    }

    fun title() = rule?.name() ?: rule?.uid() ?: "-"
}
