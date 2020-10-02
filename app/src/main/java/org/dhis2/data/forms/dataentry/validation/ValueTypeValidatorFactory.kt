package org.dhis2.data.forms.dataentry.validation

import org.dhis2.utils.Validator
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueType.INTEGER_POSITIVE

private val hasMap: HashMap<ValueType, Validator> = hashMapOf(
    INTEGER_POSITIVE to PositiveIntegerValidator()
)

fun getValidator(type: ValueType) = hasMap[type]
