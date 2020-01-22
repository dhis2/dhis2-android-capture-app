package org.dhis2

import org.dhis2.data.database.DbModule
import org.dhis2.data.server.ServerModule
import org.hisp.dhis.android.core.D2Manager


class AppTest : App() {

    @Override
    override fun onCreate() {
    //    populateDatabaseFromAssetsIfNeeded()
    //    D2Manager.blockingInstantiateD2(ServerModule.getD2Configuration(this))
        isTesting = true
        super.onCreate()
    }

}