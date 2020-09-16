package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import com.airbnb.lottie.animation.content.Content
import org.dhis2.core.functional.Either
import org.dhis2.core.types.TreeNode
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository
import timber.log.Timber
import java.lang.IllegalStateException

sealed class FeedbackFailure {
    object NotFound : FeedbackFailure()
    data class UnexpectedError(val error: Exception) : FeedbackFailure()
}

sealed class FeedbackMode {
    object ByEvent : FeedbackMode()
    object ByTechnicalArea : FeedbackMode()
}

class GetFeedback(
    private val teiDataRepository: TeiDataRepository,
    private val valuesRepository: ValuesRepository
) {
    operator fun invoke(
        feedbackMode: FeedbackMode,
        criticalFilter: Boolean? = null,
        onlyFailedFilter: Boolean = false
    ): Either<FeedbackFailure, List<TreeNode.Node<FeedbackItem>>> {
        return try {

            val events = getEnrollmentEvents(criticalFilter)

            if (events.isEmpty()) {
                Either.Left(FeedbackFailure.NotFound)
            } else {
                Either.Right(createFeedback(feedbackMode, events, onlyFailedFilter))
            }
        } catch (e: Exception) {
            Either.Left(FeedbackFailure.UnexpectedError(e))
        }
    }

    private fun createFeedback(
        feedbackMode: FeedbackMode,
        teiEvents: List<Event>,
        onlyFailedFilter: Boolean
    ): List<TreeNode.Node<FeedbackItem>> {
        return when (feedbackMode) {
            is FeedbackMode.ByEvent -> createFeedbackByEvent(teiEvents, onlyFailedFilter)
            is FeedbackMode.ByTechnicalArea -> createFeedbackByTechnicalArea(
                teiEvents, onlyFailedFilter
            )
        }
    }

    private fun createFeedbackByEvent(
        teiEvents: List<Event>,
        onlyFailed: Boolean
    ): List<TreeNode.Node<FeedbackItem>> {
        val feedbackByEvent = teiEvents.map { event ->
            val children = mapToTreeNodes(event.values)
/*
                .filter { node ->
                    val value = event.values.first { it.dataElement == node.content.code }

                    !onlyFailed || (onlyFailed && !value.success)
                }
*/

            TreeNode.Node(FeedbackItem(event.name, null, event.uid), children)
        }//.filter { it.children.isNotEmpty() }

        val filteredFeedbackByEvent =
            if (onlyFailed) filterOnlyFailed(feedbackByEvent) else feedbackByEvent

        return (filteredFeedbackByEvent as List<TreeNode.Node<FeedbackItem>>)
            .filter {
                it.children.any { child -> child.content is FeedbackItem }
            }
    }

    private fun createFeedbackByTechnicalArea(
        teiEvents: List<Event>,
        onlyFailed: Boolean
    ): List<TreeNode.Node<FeedbackItem>> {

        val distinctValues = teiEvents.flatMap { it.values }.distinctBy { it.dataElement }
        val treeNodes = mapToTreeNodes(distinctValues, false)

        addEventsToLastNodes(treeNodes, teiEvents, onlyFailed)

        return treeNodes.filter {
            it.children.any { child -> child.content is FeedbackItem }
        }
    }

    private fun filterOnlyFailed(
        nodes: List<TreeNode<*>>
    ): List<TreeNode<*>> {
        val newNodes = mutableListOf<TreeNode<*>>()

        val predicate = { node: TreeNode<*> ->
            (node.content is FeedbackItem && node.content.value != null &&
                !node.content.value.success) ||
                node.content is FeedbackHelpItem ||
                node is TreeNode.Node && anyChildrenIsFailed(node)
        }

        for (node in nodes) {
            if (predicate(node)) {
                when (node) {
                    is TreeNode.Node ->
                        newNodes.add(TreeNode.Node(node.content, filterOnlyFailed(node.children)))
                    is TreeNode.Leaf -> newNodes.add(TreeNode.Leaf(node.content))
                }
            }
        }

        return newNodes
    }

    private fun anyChildrenIsFailed(node: TreeNode<*>): Boolean {
        return node is TreeNode.Node && node.children.any { child ->
            (child.content is FeedbackItem && child.content.value != null &&
                !child.content.value.success) ||
                (child is TreeNode.Node && anyChildrenIsFailed(child))

        }
    }

    private fun mapToTreeNodes(
        values: List<Value>,
        withValue: Boolean = true
    ): List<TreeNode.Node<FeedbackItem>> {
        val treeNodes = mutableListOf<TreeNode.Node<FeedbackItem>>()

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

    private fun addEventsToLastNodes(
        treeNodes: List<TreeNode<FeedbackItem>>,
        events: List<Event>,
        onlyFailed: Boolean
    ) {
        treeNodes.forEach { treeNode ->
            if (treeNode is TreeNode.Node &&
                (treeNode.children.isEmpty() ||
                    (treeNode.children.size == 1 && treeNode.children[0] is TreeNode.Leaf))
            ) {
                events.filter { event ->
                    val eventValue =
                        event.values.firstOrNull { it.dataElement == treeNode.content.code }

                    eventValue != null && (!onlyFailed || onlyFailed && !eventValue.success)

                }.map { event ->
                    val eventValue = event.values.first { it.dataElement == treeNode.content.code }

                    FeedbackItem(
                        event.name,
                        FeedbackItemValue(
                            eventValue.value,
                            eventValue.colorByLegend,
                            eventValue.success
                        ),
                        event.uid
                    )
                }.forEach {
                    treeNode.addChild(TreeNode.Leaf(it))
                }
            } else if (treeNode is TreeNode.Node) {
                addEventsToLastNodes(
                    treeNode.children as List<TreeNode<FeedbackItem>>,
                    events,
                    onlyFailed
                )
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
                    eventValue.colorByLegend,
                    eventValue.success
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