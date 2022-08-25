package org.dhis2.android.rtsm.services.rules

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
    fun hasError() = conditionError != null || actionsError != null
    fun errors(): List<String> {
        return mutableListOf<String>().apply {
            conditionError?.let { add(it) }
            actionsError?.let { addAll(it) }
        }
    }

    fun title() = rule?.name() ?: program.displayName()
}
