package org.dhis2.commons.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * Creates an Observable that emits text changes from a TextView.
 * This replaces the RxBinding library's RxTextView.textChanges().
 */
fun TextView.textChanges(): Observable<CharSequence> {
    val subject = BehaviorSubject.createDefault(text)

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
                subject.onNext(s ?: "")
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed - onTextChanged already emits the new value
            }
        }

    addTextChangedListener(watcher)

    return subject
        .doOnDispose { removeTextChangedListener(watcher) }
}
