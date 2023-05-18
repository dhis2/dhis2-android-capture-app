package org.dhis2.composetable

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ColumnTableTest::class,
    RowTableTest::class,
    CellTableTest::class,
    BottomBarTableTests::class
)
class ComposeTableTestSuite
