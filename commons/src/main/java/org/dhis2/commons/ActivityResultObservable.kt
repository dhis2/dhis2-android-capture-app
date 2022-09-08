package org.dhis2.commons

interface ActivityResultObservable {
    fun subscribe(activityResultObserver: ActivityResultObserver)
    fun unsubscribe()
}
