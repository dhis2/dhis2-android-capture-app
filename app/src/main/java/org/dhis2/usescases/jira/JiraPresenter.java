package org.dhis2.usescases.jira;

import org.dhis2.usescases.general.ActivityGlobalAbstract;

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

public interface JiraPresenter {

    void init(ActivityGlobalAbstract context);

    void onSendClick();

    void onSummaryChanged(CharSequence s, int start, int before, int count);

    void onDescriptionChanged(CharSequence s, int start, int before, int count);

    void onJiraUserChanged(CharSequence s, int start, int before, int count);

    void onJiraPassChanged(CharSequence s, int start, int before, int count);
}
