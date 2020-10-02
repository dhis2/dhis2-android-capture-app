package org.dhis2.data.forms.dataentry.validation

import org.dhis2.utils.Validator
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueType.INTEGER_NEGATIVE
import org.hisp.dhis.android.core.common.ValueType.INTEGER_POSITIVE
import org.hisp.dhis.android.core.common.ValueType.INTEGER_ZERO_OR_POSITIVE

class ValueTypeValidatorFactory {

    companion object {
        val hasMap: HashMap<ValueType, Validator> = hashMapOf(
            INTEGER_POSITIVE to PositiveIntegerValidator(),
            INTEGER_NEGATIVE to NegativeIntegerValidator(),
            INTEGER_ZERO_OR_POSITIVE to ZeroOrPositiveIntegerValidator()
        )
    }
}

fun getValidator(type: ValueType) = ValueTypeValidatorFactory.hasMap[type]
