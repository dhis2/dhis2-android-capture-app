package org.dhis2.commons.rules

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Date

fun Date.toRuleEngineInstant() =
    Instant.fromEpochMilliseconds(this.time)

fun Date.toRuleEngineLocalDate() =
    toRuleEngineInstant().toLocalDateTime(TimeZone.currentSystemDefault()).date
