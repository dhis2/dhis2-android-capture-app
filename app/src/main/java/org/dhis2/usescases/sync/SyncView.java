package org.dhis2.usescases.sync;

import org.dhis2.usescases.general.AbstractActivityContracts;

interface SyncView extends AbstractActivityContracts.View {

    void saveTheme(String themeColor);

    void saveFlag(String flag);
}
