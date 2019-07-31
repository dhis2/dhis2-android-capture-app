package org.dhis2.utils

import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.hisp.dhis.rules.models.RuleActionShowError

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

interface RulesActionCallbacks {

    fun setCalculatedValue(calculatedValueVariable: String, value: String)

    fun setShowError(showError: RuleActionShowError, model: FieldViewModel?)

    fun unsupportedRuleAction()

    fun save(uid: String, value: String?)

    fun setDisplayKeyValue(label: String, value: String)

    fun setHideSection(sectionUid: String)

    fun setMessageOnComplete(content: String, canComplete: Boolean)

    fun setHideProgramStage(programStageUid: String)

    fun setOptionToHide(optionUid: String)

    fun setOptionGroupToHide(optionGroupUid: String, toHide: Boolean, field: String = "")
}
