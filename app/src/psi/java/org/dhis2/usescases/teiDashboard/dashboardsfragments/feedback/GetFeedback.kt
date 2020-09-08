package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import org.dhis2.core.functional.Either
import org.dhis2.core.types.TreeNode
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel
import java.lang.IllegalStateException

sealed class FeedbackFailure {
    object NotFound : FeedbackFailure()
    data class UnexpectedError(val error: Exception) : FeedbackFailure()
}

sealed class FeedbackMode {
    data class ByEvent(val criticalFilter: Boolean? = null) : FeedbackMode()
    object ByTechnicalArea : FeedbackMode()
}

class GetFeedback(
    private val teiDataRepository: TeiDataRepository,
    private val valuesRepository: ValuesRepository
) {
    operator fun invoke(feedbackMode: FeedbackMode): Either<FeedbackFailure, List<TreeNode<FeedbackItem>>> {
        return try {
            val compulsoryFilter = when (feedbackMode) {
                is FeedbackMode.ByEvent -> feedbackMode.criticalFilter
                is FeedbackMode.ByTechnicalArea -> null
            }

            val events = getEnrollmentEvents(compulsoryFilter)

            if (events.isEmpty()) {
                Either.Left(FeedbackFailure.NotFound)
            } else {
                Either.Right(createFeedback(feedbackMode, events))
            }
        } catch (e: Exception) {
            Either.Left(FeedbackFailure.UnexpectedError(e))
        }
    }

    private fun createFeedback(
        feedbackMode: FeedbackMode,
        teiEvents: List<Event>
    ): List<TreeNode<FeedbackItem>> {
        return when (feedbackMode) {
            is FeedbackMode.ByEvent -> createFeedbackByEvent(teiEvents)
            is FeedbackMode.ByTechnicalArea -> createFeedbackByTechnicalAre(teiEvents)
        }
    }

    private fun createFeedbackByEvent(
        teiEvents: List<Event>
    ): List<TreeNode<FeedbackItem>> {

        val teiEventsWithValues = teiEvents.filter { it.values.isNotEmpty() }

        return teiEventsWithValues.map { event ->
            val children = mapToTreeNodes(event.values)

            TreeNode.Node(FeedbackItem(event.name, null, event.uid), children)
        }
    }

    private fun createFeedbackByTechnicalAre(
        teiEvents: List<Event>
    ): List<TreeNode<FeedbackItem>> {

        val distinctValues = teiEvents.flatMap { it.values }.distinctBy { it.dataElement }
        val treeNodes = mapToTreeNodes(distinctValues, false)

        addEventsToLastNodes(treeNodes, teiEvents)

        return treeNodes
    }

    private fun mapToTreeNodes(
        values: List<Value>,
        withValue: Boolean = true
    ): List<TreeNode<FeedbackItem>> {
        val treeNodes = mutableListOf<TreeNode<FeedbackItem>>()

        val nodesMap = values.associate { it.feedbackOrder.value to mapToTreeNode(it, withValue) }

        values.sortedBy { it.feedbackOrder }.forEach { eventValue ->
            val currentNode = nodesMap[eventValue.feedbackOrder.value]!!

            if (eventValue.feedbackOrder.parent == null) {
                treeNodes.add(currentNode)
            } else {
                val currentParent = nodesMap[eventValue.feedbackOrder.parent]

                if (currentParent == null) {
                    throw IllegalStateException("The data element with order ${eventValue.feedbackOrder.value} has not parent")
                } else {
                    currentParent.addChild(currentNode)
                }
            }
        }

        return treeNodes
    }

    private fun addEventsToLastNodes(treeNodes: List<TreeNode<FeedbackItem>>, events: List<Event>) {
        treeNodes.forEach { treeNode ->
            if (treeNode is TreeNode.Node &&
                (treeNode.children.isEmpty() || treeNode.children[0] is TreeNode.Leaf)
            ) {
                events.filter { event ->
                    event.values.any { v -> v.dataElement == treeNode.content.code }
                }.map { event ->
                    val eventValue = event.values.first { it.dataElement == treeNode.content.code }

                    FeedbackItem(
                        event.name,
                        FeedbackItemValue(eventValue.value, eventValue.colorByLegend),
                        event.uid
                    )
                }.forEach {
                    treeNode.addChild(TreeNode.Node(it))
                }
            } else if (treeNode is TreeNode.Node) {
                addEventsToLastNodes(treeNode.children as List<TreeNode<FeedbackItem>>, events)
            }
        }
    }

    private fun mapToTreeNode(
        eventValue: Value,
        withValue: Boolean = true
    ): TreeNode.Node<FeedbackItem> {
        return TreeNode.Node(
            FeedbackItem(
                eventValue.name,
                if (withValue) FeedbackItemValue(
                    eventValue.value,
                    eventValue.colorByLegend
                ) else null,
                eventValue.dataElement
            ), if (eventValue.feedbackHelp != null) listOf(
                TreeNode.Leaf(FeedbackHelpItem(eventValue.feedbackHelp))
            ) else mutableListOf()
        )
    }

    private fun getEnrollmentEvents(compulsoryValues: Boolean?): List<Event> {
        val enrolmentEvents = teiDataRepository.getTEIEnrollmentEvents(
            null,
            false,
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            false,
            mutableListOf(),
            mutableListOf()
        ).blockingGet()

        return enrolmentEvents.map {
            val values = valuesRepository.getByEvent(it.event!!.uid(), compulsoryValues)

            Event(it.event.uid(), it.stage?.displayName()!!, values)
        }
    }
}

interface ValuesRepository {
    fun getByEvent(eventUid: String, compulsoryFilter: Boolean? = null): List<Value>
}