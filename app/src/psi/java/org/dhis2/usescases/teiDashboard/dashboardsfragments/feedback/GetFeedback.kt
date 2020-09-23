package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import org.dhis2.core.functional.Either
import org.dhis2.core.types.TreeNode
import org.dhis2.core.types.root
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
    ): Either<FeedbackFailure, TreeNode.Root<*>> {
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
    ): TreeNode.Root<*> {
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
    ): TreeNode.Root<*> {

        val feedbackByEvent = teiEvents.map { event ->
            val children = mapToTreeNodes(event.values)

            TreeNode.Node(FeedbackItem(event.name, null, event.uid), children)
        }

        val filteredFeedbackByEvent =
            filterFeedback(criticalFilter, onlyFailed, feedbackByEvent)

        val nonEmptyFeedback =
            (filteredFeedbackByEvent as List<TreeNode.Node<FeedbackItem>>)
                .filter {
                    it.children.any { child -> child.content is FeedbackItem }
                }

        return root(null, nonEmptyFeedback)
    }

    private fun createFeedbackByTechnicalArea(
        teiEvents: List<Event>,
        onlyFailed: Boolean
    ): TreeNode.Root<*> {

        val level0DistinctValues = teiEvents.flatMap {
            it.values
        }.distinctBy {
            it.dataElement
        }.filter { it.feedbackOrder.level == 0 }

        val feedbackByTechnicalArea = level0DistinctValues.map {
            val eventsChildren =
                createEventsChildrenByDataElement(it.dataElement, teiEvents)

            val finalChildren = if (it.feedbackHelp != null)
                listOf(TreeNode.Leaf(FeedbackHelpItem(it.feedbackHelp))) + eventsChildren else eventsChildren

            TreeNode.Node(FeedbackItem(it.name, null, it.dataElement), finalChildren)
        }

        val filteredFeedbackByTechnicalArea =
            filterFeedback(null, onlyFailed, feedbackByTechnicalArea)

        val nonEmptyFeedback =
            (filteredFeedbackByTechnicalArea as List<TreeNode.Node<FeedbackItem>>)
                .filter {
                    it.children.any { child -> child.content is FeedbackItem }
                }

        return root(null, nonEmptyFeedback)
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
        topLevel: Int = 0
    ): List<TreeNode.Node<FeedbackItem>> {
        val treeNodesByParent = HashMap<String?, MutableList<TreeNode.Node<FeedbackItem>>>()

        values
            .sortedWith(compareByDescending<Value> { it.feedbackOrder.level }.thenBy { it.feedbackOrder })
            .forEach { eventValue ->
                val children =
                    treeNodesByParent[eventValue.feedbackOrder.value] ?: listOf<TreeNode<*>>()

                val currentNode =
                    mapValueToTreeNode(eventValue, true, children)

                val treeNodesByParentList =
                    treeNodesByParent.getOrPut(eventValue.feedbackOrder.parent, { mutableListOf() })

                treeNodesByParentList.add(currentNode)
            }

        val topTreeEntries =
            treeNodesByParent.filter { entry ->
                val parentFeedbackOrder =
                    if (entry.key == null) null else FeedbackOrder(entry.key!!)

                (topLevel == 0 && parentFeedbackOrder == null) ||
                    (topLevel == parentFeedbackOrder!!.level + 1)
            }

        return topTreeEntries.flatMap { it.value.toList() }
    }

    private fun createEventsChildrenByDataElement(dataElement: String, eventsToLeaf: List<Event>)
        : List<TreeNode.Node<FeedbackItem>> {
        return eventsToLeaf.filter { event ->
            event.values.any { it.dataElement == dataElement }
        }.map { event ->
            val eventValueLevel0 = event.values.first { it.dataElement == dataElement }

            val descendantLevel0Values = event.values.filter { value ->
                value.feedbackOrder.isDescendantOf(eventValueLevel0.feedbackOrder)
            }

            val children = mapToTreeNodes(descendantLevel0Values, 1)

            TreeNode.Node(
                FeedbackItem(
                    event.name,
                    FeedbackItemValue(
                        eventValueLevel0.value,
                        eventValueLevel0.colorByLegend,
                        eventValueLevel0.success,
                        eventValueLevel0.critical
                    ),
                    event.uid
                ), children
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