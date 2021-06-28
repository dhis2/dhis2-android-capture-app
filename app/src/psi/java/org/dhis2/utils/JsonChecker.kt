package org.dhis2.utils

import com.google.gson.JsonParser
import java.lang.Exception

sealed class JsonCheckResult {
    object Json : JsonCheckResult()
    object MalformedJson : JsonCheckResult()
    object Text : JsonCheckResult()
}

class JsonChecker {
    fun check(json: String): JsonCheckResult {

        return if (isAJson(json)) {
            try {
                JsonParser.parseString(json)

                JsonCheckResult.Json
            } catch (e: Exception) {
                JsonCheckResult.MalformedJson
            }
        } else {
            JsonCheckResult.Text
        }
    }

    private fun isAJson(json: String): Boolean {
        return json.indexOfAny(charArrayOf('{', '}')) >= 0
    }
}