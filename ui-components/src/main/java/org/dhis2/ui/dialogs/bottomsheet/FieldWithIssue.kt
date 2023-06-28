package org.dhis2.ui.dialogs.bottomsheet

data class FieldWithIssue(
    val fieldUid: String,
    val fieldName: String,
    val issueType: IssueType,
    val message: String
)

enum class IssueType {
    ERROR,
    MANDATORY,
    WARNING,
    ERROR_ON_COMPLETE,
    WARNING_ON_COMPLETE
}
