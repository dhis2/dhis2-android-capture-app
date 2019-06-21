package org.dhis2.utils

import java.util.*

class CurrentDateProviderImpl: CurrentDateProvider {
    override fun currentDate(): Date {
        return Date()
    }

}