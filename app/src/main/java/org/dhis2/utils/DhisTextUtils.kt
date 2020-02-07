package org.dhis2.utils

class DhisTextUtils {
    companion object {
        fun isEmpty(str: CharSequence?): Boolean {
            return str.isNullOrEmpty()
        }

        fun isNotEmpty(str: CharSequence?): Boolean {
            return str != null && str.isNotEmpty()
        }

        fun isNullOrEmpty(str: CharSequence?): Boolean {
            return str.isNullOrEmpty()
        }
    }
}
