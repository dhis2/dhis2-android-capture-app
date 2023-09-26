package org.dhis2.android.rtsm.ui.base

import org.hisp.dhis.rules.models.RuleEffect

interface OnQuantityValidated {
    fun validationCompleted(ruleEffects: List<RuleEffect>)
}
