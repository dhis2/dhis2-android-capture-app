package org.dhis2.composetable.semantics

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import org.dhis2.composetable.ui.INFO_ICON
import org.dhis2.composetable.ui.infoIconId
import org.dhis2.composetable.ui.rowBackground
import org.dhis2.composetable.ui.rowIndexSemantic
import org.dhis2.composetable.ui.tableIdSemantic

fun Modifier.withItemHeaderSemantics(
    tableId: String,
    rowIndex: Int?,
    showDecoration: Boolean,
    cellBackgroundColor: Color
) = this.then(
    semantics {
        tableIdSemantic = tableId
        rowIndex?.let { rowIndexSemantic = rowIndex }
        infoIconId = if (showDecoration) INFO_ICON else ""
        rowBackground = cellBackgroundColor
    }
        .testTag("$tableId$rowIndex")
)
