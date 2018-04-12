package com.dhis2.usescases.syncManager;

import com.dhis2.usescases.general.AbstractActivityContracts;

/**
 * Created by lmartin on 21/03/2018.
 */

public class SyncManagerContracts {

    interface View extends AbstractActivityContracts.View {


    }

    public interface Presenter {

        void sync();

    }
}
