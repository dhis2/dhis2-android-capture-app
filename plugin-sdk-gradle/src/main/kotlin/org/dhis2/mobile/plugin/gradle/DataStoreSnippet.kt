package org.dhis2.mobile.plugin.gradle

/**
 * Renders `plugin-config.json`: the §4 dataStore entry a DHIS2 administrator posts, with the two
 * fields the build knows for certain — `version` and `checksum` — already filled in.
 *
 * A convenience, not part of the bundle. The server dataStore stays the single source of truth for
 * a plugin's identity and data scope; this only saves the administrator from assembling the JSON by
 * hand, and saves the plugin author from re-pasting a checksum after every build.
 */
internal object DataStoreSnippet {
    fun render(
        pluginId: String,
        version: String,
        entryPoint: String,
        bundleFileName: String,
        checksum: String,
    ): String =
        """
        {
          "plugins": [
            {
              "id": "$pluginId",
              "version": "$version",
              "entryPoint": "$entryPoint",
              "downloadUrl": "http://10.0.2.2:8081/$bundleFileName",
              "checksum": "$checksum",
              "injectionPoints": [
                "HOME_ABOVE_PROGRAM_LIST"
              ],
              "scope": {
                "programs": { "uids": [] },
                "dataSets": { "uids": [] },
                "orgUnits": { "uids": [], "mode": "DESCENDANTS" },
                "capabilities": [
                  "READ_METADATA"
                ],
                "writable": {
                  "programs": { "uids": [] },
                  "dataSets": { "uids": [] }
                }
              }
            }
          ]
        }

        """.trimIndent()
}
