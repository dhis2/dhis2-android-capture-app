package org.dhis2.usescases.enrollment

import org.junit.Assert.assertTrue
import org.junit.Test

class TeiInfoTest {
    @Test
    fun shouldReturnOneAttributeAsMainLabel() {
        val teiInfo = TeiInfo(
            attributes = listOf("attr1"),
            profileImage = "path",
            teTypeName = "name"
        )

        assertTrue(teiInfo.teiMainLabel(null) == "attr1")
        assertTrue(teiInfo.teiSecondaryLabel() == null)
    }

    @Test
    fun shouldReturnTwoAttributesAsMainLabel() {
        val teiInfo = TeiInfo(
            attributes = listOf("attr1", "attr2"),
            profileImage = "path",
            teTypeName = "name"
        )

        assertTrue(teiInfo.teiMainLabel(null) == "attr1 attr2")
        assertTrue(teiInfo.teiSecondaryLabel() == null)
    }

    @Test
    fun shouldReturnTwoAttributesAsMainLabelAndOneAttributeAsSecondaryLabel() {
        val teiInfo = TeiInfo(
            attributes = listOf("attr1", "attr2", "attr3", "attr4"),
            profileImage = "path",
            teTypeName = "name"
        )

        assertTrue(teiInfo.teiMainLabel(null) == "attr1 attr2")
        assertTrue(teiInfo.teiSecondaryLabel() == "attr3")
    }

    @Test
    fun shouldReturnTrackedEntityTypeName() {
        val teiInfo = TeiInfo(
            attributes = listOf(),
            profileImage = "path",
            teTypeName = "name"
        )

        assertTrue(teiInfo.teiMainLabel(null) == "name")
        assertTrue(teiInfo.teiSecondaryLabel() == null)
    }

    @Test
    fun shouldReturnFormattedTrackedEntityTypeName() {
        val teiInfo = TeiInfo(
            attributes = listOf(),
            profileImage = "path",
            teTypeName = "name"
        )

        assertTrue(teiInfo.teiMainLabel("%s details") == "name details")
        assertTrue(teiInfo.teiSecondaryLabel() == null)
    }

    @Test
    fun shouldReturnFormattedLabelIfNoArgumentsAvailable() {
        val teiInfo = TeiInfo(
            attributes = listOf(),
            profileImage = "path",
            teTypeName = "name"
        )

        assertTrue(teiInfo.teiMainLabel("details") == "details")
        assertTrue(teiInfo.teiSecondaryLabel() == null)
    }
}
