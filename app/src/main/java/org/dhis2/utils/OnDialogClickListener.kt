package org.dhis2.utils

import androidx.appcompat.app.AlertDialog

/**
 * QUADRAM. Created by ppajuelo on 09/10/2018.
 */

interface OnDialogClickListener {
    fun onPossitiveClick(alertDialog: AlertDialog)
    fun onNegativeClick(alertDialog: AlertDialog)
}
