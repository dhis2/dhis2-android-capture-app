package org.dhis2.form.data

import java.util.Date
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Date.toRuleEngineInstant() =
    Instant.fromEpochMilliseconds(this.time)

fun Date.toRuleEngineLocalDate() =
    toRuleEngineInstant().toLocalDateTime(TimeZone.currentSystemDefault()).date
