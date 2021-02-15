package org.dhis2.utils

import org.junit.Assert
import org.junit.Test

class JsonCheckerTest {

    @Test
    fun `should return text for simple text`() {
        val checker = JsonChecker()

        val result = checker.check("simple text")

        Assert.assertTrue(result is JsonCheckResult.Text)
    }

    @Test
    fun `should return json for valid json text`() {
        val checker = JsonChecker()

        val result = checker.check("[{\"prop\":\"value\" }]")

        Assert.assertTrue(result is JsonCheckResult.Json)
    }

    @Test
    fun `should return malformed json for malformed json text`() {
        val checker = JsonChecker()

        val result = checker.check("[{\"prop\":\"value\"")

        Assert.assertTrue(result is JsonCheckResult.MalformedJson)
    }
}
