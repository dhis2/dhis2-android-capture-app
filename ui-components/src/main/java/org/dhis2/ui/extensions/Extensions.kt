package org.dhis2.ui.extensions

import java.text.DecimalFormat

fun Float.decimalFormat(pattern: String = "*0.##"): String {
    return DecimalFormat(pattern).format(this)
}
