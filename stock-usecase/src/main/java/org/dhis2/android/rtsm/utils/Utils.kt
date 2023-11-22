package org.dhis2.android.rtsm.utils

import android.text.TextUtils
import timber.log.Timber
import java.util.regex.Pattern

class Utils {
    companion object {
        @JvmStatic
        fun isValidStockOnHand(value: String?): Boolean {
            if (value == null || TextUtils.isEmpty(value)) {
                Timber.w("Stock on hand value received is empty")
                return false
            }

            return try {
                value.toLong() >= 0
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Checks if a string is a signed numeric
         *
         * @param s The string to check
         * @return A Boolean denoting the outcome of the operation
         */
        @JvmStatic
        fun isSignedNumeric(s: String) = Pattern.compile("-?\\d+").matcher(s).matches()

        /**
         * Removes extraneous space from signed numbers
         *
         * For example,
         *
         * cleanUpSignedNumber("- 12") = -12
         *
         * @return The clean number string
         */
        fun cleanUpSignedNumber(s: String): String {
            val signedNumberWithSpace = Pattern.compile("-?\\s?\\d+").matcher(s).matches()
            return if (signedNumberWithSpace) {
                Timber.w("Extraneous space after negative sign removed from speech input: %s", s)
                s.replace("\\s".toRegex(), "")
            } else {
                s
            }
        }

        @JvmStatic
        fun capitalizeText(text: String): String {
            return text.lowercase()
                .replaceFirstChar {
                    it.uppercase()
                }
        }
    }
}
