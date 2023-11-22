package org.dhis2.utils.extension

import android.app.Activity
import android.content.Intent
import android.os.Bundle

inline fun <reified T> Activity.navigateTo(
    finishCurrent: Boolean = false,
    noinline block: (Bundle.() -> Unit)? = null,
    flagsToApply: Int? = null,
) {
    Intent(this, T::class.java).apply {
        block?.apply {
            putExtra(T::class.java.name, Bundle().also(this))
        }
        flagsToApply?.let {
            flags = flagsToApply
        }
        startActivity(this)
        if (finishCurrent) {
            finish()
        }
    }
}

fun Activity.share(messageToShare: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, messageToShare)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}
