package org.dhis2.mobileProgramRules

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitGroup
import org.hisp.dhis.android.core.program.ProgramRule
import org.hisp.dhis.android.core.program.ProgramRuleAction
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

class RulesRepositoryTest {
    private lateinit var repository: RulesRepository

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setUp() {
        repository = RulesRepository(d2)
    }

    @Test
    fun `Should load supplementary data`() {
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .withOrganisationUnitGroups()
                .uid("org_unit_test")
                .blockingGet(),
        ) doReturn getTestOrgUnit()
        whenever(d2.userModule().userRoles().blockingGetUids()) doReturn getTestUserRoles()
        whenever(d2.userModule().userGroups().blockingGetUids()) doReturn getTestUserGroups()

        val supplData =
            runBlocking {
                repository.supplementaryData("org_unit_test")
            }
        assertTrue(supplData.userRoles.isNotEmpty())
        assertTrue(supplData.userGroups.isNotEmpty())
        assertTrue(supplData.orgUnitGroups.containsKey("org_unit_group_test_code"))
        assertTrue(supplData.orgUnitGroups.containsKey("org_unit_group_test"))
        assertTrue(supplData.userRoles.contains("UtXToHNI0Cb"))
        assertTrue(supplData.userRoles.contains("oPNOIj7zJ1m"))
        assertTrue(supplData.userGroups.contains("gVC8vCfNAx8"))
        assertTrue(supplData.userGroups.contains("Kk12LkEWtXp"))
        assertTrue(
            supplData.orgUnitGroups
                .getOrElse("org_unit_group_test") { arrayListOf() }
                .contains("org_unit_test"),
        )
        assertTrue(
            supplData.orgUnitGroups
                .getOrElse("org_unit_group_test_code") { arrayListOf() }
                .contains("org_unit_test"),
        )
    }

    @Test
    fun `Supplementary data should not include option groups with null code`() {
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .withOrganisationUnitGroups()
                .uid("org_unit_test")
                .blockingGet(),
        ) doReturn getTestOrgUnitWithNullCodeGroup()
        whenever(d2.userModule().userRoles().blockingGetUids()) doReturn getTestUserRoles()
        whenever(d2.userModule().userGroups().blockingGetUids()) doReturn getTestUserGroups()

        val supplData =
            runBlocking {
                repository.supplementaryData("org_unit_test")
            }

        assertTrue(supplData.userRoles.isNotEmpty())
        assertTrue(!supplData.orgUnitGroups.containsKey("org_unit_group_test_code"))
        assertTrue(supplData.orgUnitGroups.containsKey("org_unit_group_test"))
        assertTrue(supplData.userRoles.contains("UtXToHNI0Cb"))
        assertTrue(supplData.userRoles.contains("oPNOIj7zJ1m"))
        assertTrue(
            supplData.orgUnitGroups
                .getOrElse("org_unit_group_test") { arrayListOf() }
                .contains("org_unit_test"),
        )
        assertTrue(
            supplData.orgUnitGroups
                .getOrElse("org_unit_group_test_code") { arrayListOf() }
                .isEmpty(),
        )
    }

    @Test
    fun shouldSkipDisplayActionsRules() = runTest {
        val programUid = "program_uid"
        val testingRules = listOf(
            ProgramRule.builder()
                .uid("uid_1")
                .name("name_1")
                .condition("true")
                .programRuleActions(
                    listOf(
                        ProgramRuleAction.builder()
                            .uid("action_1")
                            .programRuleActionType(ProgramRuleActionType.DISPLAYTEXT)
                            .location("feedback")
                            .displayContent("Content A")
                            .build(),
                    ),
                )
                .build(),
            ProgramRule.builder()
                .uid("uid_2")
                .name("name_2")
                .condition("true")
                .programRuleActions(
                    listOf(
                        ProgramRuleAction.builder()
                            .uid("action_2")
                            .programRuleActionType(ProgramRuleActionType.WARNINGONCOMPLETE)
                            .displayContent("Warning")
                            .build(),
                    ),
                )
                .build(),
            ProgramRule.builder()
                .uid("uid_3")
                .name("name_3")
                .condition("true")
                .programRuleActions(
                    listOf(
                        ProgramRuleAction.builder()
                            .uid("action_3")
                            .data("key B")
                            .programRuleActionType(ProgramRuleActionType.DISPLAYKEYVALUEPAIR)
                            .location("feedback")
                            .displayContent("Content B")
                            .build(),
                    ),
                )
                .build(),
            ProgramRule.builder()
                .uid("uid_4")
                .name("name_4")
                .condition("true")
                .programRuleActions(
                    listOf(
                        ProgramRuleAction.builder()
                            .uid("action_4")
                            .programRuleActionType(ProgramRuleActionType.DISPLAYTEXT)
                            .location("feedback")
                            .displayContent("Content C")
                            .build(),
                        ProgramRuleAction.builder()
                            .uid("action_5")
                            .trackedEntityAttribute(ObjectWithUid.create("attribute_uid"))
                            .data("Hello there")
                            .programRuleActionType(ProgramRuleActionType.SHOWWARNING)
                            .displayContent("Content D")
                            .build(),
                    ),
                )
                .build(),
        )
        whenever(
            d2.programModule().programRules()
                .byProgramUid()
        ) doReturn mock()
        whenever(
            d2.programModule().programRules()
                .byProgramUid().eq(programUid)
        ) doReturn mock()

        whenever(
            d2.programModule().programRules()
                .byProgramUid().eq(programUid)
                .withProgramRuleActions()
        ) doReturn mock()

        whenever(
            d2.programModule().programRules()
                .byProgramUid().eq(programUid)
                .withProgramRuleActions()
                .blockingGet()
        ) doReturn testingRules

        val resultWithoutSkipping = repository.rules(
            programUid = programUid,
            skipDiplayRules = false,
        )

        assertTrue(resultWithoutSkipping.size == 4)

        val resultWithSkipping = repository.rules(
            programUid = programUid,
            skipDiplayRules = true,
        )

        assertTrue(resultWithSkipping.size == 2)
    }

    private fun getTestUserRoles(): List<String> = arrayListOf("UtXToHNI0Cb", "oPNOIj7zJ1m")

    private fun getTestUserGroups(): List<String> = listOf("gVC8vCfNAx8", "Kk12LkEWtXp")

    private fun getTestOrgUnit(): OrganisationUnit =
        OrganisationUnit
            .builder()
            .uid("org_unit_test")
            .organisationUnitGroups(arrayListOf(getTestOrgUnitGroup("org_unit_group_test_code")))
            .build()

    private fun getTestOrgUnitWithNullCodeGroup(): OrganisationUnit =
        OrganisationUnit
            .builder()
            .uid("org_unit_test")
            .organisationUnitGroups(arrayListOf(getTestOrgUnitGroup()))
            .build()

    private fun getTestOrgUnitGroup(ouCode: String? = null): OrganisationUnitGroup? =
        OrganisationUnitGroup
            .builder()
            .uid("org_unit_group_test")
            .code(ouCode)
            .build()
}
