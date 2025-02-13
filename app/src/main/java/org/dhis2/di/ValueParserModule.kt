package org.dhis2.di

import org.dhis2.bindings.userFriendlyValue
import org.dhis2.mobile.aggregates.data.ValueParser
import org.hisp.dhis.android.core.D2
import org.koin.dsl.module

val valueParserModule = module {
    single<ValueParser> {
        ValueParserImpl(get())
    }
}

private class ValueParserImpl(private val d2: D2) : ValueParser {
    override fun parseValue(uid: String, value: String): String? {
        val valueTypeAndOptionSet = d2.trackedEntityModule().trackedEntityAttributes()
            .uid(uid).blockingGet()
            ?.let {
                Pair(it.valueType(), it.optionSet()?.uid())
            } ?: d2.dataElementModule().dataElements().uid(uid).blockingGet()?.let {
            Pair(it.valueType(), it.optionSet()?.uid())
        }
        val valueType = valueTypeAndOptionSet?.first
        val optionSetUid = valueTypeAndOptionSet?.second
        return value.userFriendlyValue(d2, valueType, optionSetUid)
    }
}
