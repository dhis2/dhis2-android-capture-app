package org.dhis2.usescases.datasets

internal val legendTestingData = listOf(
    LegendTestingData(
        valueToType = "20",
        expectedColor = "#08519C",
        expectedLabel = "Non reporting"
    ),
    LegendTestingData(
        valueToType = "45",
        expectedColor = "#3182BD",
        expectedLabel = "Low"
    ),
    LegendTestingData(
        valueToType = "61",
        expectedColor = "#6baed6",
        expectedLabel = "Medium"
    ),
    LegendTestingData(
        valueToType = "89",
        expectedColor = "#BDD7E7",
        expectedLabel = "High"
    ),
    LegendTestingData(
        valueToType = "93",
        expectedColor = "#EFF3FF",
        expectedLabel = "Excellent"
    ),
    LegendTestingData(
        valueToType = "123",
        expectedColor = "#CCCCCC",
        expectedLabel = "Invalid"
    ),
)

internal data class LegendTestingData(
    val valueToType: String,
    val expectedColor: String,
    val expectedLabel: String,
)