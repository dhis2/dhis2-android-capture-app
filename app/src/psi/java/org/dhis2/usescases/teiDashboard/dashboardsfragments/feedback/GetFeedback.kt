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
        val feedbackByTechnicalArea = mapToTreeNodes(distinctValues, teiEvents)

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
        eventsToLeaf: List<Event> = listOf()
    ): List<TreeNode.Node<FeedbackItem>> {
        val treeNodesByParent = HashMap<String, MutableList<TreeNode.Node<FeedbackItem>>>()
        val topTreeNodes = mutableListOf<TreeNode.Node<FeedbackItem>>()

        values.sortedWith(compareByDescending<Value> { it.feedbackOrder.level }.thenBy { it.feedbackOrder })
            .forEach { eventValue ->
                val eventsLeafByValue = createEventsLeafByValue(eventValue, eventsToLeaf)

                val children =
                    treeNodesByParent[eventValue.feedbackOrder.value] ?: listOf<TreeNode<*>>()

                val currentNode =
                    mapValueToTreeNode(
                        eventValue,
                        eventsToLeaf.isEmpty(),
                        eventsLeafByValue + children
                    )

                if (eventValue.feedbackOrder.parent == null) {
                    topTreeNodes.add(currentNode)
                } else {

                    if (treeNodesByParent.containsKey(eventValue.feedbackOrder.parent)) {
                        treeNodesByParent[eventValue.feedbackOrder.parent]?.add(currentNode)
                    } else {
                        treeNodesByParent[eventValue.feedbackOrder.parent] =
                            mutableListOf(currentNode)
                    }
                }
            }

        return topTreeNodes
    }

    private fun createEventsLeafByValue(eventValue: Value, eventsToLeaf: List<Event>)
        : List<TreeNode.Leaf<FeedbackItem>> {
        return eventsToLeaf.filter { event ->
            event.values.any { it.dataElement == eventValue.dataElement }
        }.map { event ->
            val eventValue = event.values.first { it.dataElement == eventValue.dataElement }

            TreeNode.Leaf(
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
            )
        }
    }

    private fun mapValueToTreeNode(
        eventValue: Value,
        withValue: Boolean,
        children: List<TreeNode<*>>
    ): TreeNode.Node<FeedbackItem> {

        val finalChildren = if (eventValue.feedbackHelp != null)
            listOf(TreeNode.Leaf(FeedbackHelpItem(eventValue.feedbackHelp))) + children else children

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
            ), finalChildren
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