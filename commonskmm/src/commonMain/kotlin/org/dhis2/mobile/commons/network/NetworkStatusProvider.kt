package org.dhis2.mobile.commons.network

interface NetworkStatusProvider {
    fun isOnline(): Boolean

    fun init()

    fun clear()
}
