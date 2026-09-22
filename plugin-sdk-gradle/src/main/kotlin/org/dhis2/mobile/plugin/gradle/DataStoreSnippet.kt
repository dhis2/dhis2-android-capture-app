package org.dhis2.mobile.plugin.gradle

/**
 * Renders `plugin-config.json`: the dataStore entry a DHIS2 administrator posts, with everything
 * the build knows already filled in — `version` and `checksum` for certain, and `id`, `entryPoint`,
 * `downloadUrl`, `injectionPoints` and `slotConfig` from whatever `pluginBundle { }` declared.
 *
 * A convenience, not part of the bundle. The server dataStore stays the single source of truth for
 * a plugin's identity and slot configuration; this only saves the administrator from assembling the
 * JSON by hand, and saves the plugin author from re-pasting a checksum after every build.
 *
 * Built line by line rather than from one indented template: the injection point list and the slot
 * configuration are themselves multi-line, and interpolating those into a `trimIndent()` literal
 * makes the rendered indentation depend on their contents.
 */
internal object DataStoreSnippet {
    fun render(
        pluginId: String,
        version: String,
        entryPoint: String,
        bundleFileName: String,
        downloadUrlBase: String,
        checksum: String,
        injectionPoints: List<String>,
        slotConfig: Map<String, Map<String, List<String>>>,
    ): String =
        buildString {
            appendLine("{")
            appendLine("""  "plugins": [""")
            appendLine("    {")
            appendLine("""      "id": "$pluginId",""")
            appendLine("""      "version": "$version",""")
            appendLine("""      "entryPoint": "$entryPoint",""")
            appendLine("""      "downloadUrl": "${downloadUrlBase.trimEnd('/')}/$bundleFileName",""")
            appendLine("""      "checksum": "$checksum",""")
            appendLine("""      "injectionPoints": [""")
            injectionPoints.forEachIndexed { index, slot ->
                val comma = if (index == injectionPoints.lastIndex) "" else ","
                appendLine("""        "$slot"$comma""")
            }
            // Omitted entirely when nothing is configured, rather than emitted empty: a replacement
            // slot renders nowhere until it is configured, and an empty block reads like a working
            // configuration that happens to do nothing.
            if (slotConfig.isEmpty()) {
                appendLine("      ]")
            } else {
                appendLine("      ],")
                appendLine("""      "slotConfig": {""")
                slotConfig.entries.forEachIndexed { slotIndex, (slot, fields) ->
                    val slotComma = if (slotIndex == slotConfig.size - 1) "" else ","
                    appendLine("""        "$slot": {""")
                    fields.entries.forEachIndexed { fieldIndex, (field, values) ->
                        val fieldComma = if (fieldIndex == fields.size - 1) "" else ","
                        val rendered = values.joinToString(", ") { """"$it"""" }
                        appendLine("""          "$field": [$rendered]$fieldComma""")
                    }
                    appendLine("        }$slotComma")
                }
                appendLine("      }")
            }
            appendLine("    }")
            appendLine("  ]")
            appendLine("}")
        }
}
