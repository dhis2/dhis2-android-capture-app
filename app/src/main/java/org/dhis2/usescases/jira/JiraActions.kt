package org.dhis2.usescases.jira

import android.widget.CompoundButton

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
interface JiraActions {


    fun onSummaryChanged(s: CharSequence, start: Int, before: Int, count: Int)

    fun onDescriptionChanged(s: CharSequence, start: Int, before: Int, count: Int)

    fun onJiraUserChanged(s: CharSequence, start: Int, before: Int, count: Int)

    fun onJiraPassChanged(s: CharSequence, start: Int, before: Int, count: Int)

    fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)

    fun sendIssue()

    fun closeSession()

    fun openSession()
}
