package org.dhis2.utils

import android.util.Patterns
import java.util.regex.Pattern
import org.dhis2.R
import org.hisp.dhis.android.core.common.ValueType

const val urlStringPattern =
    "^(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)[a-z0-9]" +
        "+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?$"
val urlPattern: Pattern = Pattern.compile(urlStringPattern)

class ValidationUtils {

    companion object {

        @JvmStatic
        fun validate(valueType: ValueType, value: String?): String? {
            return value?.let {
                if (value == "-") {
                    "0"
                } else {
                    when (valueType) {
                        ValueType.INTEGER,
                        ValueType.INTEGER_NEGATIVE,
                        ValueType.INTEGER_POSITIVE,
                        ValueType.INTEGER_ZERO_OR_POSITIVE,
                        ValueType.PERCENTAGE -> it.toInt().toString()
                        ValueType.UNIT_INTERVAL -> it.toFloat().toString()
                        ValueType.NUMBER ->
                            if (it.toDouble() % 1.toDouble() == 0.toDouble()) {
                                it.toInt().toString()
                            } else {
                                it.toDouble().toString()
                            }
                        else -> it
                    }
                }
            }
        }

        @JvmStatic
        fun formatValidation(value: String?, valueType: ValueType): Pair<Boolean, Int> {
            return value?.let {
                when (valueType) {
                    ValueType.PHONE_NUMBER -> Pair(
                        Patterns.PHONE.matcher(value).matches(),
                        R.string.invalid_phone_number
                    )
                    ValueType.EMAIL -> Pair(
                        Patterns.EMAIL_ADDRESS.matcher(value).matches(),
                        R.string.invalid_email
                    )
                    ValueType.INTEGER_NEGATIVE -> Pair(
                        value.toInt() < 0,
                        R.string.invalid_negative_number
                    )
                    ValueType.INTEGER_ZERO_OR_POSITIVE -> Pair(
                        value.toInt() >= 0,
                        R.string.invalid_possitive_zero
                    )
                    ValueType.INTEGER_POSITIVE -> Pair(
                        value.toInt() > 0,
                        R.string.invalid_possitive
                    )
                    ValueType.UNIT_INTERVAL -> Pair(
                        value.toInt() in 0..1,
                        R.string.invalid_interval
                    )
                    ValueType.PERCENTAGE -> Pair(
                        value.toInt() in 0..100,
                        R.string.invalid_percentage
                    )
                    ValueType.URL -> Pair(
                        urlPattern.matcher(value).matches(),
                        R.string.validation_url
                    )
                    else -> Pair(true, -1)
                }
            } ?: Pair(true, -1)
        }
    }
}
