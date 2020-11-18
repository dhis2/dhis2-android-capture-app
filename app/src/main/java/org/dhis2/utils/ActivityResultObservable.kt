package org.dhis2.utils

interface ActivityResultObservable {
    fun subscribe(activityResultObserver: ActivityResultObserver)
    fun unsubscribe()
}
