package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import org.dhis2.core.functional.Either
import org.dhis2.core.types.TreeNode
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository

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

            val events = getEnrollmentEvents()

            if (events.isEmpty()) {
                Either.Left(FeedbackFailure.NotFound)
            } else {
                Either.Right(createFeedback(feedbackMode, events, criticalFilter, onlyFailedFilter))
            }
        } catch (e: Exception) {
            Either.Left(FeedbackFailure.UnexpectedError(e))
        }
    }

    private fun createFeedback(
        feedbackMode: FeedbackMode,
        teiEvents: List<Event>,
        criticalFilter: Boolean?,
        onlyFailedFilter: Boolean
    ): List<TreeNode.Node<FeedbackItem>> {
        return when (feedbackMode) {
            is FeedbackMode.ByEvent -> createFeedbackByEvent(
                teiEvents,
                criticalFilter,
                onlyFailedFilter
            )
            is FeedbackMode.ByTechnicalArea -> createFeedbackByTechnicalArea(
                teiEvents, onlyFailedFilter
            )
        }
    }

    private fun createFeedbackByEvent(
        teiEvents: List<Event>,
        criticalFilter: Boolean?,
        onlyFailed: Boolean
    ): List<TreeNode.Node<FeedbackItem>> {
        val feedbackByEvent = teiEvents.map { event ->
            val children = mapToTreeNodes(event.values)

            TreeNode.Node(FeedbackItem(event.name, null, event.uid), children)
        }

        val filteredFeedbackByEvent =
            filterFeedback(criticalFilter, onlyFailed, feedbackByEvent)

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
        val feedbackByTechnicalArea = mapToTreeNodes(distinctValues, false)

        addEventsToDE(feedbackByTechnicalArea, teiEvents)

        val filteredFeedbackByTechnicalArea =
            filterFeedback(null, onlyFailed, feedbackByTechnicalArea)

        return (filteredFeedbackByTechnicalArea as List<TreeNode.Node<FeedbackItem>>)
            .filter {
                it.children.any { child -> child.content is FeedbackItem }
            }
    }

    private fun filterFeedback(
        criticalFilter: Boolean?,
        onlyFailed: Boolean,
        nodes: List<TreeNode<*>>
    ): List<TreeNode<*>> {
        val newNodes = mutableListOf<TreeNode<*>>()

        val predicate = { node: TreeNode<*> ->
            (((node.content is FeedbackItem && node.content.value != null &&
                !node.content.value.success && onlyFailed) || !onlyFailed) &&
                ((node.content is FeedbackItem && node.content.value != null &&
                    node.content.value.critical == criticalFilter) || criticalFilter == null)) ||
                node.content is FeedbackHelpItem ||
                node is TreeNode.Node && anyChildrenIsFailed(criticalFilter, onlyFailed, node)
        }

        for (node in nodes) {
            if (predicate(node)) {
                when (node) {
                    is TreeNode.Node ->
                        newNodes.add(
                            TreeNode.Node(
                                node.content,
                                filterFeedback(criticalFilter, onlyFailed, node.children)
                            )
                        )
                    is TreeNode.Leaf -> newNodes.add(TreeNode.Leaf(node.content))
                }
            }
        }

        return newNodes
    }

    private fun anyChildrenIsFailed(
        criticalFilter: Boolean?,
        onlyFailed: Boolean,
        node: TreeNode<*>
    ): Boolean {
        return node is TreeNode.Node && node.children.any { child ->
            (((child.content is FeedbackItem && child.content.value != null &&
                !child.content.value.success && onlyFailed) || !onlyFailed) &&
                ((child.content is FeedbackItem && child.content.value != null &&
                    child.content.value.critical == criticalFilter) || criticalFilter == null)) ||
                (child is TreeNode.Node && anyChildrenIsFailed(criticalFilter, onlyFailed, child))

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

    private fun addEventsToDE(
        treeNodes: List<TreeNode<FeedbackItem>>,
        events: List<Event>
    ) {
        treeNodes.forEach { treeNode ->
            if (treeNode is TreeNode.Node) {
                events.filter { event ->
                    event.values.any { it.dataElement == treeNode.content.code }
                }.map { event ->
                    val eventValue = event.values.first { it.dataElement == treeNode.content.code }

                    FeedbackItem(
                        event.name,
                        FeedbackItemValue(
                            eventValue.value,
                            eventValue.colorByLegend,
                            eventValue.success,
                            eventValue.critical
                        ),
                        event.uid
                    )
                }.forEach {
                    val index =
                        if (treeNode.children.isNotEmpty() && treeNode.children[0] is TreeNode.Leaf) 1
                        else 0

                    treeNode.addChild(TreeNode.Leaf(it), index)
                }

                addEventsToDE(
                    treeNode.children as List<TreeNode<FeedbackItem>>,
                    events
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
                    eventValue.success,
                    eventValue.critical
                ) else null,
                eventValue.dataElement
            ), if (eventValue.feedbackHelp != null) listOf(
                TreeNode.Leaf(FeedbackHelpItem(eventValue.feedbackHelp))
            ) else mutableListOf()
        )
    }

    private fun getEnrollmentEvents(): List<Event> {
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
            val values = valuesRepository.getByEvent(it.event!!.uid())

            Event(it.event.uid(), it.stage?.displayName()!!, values)
        }
    }
}

interface ValuesRepository {
    fun getByEvent(eventUid: String): List<Value>
}