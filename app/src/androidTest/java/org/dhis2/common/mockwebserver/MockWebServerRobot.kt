package org.dhis2.common.mockwebserver

import org.hisp.dhis.android.core.mockwebserver.Dhis2MockServer

class MockWebServerRobot(private val dhis2MockServer: Dhis2MockServer) {

    fun start() {
        dhis2MockServer.setDhis2Dispatcher()
    }

    fun shutdown() {
        dhis2MockServer.shutdown()
    }

    fun addResponse(method: String, path: String, sdkResource: String, responseCode: Int = 200) {
        dhis2MockServer.addResponse(method, path, sdkResource, responseCode)
    }
}
