package org.dhis2.commons.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import io.reactivex.Observable

/**
 * Creates an Observable that emits text changes from a TextView.
 * This replaces the RxBinding library's RxTextView.textChanges().
 *
 * The TextWatcher is only added when the Observable is subscribed to,
 * and removed when unsubscribed, preventing memory leaks.
 */
fun TextView.textChanges(): Observable<CharSequence> {
    return Observable.create { emitter ->
        val watcher =
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    // Not needed - we only care about text changes, not pre-change state
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    if (!emitter.isDisposed) {
                        try {
                            emitter.onNext(s ?: "")
                        } catch (_: Exception) {
                            // Emitter was disposed - ignore
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // Not needed - onTextChanged already emits the new value
                }
            }

        // Add the watcher when subscribed
        addTextChangedListener(watcher)

        // Emit the current text value immediately
        if (!emitter.isDisposed) {
            try {
                emitter.onNext(text ?: "")
            } catch (_: Exception) {
                // Emitter was disposed - ignore
            }
        }

        // Remove the watcher when unsubscribed
        emitter.setCancellable {
            removeTextChangedListener(watcher)
        }
    }
}
