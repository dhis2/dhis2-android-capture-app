package org.dhis2.mobile.commons.coroutine

object IdlingResourceProvider {
    var idlingResource: CoroutineIdlingResource = NoOpIdlingResource
}
