package org.dhis2.commons.dialogs.bottomsheet

data class FieldWithIssue(
    val fieldUid: String,
    val fieldName: String,
    val issueType: IssueType,
    val message: String,
)

enum class IssueType {
    ERROR,
    MANDATORY,
    WARNING,
    ERROR_ON_COMPLETE,
    WARNING_ON_COMPLETE,
    ;

    fun shouldShowError() = this == ERROR || this == ERROR_ON_COMPLETE || this == MANDATORY
}
