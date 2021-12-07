package org.dhis2.form.data

import org.hisp.dhis.rules.models.RuleEffect

interface RuleEngineRepository {
    fun calculate(): List<RuleEffect>
}
