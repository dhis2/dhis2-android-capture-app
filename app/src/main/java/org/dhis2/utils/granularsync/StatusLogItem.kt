package org.dhis2.utils.granularsync

import com.google.auto.value.AutoValue
import java.util.Date

@AutoValue
abstract class StatusLogItem {

    abstract fun date(): Date

    abstract fun description(): String

    abstract fun openLogs(): Boolean

    companion object {

        fun create(date: Date, description: String, openLogs: Boolean = false): StatusLogItem {
            return AutoValue_StatusLogItem(date, description, openLogs)
        }
    }
}
