package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.viewactions.scrollToBottomRecyclerView

fun eventRobot(eventRobot: EventRobot.() -> Unit) {
    EventRobot().apply {
        eventRobot()
    }
}

class EventRobot: BaseRobot() {

    fun scrollToBottomFormulary(){
        onView(withId(R.id.formRecycler)).perform(scrollToBottomRecyclerView())
    }
}
