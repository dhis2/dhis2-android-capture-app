package org.dhis2.utils.granularsync

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.development.DevelopmentActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SMSSenderHelperTest : BaseTest() {

    @get:Rule
    val activityScenarioRule: ActivityScenarioRule<DevelopmentActivity> =
        ActivityScenarioRule(DevelopmentActivity::class.java)

    @Test
    fun shouldSendUniqueSms() {
        var status: SMSSenderHelper.Status? = null
        val smsMsg = "This is an sms"

        mockSMSIntentResult()
        launchActivity(
            smsMsg = smsMsg,
            onStatusChanged = { status = it },
            onSmsHelperInit = { smsHelper ->
                assert(smsHelper.smsCount() == 1)
                smsHelper.pollSms()
                assert(smsHelper.smsCount() == 0)
            }
        )
        assertStatus({ status }, null, SMSSenderHelper.Status.RETURNED_TO_APP)
    }

    @Test
    fun shouldSendSeveralSms() {
        var status: SMSSenderHelper.Status? = null
        var longSmsMsg = "This is a very long sms"
        repeat(10) {
            longSmsMsg += longSmsMsg
        }

        mockSMSIntentResult()
        launchActivity(
            smsMsg = longSmsMsg,
            onStatusChanged = { status = it },
            onSmsHelperInit = { smsHelper ->
                assert(smsHelper.smsCount() == 154)
                smsHelper.pollSms()
                assert(smsHelper.smsCount() == 153)
            }
        )
        assertStatus({ status }, null, SMSSenderHelper.Status.RETURNED_TO_APP)
    }

    @Test
    fun shouldSetAllValuesSent() {
        var status: SMSSenderHelper.Status? = null
        val smsMsg = "This is an sms"

        var smsSenderHelper: SMSSenderHelper? = null
        mockSMSIntentResult()
        launchActivity(
            smsMsg = smsMsg,
            onStatusChanged = { status = it },
            onSmsHelperInit = { smsHelper ->
                smsSenderHelper = smsHelper
                assert(smsHelper.smsCount() == 1)
                smsHelper.pollSms()
                assert(smsHelper.smsCount() == 0)
            }
        )
        assertStatus({ status }, null, SMSSenderHelper.Status.RETURNED_TO_APP)
        smsSenderHelper?.pollSms()
        assertStatus(
            { status },
            SMSSenderHelper.Status.RETURNED_TO_APP,
            SMSSenderHelper.Status.ALL_SMS_SENT
        )
    }

    private fun mockSMSIntentResult() {
        enableIntents()
        intending(hasExtra(Intent.EXTRA_TITLE, "Send Sms with"))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))
    }

    private fun launchActivity(
        smsMsg: String,
        onSmsHelperInit: (SMSSenderHelper) -> Unit,
        onStatusChanged: (SMSSenderHelper.Status) -> Unit
    ) {
        activityScenarioRule.scenario.onActivity { activity ->
            val smsHelper = SMSSenderHelper(
                context = activity,
                registry = activity.activityResultRegistry,
                fragmentManager = activity.supportFragmentManager,
                smsNumberTo = "+00111111",
                smsMessage = smsMsg
            ) {
                onStatusChanged(it)
            }
            onSmsHelperInit(smsHelper)
        }
    }

    private fun assertStatus(
        currentStatus: () -> SMSSenderHelper.Status?,
        lastStatus: SMSSenderHelper.Status?,
        expectedStatus: SMSSenderHelper.Status
    ) {
        do {
            BaseRobot().waitToDebounce(1000)
        } while (currentStatus() == lastStatus)
        assert(currentStatus() == expectedStatus)
    }
}