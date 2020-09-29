package org.dhis2.Bindings

import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan

fun List<Pair<String, String>>.toSpannableString(): SpannableStringBuilder {
    val stringBuilder = SpannableStringBuilder()
    this.forEach { nameValuePair ->
        if (nameValuePair.second != "-") {
            val value = SpannableString(nameValuePair.second)
            val colorToUse = when {
                this.indexOf(nameValuePair) % 2 == 0 -> Color.parseColor("#8A333333")
                else -> Color.parseColor("#61333333")
            }
            value.setSpan(
                ForegroundColorSpan(colorToUse),
                0,
                value.length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            stringBuilder.append(value)
            if (this.indexOf(nameValuePair) != this.size - 1) {
                stringBuilder.append(" ")
            }
        }
    }
    return stringBuilder
}
