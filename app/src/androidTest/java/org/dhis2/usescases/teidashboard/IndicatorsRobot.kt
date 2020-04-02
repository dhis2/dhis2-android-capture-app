package org.dhis2.usescases.teidashboard

import org.dhis2.common.BaseRobot

fun indicatorsRobot(indicatorsRobot: IndicatorsRobot.() -> Unit) {
    IndicatorsRobot().apply {
        indicatorsRobot()
    }
}

class IndicatorsRobot: BaseRobot() {


}