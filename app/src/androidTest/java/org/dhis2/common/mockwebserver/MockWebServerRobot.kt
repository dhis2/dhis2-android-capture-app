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
        const val API_UNIQUE_ID_TRACKED_ENTITY_ATTRIBUTES_RESERVED_VALUES_PATH =
            "/api/trackedEntityAttributes/lZGmxYbs97q/generateAndReserve?.*"
        const val API_UNIQUE_ID_TRACKED_ENTITY_ATTRIBUTES_RESERVED_VALUES_RESPONSE =
            "mocks/teidashboard/unique_id_tracked_entity_attribute_reserved_values.json"
        const val API_TB_IDENTIFIER_TRACKED_ENTITY_ATTRIBUTES_RESERVED_VALUES_PATH =
            "/api/trackedEntityAttributes/xs8A6tQJY0s/generateAndReserve?.*"
        const val API_TB_IDENTIFIER_TRACKED_ENTITY_ATTRIBUTES_RESERVED_VALUES_RESPONSE =
            "mocks/teidashboard/tb_identifier_tracked_entity_attribute_reserved_values.json"

        const val API_TRACKED_ENTITY_PATH = "/api/tracker/trackedEntities?.*"
        const val API_TRACKED_ENTITY_EMPTY_RESPONSE =
            "mocks/teilist/tracked_entity_empty_response.json"
        const val API_EVENTS_PATH = "/api/tracker/events?.*"
        const val API_EVENTS_EMPTY_RESPONSE = "mocks/teilist/events_empty_response.json"
    }
}
