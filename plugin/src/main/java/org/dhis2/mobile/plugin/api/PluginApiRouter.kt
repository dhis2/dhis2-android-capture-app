package org.dhis2.mobile.plugin.api

import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.hisp.dhis.android.core.D2
import timber.log.Timber
import java.net.URLDecoder
import kotlin.coroutines.cancellation.CancellationException

/**
 * Answers a plugin's DHIS2 Web API calls from the SDK's local database, so a plugin built for the
 * web works offline without knowing it is not talking to a server.
 *
 * Reads come from the SDK; writes go into it, so they are uploaded by the app's normal sync. Each
 * response has the shape the web API returns, which is what `@dhis2/app-runtime` parses. An endpoint
 * with no offline implementation answers `501` and is logged, so the list of what real plugins need
 * builds itself.
 */
class PluginApiRouter(
    private val d2: D2,
    private val dispatcher: Dispatcher,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun handle(request: ApiRequest): ApiResponse =
        withContext(dispatcher.io) {
            try {
                route(request)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Timber.e(error, "Plugin API call failed: %s %s", request.method, request.url)
                webError(500, "Internal Server Error", error.message ?: "The request could not be completed")
            }
        }

    private fun route(request: ApiRequest): ApiResponse {
        val method = request.method.uppercase()
        val segments = apiSegments(request.url)
        return when {
            method == "GET" && segments == listOf("me") -> me()
            method == "GET" && segments == listOf("system", "info") -> systemInfo()
            method == "GET" && segments == listOf("userSettings") -> userSettings()
            method == "GET" && segments == listOf("systemSettings") -> ok(JsonObject(emptyMap()))
            segments.firstOrNull() == "dataStore" -> dataStore(method, segments.drop(1), request.body)
            method == "POST" && segments == listOf("dataValueSets") -> importDataValueSet(request.body)
            else -> {
                Timber.w("Plugin called %s %s, which has no offline implementation", method, request.url)
                webError(501, "Not Implemented", "$method ${request.url} is not available offline in DHIS2 Android")
            }
        }
    }

    /** The path below `/api/`, decoded, without the optional API version segment. */
    private fun apiSegments(url: String): List<String> {
        val segments =
            url
                .substringBefore('?')
                .substringAfter("/api/", missingDelimiterValue = "")
                .split('/')
                .filter { it.isNotEmpty() }
                .map { URLDecoder.decode(it, Charsets.UTF_8.name()) }
        return if (segments.firstOrNull()?.all(Char::isDigit) == true) segments.drop(1) else segments
    }

    private fun me(): ApiResponse {
        val user = d2.userModule().user().blockingGet()
        val authorities =
            d2
                .userModule()
                .authorities()
                .blockingGet()
                .mapNotNull { it.name() }
        return ok(
            buildJsonObject {
                put("id", user?.uid().orEmpty())
                put("username", user?.username().orEmpty())
                put("firstName", user?.firstName().orEmpty())
                put("surname", user?.surname().orEmpty())
                put("displayName", user?.displayName().orEmpty())
                put("name", user?.displayName().orEmpty())
                put("email", user?.email().orEmpty())
                put("authorities", JsonArray(authorities.map(::JsonPrimitive)))
                put("settings", JsonObject(emptyMap()))
                put(
                    "userCredentials",
                    buildJsonObject {
                        put("username", user?.username().orEmpty())
                        put("userRoles", JsonArray(emptyList()))
                    },
                )
            },
        )
    }

    private fun systemInfo(): ApiResponse {
        val info = d2.systemInfoModule().systemInfo().blockingGet()
        return ok(
            buildJsonObject {
                put("version", info?.version() ?: "unknown")
                put("contextPath", info?.contextPath().orEmpty())
                put("dateFormat", info?.dateFormat() ?: "yyyy-MM-dd")
                put("calendar", "iso8601")
                put("systemName", "DHIS2 Android Capture")
            },
        )
    }

    private fun userSettings(): ApiResponse =
        ok(
            buildJsonObject {
                put(
                    "keyUiLocale",
                    java.util.Locale
                        .getDefault()
                        .language,
                )
                put(
                    "keyDbLocale",
                    java.util.Locale
                        .getDefault()
                        .language,
                )
            },
        )

    private fun dataStore(
        method: String,
        path: List<String>,
        body: String?,
    ): ApiResponse {
        val repository = d2.dataStoreModule().dataStore()
        return when {
            path.size == 1 && method == "GET" -> {
                val keys =
                    repository
                        .byNamespace()
                        .eq(path[0])
                        .blockingGet()
                        .filter { it.deleted() != true }
                        .map { it.key() }
                ok(JsonArray(keys.map(::JsonPrimitive)))
            }

            path.size == 2 -> dataStoreKey(method, namespace = path[0], key = path[1], body = body)

            else -> webError(400, "Bad Request", "Unsupported dataStore path")
        }
    }

    private fun dataStoreKey(
        method: String,
        namespace: String,
        key: String,
        body: String?,
    ): ApiResponse {
        val value = d2.dataStoreModule().dataStore().value(namespace, key)
        val existing = value.blockingGet()?.takeIf { it.deleted() != true }
        return when (method) {
            "GET" ->
                existing?.value()?.let { ApiResponse(200, it) }
                    ?: webError(404, "Not Found", "Key '$key' not found in namespace '$namespace'")

            "PUT" -> {
                value.blockingSet(body.orEmpty())
                message(200, "OK", "Key '$key' in namespace '$namespace' updated.")
            }

            "POST" ->
                if (existing != null) {
                    webError(409, "Conflict", "Key '$key' already exists in namespace '$namespace'")
                } else {
                    value.blockingSet(body.orEmpty())
                    message(201, "Created", "Key '$key' created.")
                }

            "DELETE" -> {
                value.blockingDeleteIfExist()
                message(200, "OK", "Key '$key' deleted from namespace '$namespace'.")
            }

            else -> webError(405, "Method Not Allowed", "$method is not supported on dataStore keys")
        }
    }

    private fun importDataValueSet(body: String?): ApiResponse {
        val payload = json.decodeFromString<DataValueSetPayload>(body.orEmpty())
        // Since DHIS2 2.43 a data value belongs to a data set; the SDK will not guess one.
        val dataSet =
            payload.dataSet
                ?: return webError(409, "Conflict", "A dataSet is required to store data values offline")

        // A value without option combos takes the only one its category combo has, but only when that
        // is the default combo. The SDK stores whatever it is given and the server rejects a wrong
        // combo on upload (E8023), so guessing here would leave data that can never sync.
        val attributeOptionCombo by lazy {
            payload.attributeOptionCombo ?: defaultOptionComboOf(dataSetCategoryCombo(dataSet))
        }
        var imported = 0
        var updated = 0
        var deleted = 0
        val conflicts = mutableListOf<Pair<String, String>>()

        for (dataValue in payload.dataValues) {
            val period = dataValue.period ?: payload.period
            val orgUnit = dataValue.orgUnit ?: payload.orgUnit
            if (period == null || orgUnit == null) {
                conflicts += dataValue.dataElement to "Data value has no period or organisation unit"
                continue
            }
            val categoryOptionCombo =
                dataValue.categoryOptionCombo
                    ?: defaultOptionComboOf(dataElementCategoryCombo(dataValue.dataElement))
            val valueAttributeOptionCombo = dataValue.attributeOptionCombo ?: attributeOptionCombo
            if (categoryOptionCombo == null || valueAttributeOptionCombo == null) {
                conflicts += dataValue.dataElement to
                    "categoryOptionCombo and attributeOptionCombo are required: the category combo is not the default"
                continue
            }
            try {
                val repository =
                    d2.dataValueModule().dataValues().value(
                        period,
                        orgUnit,
                        dataValue.dataElement,
                        categoryOptionCombo,
                        valueAttributeOptionCombo,
                        dataSet,
                    )
                val newValue = dataValue.value?.contentOrNull
                when {
                    newValue.isNullOrEmpty() -> {
                        repository.blockingDeleteIfExist()
                        deleted++
                    }

                    repository.blockingExists() -> {
                        repository.blockingSet(newValue)
                        updated++
                    }

                    else -> {
                        repository.blockingSet(newValue)
                        imported++
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                conflicts += dataValue.dataElement to (error.message ?: "Value could not be stored")
            }
        }

        // As the server does: a conflict alongside stored values is a warning, conflicts alone an error.
        val nothingStored = imported + updated + deleted == 0
        val (httpCode, outcome) =
            when {
                conflicts.isEmpty() -> 200 to "OK"
                nothingStored -> 409 to "ERROR"
                else -> 200 to "WARNING"
            }
        val status = if (conflicts.isEmpty()) "SUCCESS" else outcome
        return ApiResponse(
            httpCode,
            buildJsonObject {
                put("httpStatus", if (httpCode == 200) "OK" else "Conflict")
                put("httpStatusCode", httpCode)
                put("status", outcome)
                put("message", conflicts.firstOrNull()?.second ?: "Import was successful.")
                put(
                    "response",
                    buildJsonObject {
                        put("responseType", "ImportSummary")
                        put("status", status)
                        put(
                            "importCount",
                            buildJsonObject {
                                put("imported", imported)
                                put("updated", updated)
                                put("ignored", conflicts.size)
                                put("deleted", deleted)
                            },
                        )
                        put(
                            "conflicts",
                            buildJsonArray {
                                conflicts.forEach { (obj, value) ->
                                    add(
                                        buildJsonObject {
                                            put("object", obj)
                                            put("value", value)
                                        },
                                    )
                                }
                            },
                        )
                    },
                )
            }.toString(),
        )
    }

    private fun dataElementCategoryCombo(dataElement: String): String? =
        d2
            .dataElementModule()
            .dataElements()
            .uid(dataElement)
            .blockingGet()
            ?.categoryCombo()
            ?.uid()

    private fun dataSetCategoryCombo(dataSet: String): String? =
        d2
            .dataSetModule()
            .dataSets()
            .uid(dataSet)
            .blockingGet()
            ?.categoryCombo()
            ?.uid()

    /** The only option combo of [categoryCombo] if it is the default combo, otherwise null. */
    private fun defaultOptionComboOf(categoryCombo: String?): String? {
        if (categoryCombo == null) return null
        val isDefault =
            d2
                .categoryModule()
                .categoryCombos()
                .uid(categoryCombo)
                .blockingGet()
                ?.isDefault == true
        if (!isDefault) return null
        return d2
            .categoryModule()
            .categoryOptionCombos()
            .byCategoryComboUid()
            .eq(categoryCombo)
            .one()
            .blockingGet()
            ?.uid()
    }

    private fun ok(body: JsonElement) = ApiResponse(200, body.toString())

    private fun message(
        status: Int,
        httpStatus: String,
        text: String,
    ) = ApiResponse(status, webApiMessage(status, httpStatus, "OK", text))

    private fun webError(
        status: Int,
        httpStatus: String,
        text: String,
    ) = ApiResponse(status, webApiMessage(status, httpStatus, "ERROR", text))

    /** The `WebMessage` envelope the DHIS2 Web API wraps statuses and errors in. */
    private fun webApiMessage(
        status: Int,
        httpStatus: String,
        outcome: String,
        text: String,
    ): String =
        buildJsonObject {
            put("httpStatus", httpStatus)
            put("httpStatusCode", status)
            put("status", outcome)
            put("message", text)
        }.toString()
}

@Serializable
private data class DataValueSetPayload(
    val dataSet: String? = null,
    val period: String? = null,
    val orgUnit: String? = null,
    val attributeOptionCombo: String? = null,
    val dataValues: List<DataValuePayload> = emptyList(),
)

@Serializable
private data class DataValuePayload(
    val dataElement: String,
    val period: String? = null,
    val orgUnit: String? = null,
    val categoryOptionCombo: String? = null,
    val attributeOptionCombo: String? = null,
    /** A string or a number on the web API, so kept as a primitive. */
    val value: JsonPrimitive? = null,
)
