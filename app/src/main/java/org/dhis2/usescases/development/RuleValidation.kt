package org.dhis2.usescases.development

import org.dhis2.R
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.rules.models.Rule

data class RuleValidation(
    val rule: Rule? = null,
    val program: Program,
    val externalLink: String? = null,
    val conditionError: String? = null,
    val actionsError: List<String>? = null
) {
    fun uid() = rule?.uid() ?: program.uid()
    fun layout() =
        if (rule == null) R.layout.rule_validation_section_item else R.layout.rule_validation_item

    fun hasError() = conditionError != null || actionsError != null
    fun errors(): String {
        return mutableListOf<String>().apply {
            conditionError?.let { add(it) }
            actionsError?.let { addAll(it) }
        }.takeIf { it.isNotEmpty() }?.joinToString(separator = "\n- ", prefix = "- ")
            ?: ""
    }

    fun title() = rule?.name() ?: program.displayName()
}
