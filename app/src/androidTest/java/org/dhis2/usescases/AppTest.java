package org.dhis2.usescases;

import org.dhis2.App;
import org.dhis2.data.database.DbModule;
import org.dhis2.data.server.FakeServerModule;
import org.dhis2.data.user.FakeUserModule;

public class AppTest extends App {

    @Override
    protected void setUpServerComponent() {
        serverComponent = appComponent().plus(new FakeServerModule(), new DbModule(DATABASE_NAME));
        setUpServerComponent();
    }

    @Override
    protected void setUpUserComponent() {
        userComponent = serverComponent.plus(new FakeUserModule());
    }
}
