package org.dhis2.usescases.jira;

import android.widget.CompoundButton;

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
public interface JiraActions {


    void onSummaryChanged(CharSequence s, int start, int before, int count);

    void onDescriptionChanged(CharSequence s, int start, int before, int count);

    void onJiraUserChanged(CharSequence s, int start, int before, int count);

    void onJiraPassChanged(CharSequence s, int start, int before, int count);

    void onCheckedChanged(CompoundButton buttonView, boolean isChecked);

    void sendIssue();

    void closeSession();

    void openSession();
}
