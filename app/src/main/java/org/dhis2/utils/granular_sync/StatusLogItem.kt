package org.dhis2.utils.granular_sync

import com.google.auto.value.AutoValue

import java.util.Date


@AutoValue
abstract class StatusLogItem {

    abstract fun date(): Date

    abstract fun description(): String

    companion object {

        fun create(date: Date, description: String): StatusLogItem {
            return AutoValue_StatusLogItem(date, description)
        }
    }

}
