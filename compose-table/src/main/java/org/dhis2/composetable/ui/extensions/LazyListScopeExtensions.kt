package org.dhis2.composetable.ui.extensions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable

@ExperimentalFoundationApi
fun LazyListScope.fixedStickyHeader(
    fixHeader: Boolean = true,
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (fixHeader) {
        stickyHeader("${key}_sticky", contentType = contentType, content = { content() })
    } else {
        item("${key}_non_sticky", contentType = contentType, content = content)
    }
}
