package org.dhis2.composetable.ui.semantics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.unit.dp

const val ROW_TEST_TAG = "ROW_TEST_TAG_"
const val CELL_TEST_TAG = "CELL_TEST_TAG_"
const val INFO_ICON = "infoIcon"
const val HEADER_CELL = "HEADER_CELL"
const val MANDATORY_ICON_TEST_TAG = "MANDATORY_ICON_TEST_TAG"
const val CELL_VALUE_TEST_TAG = "CELL_VALUE_TEST_TAG"
const val CELL_ERROR_UNDERLINE_TEST_TAG = "CELL_ERROR_UNDERLINE_TEST_TAG"
val MAX_CELL_WIDTH_SPACE = 96.dp

/* Row Header Cell */
val InfoIconId = SemanticsPropertyKey<String>("InfoIconId")
var SemanticsPropertyReceiver.infoIconId by InfoIconId
val TableId = SemanticsPropertyKey<String>("TableId")
var SemanticsPropertyReceiver.tableIdSemantic by TableId
val RowIndex = SemanticsPropertyKey<Int?>("RowIndex")
var SemanticsPropertyReceiver.rowIndexSemantic by RowIndex
val RowBackground = SemanticsPropertyKey<Color>("RowBackground")
var SemanticsPropertyReceiver.rowBackground by RowBackground

/* Column Header Cell */
val ColumnBackground = SemanticsPropertyKey<Color>("ColumnBackground")
var SemanticsPropertyReceiver.columnBackground by ColumnBackground
val ColumnIndexHeader = SemanticsPropertyKey<Int>("ColumnIndexHeader")
var SemanticsPropertyReceiver.columnIndexHeader by ColumnIndexHeader
val RowIndexHeader = SemanticsPropertyKey<Int>("RowIndexHeader")
var SemanticsPropertyReceiver.rowIndexHeader by RowIndexHeader
val TableIdColumnHeader = SemanticsPropertyKey<String>("TableIdColumnHeader")
var SemanticsPropertyReceiver.tableIdColumnHeader by TableIdColumnHeader

/* Cell */
val CellSelected = SemanticsPropertyKey<Boolean>("CellSelected")
var SemanticsPropertyReceiver.cellSelected by CellSelected
val HasError = SemanticsPropertyKey<Boolean>("HasError")
var SemanticsPropertyReceiver.hasError by HasError
val IsBlocked = SemanticsPropertyKey<Boolean>("IsBlocked")
var SemanticsPropertyReceiver.isBlocked by IsBlocked
