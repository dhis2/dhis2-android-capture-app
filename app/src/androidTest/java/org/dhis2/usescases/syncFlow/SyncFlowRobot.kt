package org.dhis2.usescases.syncFlow

import org.dhis2.common.BaseRobot

fun syncFlowRobot(syncFlowRobot: SyncFlowRobot.() -> Unit) {
    SyncFlowRobot().apply {
        syncFlowRobot()
    }
}

class SyncFlowRobot : BaseRobot() {

}