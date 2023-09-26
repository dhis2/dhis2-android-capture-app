package org.dhis2.composetable.ui.compositions

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import org.dhis2.composetable.actions.DefaultValidator
import org.dhis2.composetable.actions.TableInteractions
import org.dhis2.composetable.actions.TableResizeActions
import org.dhis2.composetable.actions.Validator
import org.dhis2.composetable.model.TableCell

val LocalCurrentCellValue = compositionLocalOf<() -> String?> { { "" } }
val LocalUpdatingCell = compositionLocalOf<TableCell?> { null }
val LocalInteraction = compositionLocalOf<TableInteractions> { object : TableInteractions {} }
val LocalTableResizeActions =
    compositionLocalOf<TableResizeActions> { object : TableResizeActions {} }
val LocalValidator = staticCompositionLocalOf<Validator> { DefaultValidator() }
