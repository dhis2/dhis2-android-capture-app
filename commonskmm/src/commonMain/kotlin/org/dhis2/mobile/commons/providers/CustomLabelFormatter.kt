package org.dhis2.mobile.commons.providers

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale

private val formatSpecifier = Regex("""%(\d+\$)?[-#+ 0,(<]*\d*(\.\d+)?([a-zA-Z%])""")
private val sentenceTerminators = setOf('.', '!', '?')

/**
 * Formats this string resource with a custom [term], capitalizing the term only when it is
 * the first word of a sentence. Anywhere else the term is inserted exactly as configured.
 *
 * When [quantity] is given it is passed as the first format argument and the term as the
 * second, so templates such as `"%1$d %2$s"` are supported.
 */
fun String.formatWithTerm(
    term: String,
    quantity: Int? = null,
): String {
    val termArgIndex = if (quantity != null) 2 else 1
    val casedTerm = if (isAtSentenceStart(termArgIndex)) term.capitalize(Locale.current) else term
    return if (quantity != null) format(quantity, casedTerm) else format(casedTerm)
}

/**
 * Whether the placeholder consuming the format argument at [argIndex] (1-based) starts a
 * sentence: it is preceded only by whitespace, by a line break, or by a sentence terminator
 * followed by whitespace.
 */
internal fun String.isAtSentenceStart(argIndex: Int): Boolean {
    var sequentialIndex = 0
    val placeholder =
        formatSpecifier.findAll(this).firstOrNull { match ->
            val conversion = match.groupValues[3]
            if (conversion == "%" || conversion == "n") return@firstOrNull false
            val explicitIndex = match.groupValues[1].dropLast(1).toIntOrNull()
            val index = explicitIndex ?: ++sequentialIndex
            index == argIndex
        } ?: return false

    val before = substring(0, placeholder.range.first)
    return before.isBlank() ||
        before.endsWith('\n') ||
        (before.last().isWhitespace() && before.trimEnd().last() in sentenceTerminators)
}
