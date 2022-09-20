package org.dhis2.utils.granularsync

sealed class ConvertTaskResult {
    data class Message(val smsMessage: String) : ConvertTaskResult()
    data class Count(val smsCount: Int) : ConvertTaskResult()
}
