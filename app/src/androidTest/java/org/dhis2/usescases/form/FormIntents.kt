package org.dhis2.usescases.form

import androidx.test.core.app.ApplicationProvider
import org.dhis2.LazyActivityScenarioRule
import org.dhis2.form.model.EventMode
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity

const val PROGRAM_UID = "PROGRAM_UID"
const val PROGRAM_XX_PROGRAM_RULES = "jIT6KcSZiAN"
const val EVENT_GAMMA = "MIZVQnTD4HW"


fun prepareIntentAndLaunchEventActivity(rule: LazyActivityScenarioRule<EventCaptureActivity>) {
    EventCaptureActivity.intent(
        ApplicationProvider.getApplicationContext(),
        EVENT_GAMMA,
        PROGRAM_XX_PROGRAM_RULES,
        EventMode.CHECK
    ).also { rule.launch(it) }
}
