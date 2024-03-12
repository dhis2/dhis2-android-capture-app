package org.dhis2.usescases.enrollment

import org.dhis2.commons.data.TeiAttributesInfo
import org.junit.Assert.assertTrue
import org.junit.Test

class TeiAttributesInfoTest {
    @Test
    fun shouldReturnOneAttributeAsMainLabel() {
        val teiInfo = TeiAttributesInfo(
            attributes = listOf("attr1"),
            profileImage = "path",
            teTypeName = "name",
        )

        assertTrue(teiInfo.teiMainLabel(null) == "attr1")
        assertTrue(teiInfo.teiSecondaryLabel() == null)
    }

    @Test
    fun shouldReturnTwoAttributesAsMainLabel() {
        val teiInfo = TeiAttributesInfo(
            attributes = listOf("attr1", "attr2"),
            profileImage = "path",
            teTypeName = "name",
        )

        assertTrue(teiInfo.teiMainLabel(null) == "attr1 attr2")
        assertTrue(teiInfo.teiSecondaryLabel() == null)
    }

    @Test
    fun shouldReturnTwoAttributesAsMainLabelAndOneAttributeAsSecondaryLabel() {
        val teiInfo = TeiAttributesInfo(
            attributes = listOf("attr1", "attr2", "attr3", "attr4"),
            profileImage = "path",
            teTypeName = "name",
        )

        assertTrue(teiInfo.teiMainLabel(null) == "attr1 attr2")
        assertTrue(teiInfo.teiSecondaryLabel() == "attr3")
    }

    @Test
    fun shouldReturnTrackedEntityTypeName() {
        val teiInfo = TeiAttributesInfo(
            attributes = listOf(),
            profileImage = "path",
            teTypeName = "name",
        )

        assertTrue(teiInfo.teiMainLabel(null) == "name")
        assertTrue(teiInfo.teiSecondaryLabel() == null)
    }

    @Test
    fun shouldReturnFormattedTrackedEntityTypeName() {
        val teiInfo = TeiAttributesInfo(
            attributes = listOf(),
            profileImage = "path",
            teTypeName = "name",
        )

        assertTrue(teiInfo.teiMainLabel("%s details") == "name details")
        assertTrue(teiInfo.teiSecondaryLabel() == null)
    }

    @Test
    fun shouldReturnFormattedLabelIfNoArgumentsAvailable() {
        val teiInfo = TeiAttributesInfo(
            attributes = listOf(),
            profileImage = "path",
            teTypeName = "name",
        )

        assertTrue(teiInfo.teiMainLabel("details") == "details")
        assertTrue(teiInfo.teiSecondaryLabel() == null)
    }
}
