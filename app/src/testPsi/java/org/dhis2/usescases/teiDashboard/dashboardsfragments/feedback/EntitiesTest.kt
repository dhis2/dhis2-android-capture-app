package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import org.junit.Assert
import org.junit.Test

class FeedbackUtilsTest {
    @Test
    fun `FeedbackOrder should return expected null parent from a root`() {
        val feedbackOrder = FeedbackOrder("1")

        Assert.assertNull(feedbackOrder.parent)
    }

    @Test
    fun `FeedbackOrder should ignore spaces to the begin of the text`() {
        val feedbackOrder = FeedbackOrder(" 1.2")

        Assert.assertEquals("1", feedbackOrder.parent)
    }

    @Test
    fun `FeedbackOrder should ignore spaces to the end of the text`() {
        val feedbackOrder = FeedbackOrder("1.2 ")

        Assert.assertEquals("1", feedbackOrder.parent)
    }

    @Test
    fun `FeedbackOrder should return expected parent from a non root of level 1`() {
        val feedbackOrder = FeedbackOrder("1.2")

        Assert.assertEquals("1", feedbackOrder.parent)
    }

    @Test
    fun `FeedbackOrder should return expected parent from a non root of level 4`() {
        val feedbackOrder = FeedbackOrder("1.2.1.2")

        Assert.assertEquals("1.2.1", feedbackOrder.parent)
    }

    @Test
    fun `FeedbackOrder list Should sort with expected order with items for the same level`() {
        val feedbackOrders = listOf(
            FeedbackOrder("1.2.1.2"), FeedbackOrder("1.2.1.1")
        )

        val orderedValues = feedbackOrders.sorted()

        Assert.assertEquals(
            listOf(
                FeedbackOrder("1.2.1.1"), FeedbackOrder("1.2.1.2")
            ), orderedValues
        )
    }

    @Test
    fun `FeedbackOrder list Should sort with expected order with items for the distinct level`() {
        val feedbackOrders = listOf(
            FeedbackOrder("1.2.1.1"), FeedbackOrder("1.2.1")
        )

        val orderedValues = feedbackOrders.sorted()

        Assert.assertEquals(
            listOf(
                FeedbackOrder("1.2.1"), FeedbackOrder("1.2.1.1")
            ), orderedValues
        )
    }

    @Test
    fun `FeedbackOrder list Should sort with expected order with items`() {
        val feedbackOrders = listOf(
            FeedbackOrder("20"), FeedbackOrder("20.1"),
            FeedbackOrder("1"), FeedbackOrder("1.1"),
            FeedbackOrder("1.1.1"), FeedbackOrder("2.1"),
            FeedbackOrder("1.2"), FeedbackOrder("1.2.1"),
            FeedbackOrder("2"), FeedbackOrder("1.1.2"),
            FeedbackOrder("10"), FeedbackOrder("10.1"),
            FeedbackOrder("3"), FeedbackOrder("3.1"),
            FeedbackOrder("2.1.1"), FeedbackOrder("2.1.2")
        )

        val orderedValues = feedbackOrders.sorted()

        Assert.assertEquals(
            listOf(
                FeedbackOrder("1"), FeedbackOrder("1.1"),
                FeedbackOrder("1.1.1"), FeedbackOrder("1.1.2"),
                FeedbackOrder("1.2"), FeedbackOrder("1.2.1"),
                FeedbackOrder("2"), FeedbackOrder("2.1"),
                FeedbackOrder("2.1.1"), FeedbackOrder("2.1.2"),
                FeedbackOrder("3"), FeedbackOrder("3.1"),
                FeedbackOrder("10"), FeedbackOrder("10.1"),
                FeedbackOrder("20"), FeedbackOrder("20.1")
            ), orderedValues
        )
    }

    @Test
    fun `FeedbackOrder list Should sort with expected order with items for level 0`() {
        val feedbackOrders = listOf(
            FeedbackOrder("1"), FeedbackOrder("7"), FeedbackOrder("4"),
            FeedbackOrder("5"), FeedbackOrder("6")
        )

        val orderedValues = feedbackOrders.sortedBy { it }

        Assert.assertEquals(
            listOf(
                FeedbackOrder("1"), FeedbackOrder("4"), FeedbackOrder("5")
                , FeedbackOrder("6"), FeedbackOrder("7")
            ), orderedValues
        )
    }
}
