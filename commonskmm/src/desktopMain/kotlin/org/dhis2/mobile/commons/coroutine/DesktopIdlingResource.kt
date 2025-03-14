package org.dhis2.mobile.commons.coroutine

actual val idlingResource: CoroutineIdlingResource
    get() = DefaultIdlingResource
