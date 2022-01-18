package org.dhis2.usescases

import org.dhis2.usescases.flow.searchFlow.SearchFlowTest
import org.dhis2.usescases.flow.syncFlow.SyncFlowTest
import org.dhis2.usescases.flow.teiFlow.TeiFlowTest
import org.dhis2.usescases.form.FormTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    SearchFlowTest::class,
    SyncFlowTest::class,
    TeiFlowTest::class,
    FormTest::class
)
class FlowTestsSuite
