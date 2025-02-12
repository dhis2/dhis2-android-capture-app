package org.dhis2.usescases

import org.dhis2.usescases.about.AboutTest
import org.dhis2.usescases.datasets.DataSetTest
import org.dhis2.usescases.enrollment.EnrollmentTest
import org.dhis2.usescases.event.EventTest
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.EventInitialTest
import org.dhis2.usescases.login.LoginTest
import org.dhis2.usescases.main.MainTest
import org.dhis2.usescases.pin.PinTest
import org.dhis2.usescases.programevent.ProgramEventTest
import org.dhis2.usescases.searchte.SearchTETest
import org.dhis2.usescases.settings.SettingsTest
import org.dhis2.usescases.sync.SyncActivityTest
import org.dhis2.usescases.teidashboard.TeiDashboardTest
import org.dhis2.usescases.teidashboard.dialogs.scheduling.SchedulingDialogUiTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    AboutTest::class,
    DataSetTest::class,
    EnrollmentTest::class,
    EventInitialTest::class,
    EventTest::class,
    LoginTest::class,
    MainTest::class,
    PinTest::class,
    ProgramEventTest::class,
    SearchTETest::class,
    SettingsTest::class,
    SyncActivityTest::class,
    TeiDashboardTest::class,
    SchedulingDialogUiTest::class,
)
class UseCaseTestsSuite
