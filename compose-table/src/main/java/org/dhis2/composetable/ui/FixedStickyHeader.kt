package org.dhis2.composetable.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable

@ExperimentalFoundationApi
fun LazyListScope.fixedStickyHeader(
    fixHeader: Boolean = true,
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyItemScope.() -> Unit
) {
    if (fixHeader) {
        stickyHeader(key, contentType = contentType, content = content)
    } else {
        item(key, contentType = contentType, content = content)
    }
}
