package org.dhis2.mobile.extension.host.domain

/**
 * What a custom form uses, read from its HTML alone. The survey of real forms runs this over
 * every form of an implementation to decide what the legacy shim must support and how forms
 * behave on a phone.
 */
data class CustomFormSurvey(
    val sizeInChars: Int,
    /** Inputs named `{dataElement}-{categoryOptionCombo}-val`. */
    val fields: Int,
    val totals: Int,
    val indicators: Int,
    val inlineScripts: Int,
    /** `<script src>`: these would be loaded from outside the app, which the WebView refuses. */
    val externalScripts: Set<String>,
    /** Every `dhis2.de.*` and `dhis2.util.*` name the form mentions. */
    val legacyCalls: Set<String>,
    /** The ones the shim does not provide: the form opens with the native form until it does. */
    val unsupportedCalls: Set<String>,
    /** Web API paths the form mentions. Custom forms may not call the API in 3.6.0. */
    val apiPaths: Set<String>,
    /** The widest fixed width in px found in `width` attributes and styles, if any. */
    val widestFixedWidth: Int?,
) {
    val hasScript: Boolean get() = inlineScripts > 0 || externalScripts.isNotEmpty()
}

fun surveyCustomForm(
    html: String,
    scan: LegacyApiScan = LegacyApiScan(),
): CustomFormSurvey {
    val scripts = SCRIPT.findAll(html).toList()
    val externalScripts = scripts.mapNotNull { SCRIPT_SRC.find(it.groupValues[1])?.groupValues?.get(1) }.toSet()
    val legacyCalls = scan.calls(html)
    return CustomFormSurvey(
        sizeInChars = html.length,
        fields = FIELD.findAll(html).count(),
        totals = TOTAL.findAll(html).count(),
        indicators = INDICATOR.findAll(html).count(),
        inlineScripts = scripts.size - externalScripts.size,
        externalScripts = externalScripts,
        legacyCalls = legacyCalls,
        unsupportedCalls = scan.unsupportedCalls(html),
        apiPaths = API_PATH.findAll(html).map { it.groupValues[1] }.toSet(),
        widestFixedWidth = FIXED_WIDTH.findAll(html).mapNotNull { it.groupValues[1].toIntOrNull() }.maxOrNull(),
    )
}

private val SCRIPT = Regex("""<script\b([^>]*)>""", RegexOption.IGNORE_CASE)
private val SCRIPT_SRC = Regex("""\bsrc\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
private val FIELD = Regex("""\bid\s*=\s*["'][A-Za-z0-9]{11}-[A-Za-z0-9]{11}-val["']""")
private val TOTAL = Regex("""\bid\s*=\s*["']total[A-Za-z0-9]{11}["']""")
private val INDICATOR = Regex("""\bindicatorid\s*=""", RegexOption.IGNORE_CASE)
private val API_PATH = Regex("""["'`](?:\.\./)*/?(api/[A-Za-z0-9_/.\-]+)""")
private val FIXED_WIDTH = Regex("""\bwidth\s*[:=]\s*["']?\s*(\d{3,5})(?:px)?\b""", RegexOption.IGNORE_CASE)
