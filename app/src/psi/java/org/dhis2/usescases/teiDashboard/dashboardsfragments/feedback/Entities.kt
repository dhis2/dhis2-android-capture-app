package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

data class FeedbackOrder(val value: String) : Comparable<FeedbackOrder> {
    private val orderItems: List<Int>
    val level: Int
    val warnings: List<String>
    val lastNumber: Int

    init {
        warnings = validate(value)
        orderItems = value.trim().split(".").map { it.toInt() }
        level = orderItems.size - 1
        lastNumber = orderItems.last()
    }

    val parent =
        if (orderItems.size == 1)
            null
        else
            orderItems.subList(0, orderItems.size - 1)
                .joinToString(".")

    fun isDescendantOf(feedbackOrder: FeedbackOrder): Boolean {
        return this.value.startsWith(feedbackOrder.value) && this != feedbackOrder
    }

    private fun validate(value: String): List<String> {
        return if (value.contains(" ")) {
            listOf(FeedbackOrderWithSpaces)
        } else {
            listOf()
        }
    }

    override fun compareTo(other: FeedbackOrder): Int {
        var result: Int? = null
        var position = 0

        val indexExists = { list: List<Int>, index: Int ->
            index >= 0 && index < list.size
        }

        while (result == null) {

            if (!indexExists(this.orderItems, position) && !indexExists(
                    other.orderItems,
                    position
                )
            ) {
                return 0 // equal
            } else if (!indexExists(this.orderItems, position)) {
                return -1 // this is minor
            } else if (!indexExists(other.orderItems, position)) {
                return 1 // other is minor
            } else if (this.orderItems[position] != other.orderItems[position]) {
                result = this.orderItems[position] - other.orderItems[position]
            }

            position++
        }

        return result
    }
}

const val FeedbackOrderWithSpaces = "feedback_order_with_spaces"
const val FeedbackOrderInvalid = "feedback_order_invalid"
const val FeedbackOrderDuplicate = "feedback_order_duplicate"
const val FeedbackOrderGap = "feedback_order_gap"

sealed class Validation {
    data class DataElementWarning(
        val dataElement: String,
        val message: String
    ) : Validation()

    data class DataElementError(
        val dataElement: String,
        val message: String
    ) : Validation()

    data class ProgramStageWarning(
        val programStage: String,
        val message: String
    ) : Validation()
}

data class Value(
    val dataElement: String,
    val name: String,
    val value: String,
    val feedbackOrder: FeedbackOrder,
    val colorByLegend: String? = null,
    val feedbackHelp: String? = null,
    val success: Boolean,
    val critical: Boolean,
    val eventUid: String,
    val isNumeric: Boolean
)

data class DataElement(val uid: String, val feedbackOrder: FeedbackOrder?)

data class Event(
    val uid: String,
    val name: String,
    val programStageUid: String,
    val values: List<Value> = listOf()
)

data class MultilingualFeedback(val text: String, val locale: String)

data class Hnqis2Metadata(val isCritical: String)

data class ResponseWithValidations<T>(val data: T, val validations: List<Validation>)
