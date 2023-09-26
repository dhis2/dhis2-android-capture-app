package org.dhis2.composetable.actions

import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.ValidationResult

interface Validator {
    fun validate(tableCell: TableCell): ValidationResult {
        return ValidationResult.Success(tableCell.value)
    }
}
