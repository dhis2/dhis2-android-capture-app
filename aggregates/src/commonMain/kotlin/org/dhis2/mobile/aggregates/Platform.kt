package org.dhis2.mobile.aggregates

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
