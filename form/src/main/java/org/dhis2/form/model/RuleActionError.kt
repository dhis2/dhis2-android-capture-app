package org.dhis2.form.model

import org.hisp.dhis.rules.models.RuleAction

data class RuleActionError(val action: String, val message: String) : RuleAction() {

    override fun data() = ""
}
