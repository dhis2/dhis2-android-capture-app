package org.dhis2.usescases.orgunitselector

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrgUnitSelectorTest : BaseTest() {

    @get:Rule
    val mainRule = ActivityTestRule(OUTreeActivity::class.java, false, false)

    fun startActivityWithIntent() {
        Intent().apply {
            putExtra(PROGRAM, "asd")
        }.also { mainRule.launchActivity(it) }
    }

    fun startActivityWithOutIntent() {
        mainRule.launchActivity(null)
    }

    @Test
    fun shouldOpenOrgUnitSelector() {
        //   startActivityWithOutIntent()
    }

    companion object {
        const val PROGRAM = "PROGRAM"
    }
}
