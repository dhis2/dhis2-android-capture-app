package org.dhis2.commons.sync

fun interface OnDismissListener {
    fun onDismiss(hasChanged: Boolean)
}
