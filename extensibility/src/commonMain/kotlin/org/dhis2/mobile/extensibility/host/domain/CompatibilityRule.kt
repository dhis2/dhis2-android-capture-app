package org.dhis2.mobile.extensibility.host.domain

import org.dhis2.mobile.extensibility.host.model.FailureReason

/**
 * A per-kind check that runs before an extension is shown.
 * No general versioning scheme: web content has no host version to compare against,
 * so each rule answers "usable or not, and why".
 */

fun interface CompatibilityRule<in C> {
    fun check(content: C): FailureReason?
}

class NoFormScripts : CompatibilityRule<String> {
    override fun check(content: String): FailureReason? =
        if (SCRIPTED.any { it.containsMatchIn(content) }) FailureReason.UNSUPPORTED_FEATURE else null

    private companion object {
        val SCRIPTED =
            listOf(
                Regex("""<script\b""", RegexOption.IGNORE_CASE),
                Regex("""<[a-z][^>]*\son[a-z]+\s*=""", RegexOption.IGNORE_CASE),
                Regex("""\bhref\s*=\s*["']?\s*javascript:""", RegexOption.IGNORE_CASE),
            )
    }
}
