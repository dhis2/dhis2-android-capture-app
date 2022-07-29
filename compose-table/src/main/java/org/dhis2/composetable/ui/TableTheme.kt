package org.dhis2.composetable.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun TableTheme(
    tableColors: TableColors?,
    tableDimensions: TableDimensions? = LocalTableDimensions.current,
    content:
        @Composable
        () -> Unit
) {
    CompositionLocalProvider(
        LocalTableColors provides (tableColors ?: TableColors()),
        LocalTableDimensions provides (tableDimensions ?: TableDimensions())
    ) {
        MaterialTheme(
            content = content
        )
    }
}

object TableTheme {
    val colors: TableColors
        @Composable
        get() = LocalTableColors.current
    val dimensions: TableDimensions
        @Composable
        get() = LocalTableDimensions.current
}
