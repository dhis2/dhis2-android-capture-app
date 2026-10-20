package org.dhis2.mobile.plugin.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppHubTest {
    private val versions =
        """
        {"pager": {}, "result": [
          {"id": "v2", "version": "1.0.2", "downloadUrl": "https://apps.dhis2.org/api/v2/apps/a/download/matrix-select_1.0.2.zip"},
          {"id": "v1", "version": "1.0.1", "downloadUrl": "https://apps.dhis2.org/api/v2/apps/a/download/matrix-select_1.0.1.zip"}
        ]}
        """.trimIndent()

    @Test
    fun `picks the download of exactly the version the server runs`() {
        assertEquals(
            "https://apps.dhis2.org/api/v2/apps/a/download/matrix-select_1.0.1.zip",
            AppHub.downloadUrlFor(versions, "1.0.1"),
        )
    }

    @Test
    fun `a version the App Hub does not publish has no download`() {
        assertNull(AppHub.downloadUrlFor(versions, "9.9.9"))
    }

    @Test
    fun `search results are narrowed to apps with exactly the installed name`() {
        val search =
            """
            {"pager": {}, "result": [
              {"id": "a", "name": "Matrix Select"},
              {"id": "b", "name": "Matrix Select Pro"},
              {"id": "c", "name": "matrix select"}
            ]}
            """.trimIndent()

        assertEquals(listOf("a"), AppHub.appIdsNamed(search, "Matrix Select"))
    }

    @Test
    fun `unreadable App Hub responses are no matches, not crashes`() {
        assertEquals(emptyList<String>(), AppHub.appIdsNamed("<html>", "Matrix Select"))
        assertNull(AppHub.downloadUrlFor("<html>", "1.0.2"))
    }
}
