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
            val events = getEnrollmentEvents()

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
        teiEvents: List<EventViewModel>
    ): List<TreeNode<FeedbackItem>> {
        return when (feedbackMode) {
            is FeedbackMode.ByEvent -> createFeedbackByEvent(teiEvents, feedbackMode.criticalFilter)
            is FeedbackMode.ByTechnicalArea -> createFeedbackByEvent(
                teiEvents
            )
        }
    }

    private fun createFeedbackByEvent(
        teiEvents: List<EventViewModel>,
        criticalFilter: Boolean? = null
    ): List<TreeNode<FeedbackItem>> {
        return teiEvents.map { event ->
            val eventUid = event.event?.uid()!!
            val values = valuesRepository.getByEvent(eventUid, criticalFilter)

            val children = mapToTreeNodes(values)

            TreeNode.Node(FeedbackItem(event.stage?.displayName()!!, null, eventUid), children)
        }
    }

    private fun mapToTreeNodes(values: List<Value>): List<TreeNode<*>> {
        val treeNodes = mutableListOf<TreeNode<FeedbackItem>>()

        val nodesMap = values.associate { it.feedbackOrder.value to mapToTreeNode(it) }

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

        // Add help text to the last depth children
        nodesMap.forEach { entry ->
            val node = entry.value

            if (node.children.isEmpty()) {
                val eventValue = values.first { it.dataElement == node.content.code }

                if (eventValue.feedbackHelp != null) {
                    node.addChild(TreeNode.Leaf(FeedbackHelpItem(eventValue.feedbackHelp)))
                }
            }
        }

        return treeNodes
    }

    private fun mapToTreeNode(eventValue: Value): TreeNode.Node<FeedbackItem> {
        return TreeNode.Node(
            FeedbackItem(
                eventValue.name,
                FeedbackItemValue(eventValue.value, eventValue.colorByLegend),
                eventValue.dataElement
            )
        )
    }

    private fun getEnrollmentEvents(): List<EventViewModel> {
        return teiDataRepository.getTEIEnrollmentEvents(
            null,
            false,
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            false,
            mutableListOf(),
            mutableListOf()
        ).blockingGet()
    }
}

interface ValuesRepository {
    fun getByEvent(eventUid: String, onlyMandatory: Boolean?): List<Value>
}