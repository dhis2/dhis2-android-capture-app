package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.dhis2.core.functional.Either
import org.dhis2.core.types.Tree
import org.dhis2.core.types.leaf
import org.dhis2.core.types.node
import org.dhis2.core.types.root
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModelType
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GetFeedbackTest {
    @Mock
    lateinit var teiDataRepository: TeiDataRepository

    @Mock
    lateinit var valuesRepository: ValuesRepository

    @Mock
    lateinit var dataElementRepository: DataElementRepository

    @Test
    fun `should return not found failure if It's by event and there are not events`() {
        givenThatThereNotEvents()

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)

        val feedbackResult =
            getFeedback(FeedbackMode.ByEvent, null, false)

        feedbackResult.fold(
            { failure -> Assert.assertTrue(failure is FeedbackFailure.NotFound) },
            { success -> Assert.fail("$success should be FeedbackFailure.NotFound") })
    }

    @Test
    fun `should not return feedback if It's by event and there are events without values`() {
        givenAnEventsWithoutValues()

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByEvent, null, false)

        val expectedFeedback = root(
            null, listOf<Tree.Node<FeedbackItem>>()
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> Assert.assertEquals(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should return expected feedback by events`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "CRITICAL"),
                listOf("1.1", "DE 1.1", "86%", "#FFC700", "Feedback DE 1.1", "OK", "CRITICAL"),
                listOf("1.2", "DE 1.2", "56%", "#c80f26", "Feedback DE 1.2", "OK", "CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult = getFeedback(FeedbackMode.ByEvent)

        val expectedFeedback = root(
            null, listOf(
                node(
                    FeedbackItem("ART New", null, "ART New UID"),
                    children = listOf(
                        node(
                            FeedbackItem(
                                "DE 1", FeedbackItemValue("Partly", "#FFC700", true, true, false),
                                "DE 1_UID"
                            ),
                            children = listOf(
                                leaf(FeedbackHelpItem("Feedback DE 1")),
                                node(
                                    FeedbackItem(
                                        "DE 1.1",
                                        FeedbackItemValue("86%", "#FFC700", true, true, false),
                                        "DE 1.1_UID"
                                    ),
                                    children = listOf(leaf(FeedbackHelpItem("Feedback DE 1.1")))
                                ),
                                node(
                                    FeedbackItem(
                                        "DE 1.2",
                                        FeedbackItemValue("56%", "#c80f26", true, true, false),
                                        "DE 1.2_UID"
                                    ),
                                    children = listOf(leaf(FeedbackHelpItem("Feedback DE 1.2")))
                                )
                            )
                        ),
                        node(
                            FeedbackItem(
                                "DE 2", FeedbackItemValue("100%", "#0CE922", true, true, false),
                                "DE 2_UID"
                            ),
                            children = listOf(leaf(FeedbackHelpItem("Feedback DE 2")))
                        )
                    )
                )
            )
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should not return feedback if It's by events and only failed filter is true and all values are success`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByEvent, null, true)

        val expectedFeedback = root(
            null, listOf<Tree.Node<FeedbackItem>>()
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should return only failed by events if It's by events and only failed filter is true`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "FAIL", "CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "CRITICAL"),
                listOf("3", "DE 3", "86%", "#FFC700", "Feedback DE 3", "OK", "CRITICAL"),
                listOf("4", "DE 4", "56%", "#c80f26", "Feedback DE 4", "FAIL", "CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByEvent, null, true)

        val expectedFeedback = root(
            null, listOf(
                node(
                    FeedbackItem("ART New", null, "ART New UID"),
                    children = listOf(
                        node(
                            FeedbackItem(
                                "DE 1", FeedbackItemValue("Partly", "#FFC700", false, true, false),
                                "DE 1_UID"
                            ),
                            children = listOf(leaf(FeedbackHelpItem("Feedback DE 1")))
                        ),
                        node(
                            FeedbackItem(
                                "DE 4", FeedbackItemValue("56%", "#c80f26", false, true, false),
                                "DE 4_UID"
                            ),
                            children = listOf(leaf(FeedbackHelpItem("Feedback DE 4")))
                        )
                    )
                )
            )
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should return only failed by events if It's by events and only failed filter is true and there are DE hierarchy`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "CRITICAL"),
                listOf("1.1", "DE 1.1", "86%", "#FFC700", "Feedback DE 1.1", "OK", "CRITICAL"),
                listOf("1.2", "DE 1.2", "84%", "#c80f26", "Feedback DE 1.2", "OK", "CRITICAL"),
                listOf("1.1.1", "DE 1.1.1", "56%", "#c80", "Feedback DE 1.1.1", "FAIL", "CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult = getFeedback(FeedbackMode.ByEvent, null, true)

        val expectedFeedback = root(
            null, listOf(
                node(
                    FeedbackItem("ART New", null, "ART New UID"),
                    children = listOf(
                        node(
                            FeedbackItem(
                                "DE 1", FeedbackItemValue("Partly", "#FFC700", true, true, false),
                                "DE 1_UID"
                            ),
                            children = listOf(

                                leaf(FeedbackHelpItem("Feedback DE 1")),
                                node(
                                    FeedbackItem(
                                        "DE 1.1",
                                        FeedbackItemValue("86%", "#FFC700", true, true, false),
                                        "DE 1.1_UID"
                                    ),
                                    children = listOf(

                                        leaf(FeedbackHelpItem("Feedback DE 1.1")),
                                        node(
                                            FeedbackItem(
                                                "DE 1.1.1",
                                                FeedbackItemValue(
                                                    "56%",
                                                    "#c80",
                                                    false,
                                                    true,
                                                    false
                                                ),
                                                "DE 1.1.1_UID"
                                            ),
                                            children = listOf(
                                                leaf(FeedbackHelpItem("Feedback DE 1.1.1"))
                                            )
                                        )

                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should not return feedback if It's by events and critical is true and all values are non critical`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "NON CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "NON CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByEvent, true, false)

        val expectedFeedback = root(
            null,
            listOf<Tree.Node<FeedbackItem>>()
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should not return feedback if It's by events and critical is false and all values are critical`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByEvent, false, false)

        val expectedFeedback = root(
            null,
            listOf<Tree.Node<FeedbackItem>>()
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should return only critical by events if It's by events and critical filter is true`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "NON CRITICAL"),
                listOf("3", "DE 3", "86%", "#FFC700", "Feedback DE 3", "OK", "NON CRITICAL"),
                listOf("4", "DE 4", "56%", "#c80f26", "Feedback DE 4", "FAIL", "CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByEvent, true, false)

        val expectedFeedback = root(
            null, listOf(
                node(
                    FeedbackItem("ART New", null, "ART New UID"),
                    children = listOf(
                        node(
                            FeedbackItem(
                                "DE 1", FeedbackItemValue("Partly", "#FFC700", true, true, false),
                                "DE 1_UID"
                            ),
                            children = listOf(
                                leaf(FeedbackHelpItem("Feedback DE 1"))
                            )
                        ),
                        node(
                            FeedbackItem(
                                "DE 4", FeedbackItemValue("56%", "#c80f26", false, true, false),
                                "DE 4_UID"
                            ), children = listOf(
                                leaf(FeedbackHelpItem("Feedback DE 4"))
                            )
                        )
                    )
                )
            )
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should return only non critical by events if It's by events and critical filter is false`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "NON CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "CRITICAL"),
                listOf("3", "DE 3", "86%", "#FFC700", "Feedback DE 3", "OK", "CRITICAL"),
                listOf("4", "DE 4", "56%", "#c80f26", "Feedback DE 4", "FAIL", "NON CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByEvent, false, false)

        val expectedFeedback = root(
            null, listOf(
                node(
                    FeedbackItem("ART New", null, "ART New UID"),
                    children = listOf(
                        node(
                            FeedbackItem(
                                "DE 1", FeedbackItemValue("Partly", "#FFC700", true, false, false),
                                "DE 1_UID"
                            ), children = listOf(
                                leaf(FeedbackHelpItem("Feedback DE 1"))
                            )
                        ),
                        node(
                            FeedbackItem(
                                "DE 4", FeedbackItemValue("56%", "#c80f26", false, false, false),
                                "DE 4_UID"
                            ), children = listOf(
                                leaf(FeedbackHelpItem("Feedback DE 4"))
                            )
                        )
                    )
                )
            )
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should return only critical by events if It's by events and critical filter is true and there are DE hierarchy`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "NON CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "NON CRITICAL"),
                listOf("1.1", "DE 1.1", "86%", "#FFC700", "Feedback 1.1", "FAIL", "NON CRITICAL"),
                listOf("1.2", "DE 1.2", "84%", "#c80f26", "Feedback DE 1.2", "OK", "NON CRITICAL"),
                listOf("1.1.1", "DE 1.1.1", "56%", "#c80", "Feedback DE 1.1.1", "FAIL", "CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByEvent, true, false)

        val expectedFeedback = root(
            null, listOf(
                node(
                    FeedbackItem("ART New", null, "ART New UID"),
                    children = listOf(
                        node(
                            FeedbackItem(
                                "DE 1", FeedbackItemValue("Partly", "#FFC700", true, false, false),
                                "DE 1_UID"
                            ), children = listOf(
                                leaf(FeedbackHelpItem("Feedback DE 1")),
                                node(
                                    FeedbackItem(
                                        "DE 1.1",
                                        FeedbackItemValue("86%", "#FFC700", false, false, false),
                                        "DE 1.1_UID"
                                    ), children = listOf(
                                        leaf(FeedbackHelpItem("Feedback 1.1")),
                                        node(
                                            FeedbackItem(
                                                "DE 1.1.1",
                                                FeedbackItemValue(
                                                    "56%",
                                                    "#c80",
                                                    false,
                                                    true,
                                                    false
                                                ),
                                                "DE 1.1.1_UID"
                                            ), children = listOf(
                                                leaf(FeedbackHelpItem("Feedback DE 1.1.1"))
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should return only non critical by events if It's by events and critical filter is false and there are DE hierarchy`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "CRITICAL"),
                listOf("1.1", "DE 1.1", "86%", "#FFC700", "Feedback DE 1.1", "FAIL", "CRITICAL"),
                listOf("1.2", "DE 1.2", "84%", "#c80f26", "Feedback DE 1.2", "OK", "CRITICAL"),
                listOf("1.1.1", "DE 1.1.1", "56%", "#c80", "Feedback 1.1.1", "FAIL", "NON CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByEvent, false, false)

        val expectedFeedback = root(
            null, listOf(
                node(
                    FeedbackItem("ART New", null, "ART New UID"),
                    children = listOf(
                        node(
                            FeedbackItem(
                                "DE 1", FeedbackItemValue("Partly", "#FFC700", true, true, false),
                                "DE 1_UID"
                            ), children = listOf(
                                leaf(FeedbackHelpItem("Feedback DE 1")),
                                node(
                                    FeedbackItem(
                                        "DE 1.1",
                                        FeedbackItemValue("86%", "#FFC700", false, true, false),
                                        "DE 1.1_UID"
                                    ), children = listOf(
                                        leaf(FeedbackHelpItem("Feedback DE 1.1")),
                                        node(
                                            FeedbackItem(
                                                "DE 1.1.1",
                                                FeedbackItemValue(
                                                    "56%",
                                                    "#c80",
                                                    false,
                                                    false,
                                                    false
                                                ),
                                                "DE 1.1.1_UID"
                                            ), children = listOf(
                                                leaf(FeedbackHelpItem("Feedback 1.1.1"))
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should return not found failure if It's by technical area and there are not events`() {
        givenThatThereNotEvents()

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)

        val feedbackResult =
            getFeedback(FeedbackMode.ByTechnicalArea)

        feedbackResult.fold(
            { failure -> Assert.assertTrue(failure is FeedbackFailure.NotFound) },
            { success -> Assert.fail("$success should be FeedbackFailure.NotFound") })
    }

    @Test
    fun `should not return feedback if It's by technical area and there are events without values`() {
        givenAnEventsWithoutValues()

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByTechnicalArea, null, false)

        val expectedFeedback = root(
            null, listOf<Tree.Node<FeedbackItem>>()
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> Assert.assertEquals(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should return expected feedback by technical area`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "CRITICAL"),
                listOf("1.1", "DE 1.1", "86%", "#FFC700", "Feedback DE 1.1", "OK", "CRITICAL"),
                listOf("1.2", "DE 1.2", "56%", "#c80f26", "Feedback DE 1.2", "OK", "CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult = getFeedback(FeedbackMode.ByTechnicalArea)

        val expectedFeedback = root(
            null, listOf(
                node(
                    FeedbackItem("DE 1", null, "DE 1_UID"),
                    children = listOf(
                        leaf(FeedbackHelpItem("Feedback DE 1")),
                        node(
                            FeedbackItem(
                                "ART New",
                                FeedbackItemValue("Partly", "#FFC700", true, true, false),
                                "ART New UID"
                            ), children = listOf(
                                node(
                                    FeedbackItem(
                                        "DE 1.1",
                                        FeedbackItemValue("86%", "#FFC700", true, true, false),
                                        "DE 1.1_UID"
                                    ),
                                    children = listOf(
                                        leaf(FeedbackHelpItem("Feedback DE 1.1"))
                                    )
                                ),
                                node(
                                    FeedbackItem(
                                        "DE 1.2",
                                        FeedbackItemValue("56%", "#c80f26", true, true, false),
                                        "DE 1.2_UID"
                                    ),
                                    children = listOf(
                                        leaf(FeedbackHelpItem("Feedback DE 1.2"))
                                    )
                                )
                            )
                        )
                    )
                ),
                node(
                    FeedbackItem("DE 2", null, "DE 2_UID"),
                    children = listOf(
                        leaf(FeedbackHelpItem("Feedback DE 2")),
                        node(
                            FeedbackItem(
                                "ART New",
                                FeedbackItemValue("100%", "#0CE922", true, true, false),
                                "ART New UID"
                            )
                        )
                    )
                )
            )
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should not return feedback if It's by technical area and only failed filter is true and all values are success`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByTechnicalArea, null, true)

        val expectedFeedback = root(
            null,
            listOf<Tree.Node<FeedbackItem>>()
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should return only failed by technical area if It's by technical area and only failed filter is true`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "FAIL", "CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "CRITICAL"),
                listOf("3", "DE 3", "86%", "#FFC700", "Feedback DE 3", "OK", "CRITICAL"),
                listOf("4", "DE 4", "56%", "#c80f26", "Feedback DE 4", "FAIL", "CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult =
            getFeedback(FeedbackMode.ByTechnicalArea, null, true)

        val expectedFeedback = root(
            null, listOf(
                node(
                    FeedbackItem("DE 1", null, "DE 1_UID"),
                    children = listOf(
                        leaf(FeedbackHelpItem("Feedback DE 1")),
                        node(
                            FeedbackItem(
                                "ART New",
                                FeedbackItemValue("Partly", "#FFC700", false, true, false),
                                "ART New UID"
                            )
                        )
                    )
                ),
                node(
                    FeedbackItem("DE 4", null, "DE 4_UID"),
                    children = listOf(
                        leaf(FeedbackHelpItem("Feedback DE 4")),
                        node(
                            FeedbackItem(
                                "ART New",
                                FeedbackItemValue("56%", "#c80f26", false, true, false),
                                "ART New UID"
                            )
                        )
                    )
                )
            )
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    @Test
    fun `should return only failed by technical area if by technical area and only failed filter is true and there are DE hierarchy`() {
        givenOneEventWithValues(
            "ART New", listOf(
                listOf("1", "DE 1", "Partly", "#FFC700", "Feedback DE 1", "OK", "CRITICAL"),
                listOf("2", "DE 2", "100%", "#0CE922", "Feedback DE 2", "OK", "CRITICAL"),
                listOf("1.1", "DE 1.1", "86%", "#FFC700", "Feedback DE 1.1", "FAIL", "CRITICAL"),
                listOf("1.2", "DE 1.2", "84%", "#c80f26", "Feedback DE 1.2", "OK", "CRITICAL"),
                listOf("1.1.1", "DE 1.1.1", "56%", "#c80f26", "Feedback DE 1.1.1", "OK", "CRITICAL")
            )
        )

        val getFeedback = GetFeedback(teiDataRepository, dataElementRepository, valuesRepository)
        val feedbackResult = getFeedback(FeedbackMode.ByTechnicalArea, null, true)

        val expectedFeedback = root(
            null, listOf(
                node(
                    FeedbackItem("DE 1", null, "DE 1_UID"),
                    children = listOf(
                        leaf(FeedbackHelpItem("Feedback DE 1")),
                        node(
                            FeedbackItem(
                                "ART New",
                                FeedbackItemValue("Partly", "#FFC700", true, true, false),
                                "ART New UID"
                            ), children = listOf(
                                node(
                                    FeedbackItem(
                                        "DE 1.1",
                                        FeedbackItemValue("86%", "#FFC700", false, true, false),
                                        "DE 1.1_UID"
                                    ),
                                    children = listOf(
                                        leaf(FeedbackHelpItem("Feedback DE 1.1"))
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        feedbackResult.fold(
            { failure -> Assert.fail("$failure should be success") },
            { feedback -> assertFeedback(expectedFeedback, feedback.data) })
    }

    private fun assertFeedback(
        expectedFeedback: Tree.Root<*>,
        feedback: Tree.Root<*>
    ) {
        val gson = GsonBuilder().setPrettyPrinting().create()

        Assert.assertEquals(
            "AssertionError: \n " +
                "Expected: \n ${gson.toJson(expectedFeedback)} \n" +
                "Actual: \n ${gson.toJson(feedback)}  \n",
            expectedFeedback, feedback
        )
    }

    private fun givenThatThereNotEvents() {
        whenever(
            teiDataRepository.getTEIEnrollmentEvents(
                null, false, mutableListOf(), mutableListOf(),
                mutableListOf(), false, mutableListOf(), mutableListOf(), null, true
            )
        ).thenReturn(Single.just(listOf()))
    }

    private fun givenAnEventsWithoutValues() {
        whenever(
            dataElementRepository.getWithFeedbackOrderByProgramStage(any())
        ).thenReturn(Either.Right(listOf()))

        val events = listOf(
            EventViewModel(
                EventViewModelType.EVENT,
                ProgramStage.builder().displayName("EVENT1").uid("STAGE_UID").build(),
                Event.builder().uid("EVENT1_UID").build(),
                0,
                null,
                true,
                true,
                "",
                "",
                listOf(),
                null,
                false,
                false,
                false,
                ""
            ),
            EventViewModel(
                EventViewModelType.EVENT,
                ProgramStage.builder().displayName("EVENT2").uid("STAGE_UID").build(),
                Event.builder().uid("EVENT2_UID").build(),
                0,
                null,
                true,
                true,
                "",
                "",
                listOf(),
                null,
                false,
                false,
                false,
                ""
            )
        )

        whenever(
            teiDataRepository.getTEIEnrollmentEvents(
                null, false, mutableListOf(), mutableListOf(),
                mutableListOf(), false, mutableListOf(), mutableListOf(), null, true
            )
        ).thenReturn(Single.just(events))

        events.forEach {
            whenever(valuesRepository.getByEvent(it.event!!.uid())).thenReturn(listOf())
        }
    }

    private fun givenOneEventWithValues(stageName: String, valuesData: List<List<String>>) {
        whenever(
            dataElementRepository.getWithFeedbackOrderByProgramStage(any())
        ).thenReturn(Either.Right(listOf()))

        whenever(
            teiDataRepository.getTEIEnrollmentEvents(
                null, false, mutableListOf(), mutableListOf(),
                mutableListOf(), false, mutableListOf(), mutableListOf(), null, true
            )
        ).thenReturn(
            Single.just(
                listOf(
                    EventViewModel(
                        EventViewModelType.EVENT,
                        ProgramStage.builder().displayName(stageName).uid("STAGE_UID").build(),
                        Event.builder().uid("$stageName UID").build(),
                        0,
                        null,
                        true,
                        true,
                        "",
                        "",
                        listOf(),
                        null,
                        false,
                        false,
                        false,
                        ""
                    )
                )
            )
        )

        val values = valuesData.map {
            Value(
                "${it[1]}_UID", it[1], it[2], FeedbackOrder(it[0]), it[3], it[4],
                it[5] != "FAIL", it[6] == "CRITICAL", "$stageName UID", false
            )
        }

        whenever(valuesRepository.getByEvent("$stageName UID")).thenReturn(values)
    }
}