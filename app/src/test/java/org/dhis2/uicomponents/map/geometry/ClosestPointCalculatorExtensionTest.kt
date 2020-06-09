package org.dhis2.uicomponents.map.geometry

import org.junit.Assert.assertTrue
import org.junit.Test

class ClosestPointCalculatorExtensionTest {

    @Test
    fun `Should select closest polygon point to point`() {
        val testingPolygon = listOf(
            listOf(
                listOf(0.0, 0.0),
                listOf(0.0, 1.0),
                listOf(1.0, 1.0),
                listOf(1.0, 0.0)
            )
        )

        val point = listOf(0.25, 0.25)

        val result = testingPolygon.closestPointTo(point)
        assertTrue(result[0] == 0.0 && result[1] == 0.0)
    }

    @Test
    fun `Should select closest polygon points`() {
        val testingPolygonA = listOf(
            listOf(
                listOf(0.0, 0.0),
                listOf(0.0, 1.0),
                listOf(1.0, 1.0),
                listOf(1.0, 0.0)
            )
        )

        val testingPolygonB = listOf(
            listOf(
                listOf(2.0, 1.0),
                listOf(3.0, 1.0),
                listOf(2.5, 2.0)
            )
        )

        val result = testingPolygonA.closestPointTo(testingPolygonB)
        assertTrue(
            result.first[0] == 1.0 &&
            result.first[1] == 1.0 &&
            result.second[0] == 2.0 &&
            result.second[1] == 1.0
        )
    }
}