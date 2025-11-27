package org.dhis2.tracker.relationships

import java.io.File
import kotlin.test.Test
import kotlin.test.fail

class StringResourceFormatTest {
    @Test
    fun `verify all language files have consistent format specifiers`() {
        val resourcesDir = File("src/commonMain/composeResources")
        val baseStrings = mutableMapOf<String, String>()

        // Parse base strings.xml
        val baseFile = File(resourcesDir, "values/strings.xml")
        if (baseFile.exists()) {
            parseStringsXml(baseFile, baseStrings)
        }

        // Get keys that have format specifiers in base language
        val keysWithFormatters =
            baseStrings
                .filter { (_, value) ->
                    Regex("""%\d+\$[a-z]""").containsMatchIn(value)
                }.keys

        // Check all other language files
        val errors = mutableListOf<String>()

        resourcesDir
            .walkTopDown()
            .filter { it.name == "strings.xml" && it.path != baseFile.path }
            .forEach { file ->
                val langStrings = mutableMapOf<String, String>()
                parseStringsXml(file, langStrings)

                keysWithFormatters.forEach { key ->
                    val baseValue = baseStrings[key] ?: return@forEach
                    val translatedValue = langStrings[key] ?: return@forEach

                    val baseFormatters = Regex("""%\d+\$[a-z]""").findAll(baseValue).toList()
                    val translatedFormatters =
                        Regex("""%\d+\$[a-z]""").findAll(translatedValue).toList()

                    if (baseFormatters.size != translatedFormatters.size) {
                        errors.add(
                            "Mismatch in ${file.parentFile.name}/$key:\n" +
                                "  Base: '$baseValue' (${baseFormatters.size} formatters)\n" +
                                "  Translation: '$translatedValue' (${translatedFormatters.size} formatters)",
                        )
                    }
                }
            }

        if (errors.isNotEmpty()) {
            fail("Found inconsistent format specifiers:\n${errors.joinToString("\n\n")}")
        }
    }

    private fun parseStringsXml(
        file: File,
        stringResources: MutableMap<String, String>,
    ) {
        val content = file.readText()

        // Regex to extract string resources
        // Matches: <string name="key">value</string>
        val stringPattern = Regex("""<string\s+name="([^"]+)">([^<]+)</string>""")

        stringPattern.findAll(content).forEach { matchResult ->
            val key = matchResult.groupValues[1]
            val value =
                matchResult.groupValues[2]
                    .replace("&quot;", "\"")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")

            stringResources[key] = value
        }
    }
}
