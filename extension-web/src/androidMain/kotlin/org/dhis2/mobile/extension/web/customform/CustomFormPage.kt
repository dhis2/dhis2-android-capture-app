package org.dhis2.mobile.extension.web.customform

import org.dhis2.mobile.extension.web.host.HOST_PAGES_URL

/**
 * The host page a custom form opens in.
 *
 * - [PLAIN]: a form with no script of its own (step 1). Only the binding layer runs: it shows the
 *   fields and hands every value to native code, which saves and validates it.
 * - [LEGACY]: a form whose scripts must run (step 2). The legacy shim is loaded on top of the
 *   binding layer, recreating what the old web Data Entry page gave those scripts.
 */
internal enum class CustomFormPage(
    val url: String,
) {
    PLAIN("$HOST_PAGES_URL/custom-form.html"),
    LEGACY("$HOST_PAGES_URL/custom-form-legacy.html"),
    ;

    companion object {
        fun of(html: String): CustomFormPage = if (SCRIPTED.any { it.containsMatchIn(html) }) LEGACY else PLAIN

        /** A `<script>`, an inline event handler (`onclick=`…) or a `javascript:` link. */
        private val SCRIPTED =
            listOf(
                Regex("""<script\b""", RegexOption.IGNORE_CASE),
                Regex("""<[a-z][^>]*\son[a-z]+\s*=""", RegexOption.IGNORE_CASE),
                Regex("""\bhref\s*=\s*["']?\s*javascript:""", RegexOption.IGNORE_CASE),
            )
    }
}
