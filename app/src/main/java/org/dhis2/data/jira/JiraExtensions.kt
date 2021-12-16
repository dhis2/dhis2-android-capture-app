package org.dhis2.data.jira

const val BASIC = "Basic %s"

fun String.jiraBugFormated() = DEFAULT_BUG_TEMPLATE.format(this)

fun String.toJiraJql() = "(project=10200 AND reporter=$this AND issueType=10006) order by created"

fun String.toJiraIssueUri() = "https://jira.dhis2.org/browse/$this"

fun String.toBasicAuth() = BASIC.format(this)
