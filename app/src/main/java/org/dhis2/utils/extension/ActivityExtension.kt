package org.dhis2.utils.extension

import android.app.Activity
import android.content.Intent
import android.os.Bundle

inline fun <reified T> Activity.navigateTo(
    finishCurrent: Boolean,
    noinline block: (Bundle.() -> Unit)? = null
) {
    Intent(this, T::class.java).apply {
        block?.apply {
            putExtra(T::class.java.name, Bundle().also(this))
        }
        startActivity(this)
        if (finishCurrent) {
            finish()
        }
    }
}
