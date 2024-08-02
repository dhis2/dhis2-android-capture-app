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

    companion object {
        const val API_OLD_TRACKED_ENTITY_PATH = "/api/trackedEntityInstances/query?.*"
        const val API_OLD_TRACKED_ENTITY_RESPONSE =
            "mocks/teilist/old_tracked_entity_empty_response.json"
        const val API_OLD_EVENTS_PATH = "/api/events?.*"
        const val API_OLD_EVENTS_RESPONSE = "mocks/teilist/old_events_empty_response.json"

    }
}
