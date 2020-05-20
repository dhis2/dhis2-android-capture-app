package org.dhis2.usescases.searchte

import org.dhis2.common.BaseRobot

fun indicatorsRobot(searchteiRobot: SearchTeiRobot.() -> Unit) {
    SearchTeiRobot().apply {
        searchteiRobot()
    }
}

class SearchTeiRobot : BaseRobot()
