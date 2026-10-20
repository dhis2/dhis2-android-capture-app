package org.dhis2.mobile.plugin.api

/**
 * A DHIS2 Web API call made by a plugin, captured in the WebView before it reaches the network.
 *
 * @property method HTTP method, upper case.
 * @property url Path and query from `/api/` on, e.g. `/api/41/dataStore/ns/key?fields=id`.
 * @property body Request body, for writes.
 */
data class ApiRequest(
    val method: String,
    val url: String,
    val body: String?,
)

/** The answer handed back to the plugin as if the server had sent it. */
data class ApiResponse(
    val status: Int,
    val body: String,
    val contentType: String = "application/json",
)
