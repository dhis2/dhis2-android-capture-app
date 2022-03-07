package org.dhis2.commons.data

data class FieldWithIssue(
    val fieldName: String,
    val issueType: IssueType,
    val message: String
)

enum class IssueType {
    ERROR,
    MANDATORY,
    WARNING
}
