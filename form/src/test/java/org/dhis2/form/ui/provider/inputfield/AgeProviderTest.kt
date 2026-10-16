package org.dhis2.form.ui.provider.inputfield

import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Test

class AgeProviderTest {
    private val fieldUid = "fieldUid"

    @Test
    fun `should save a complete date`() {
        val intents =
            captureIntents { handler ->
                saveValue(handler, fieldUid, "2023-01-19", ValueType.DATE, allowFutureDates = false)
            }

        assertEquals(
            listOf(
                FormIntent.OnSave(
                    uid = fieldUid,
                    value = "2023-01-19",
                    valueType = ValueType.DATE,
                    allowFutureDates = false,
                ),
            ),
            intents,
        )
    }

    @Test
    fun `should save a cleared date`() {
        val intents =
            captureIntents { handler ->
                saveValue(handler, fieldUid, null, ValueType.DATE, allowFutureDates = true)
            }

        assertEquals(
            listOf(
                FormIntent.OnSave(
                    uid = fieldUid,
                    value = null,
                    valueType = ValueType.DATE,
                    allowFutureDates = true,
                ),
            ),
            intents,
        )
    }

    @Test
    fun `should not save a partially typed date`() {
        val intents =
            captureIntents { handler ->
                saveValue(handler, fieldUid, "2023-01", ValueType.DATE, allowFutureDates = false)
            }

        assertEquals(
            listOf(
                FormIntent.OnTextChange(
                    uid = fieldUid,
                    value = "2023-01",
                    valueType = ValueType.DATE,
                ),
            ),
            intents,
        )
    }

    private fun captureIntents(block: (handler: (FormIntent) -> Unit) -> Unit): List<FormIntent> {
        val intents = mutableListOf<FormIntent>()
        block { intent -> intents.add(intent) }
        return intents
    }
}
