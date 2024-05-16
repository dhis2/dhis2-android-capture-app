package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import org.dhis2.commons.data.EventViewModel
import org.dhis2.core.functional.Either
import org.dhis2.core.types.Tree
import org.dhis2.core.types.filter
import org.dhis2.core.types.root
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository

sealed class FeedbackFailure {
    object NotFound : FeedbackFailure()
    data class ValidationsWithError(val validations: List<Validation>) : FeedbackFailure()
    data class UnexpectedError(val error: Exception) : FeedbackFailure()
}

sealed class FeedbackMode {
    object ByEvent : FeedbackMode()
    object ByTechnicalArea : FeedbackMode()
}

class GetFeedback(
    private val teiDataRepository: TeiDataRepository,
    private val dataElementRepository: DataElementRepository,
    private val valuesRepository: ValuesRepository
) {
    operator fun invoke(
        feedbackMode: FeedbackMode,
        criticalFilter: Boolean? = null,
        onlyFailedFilter: Boolean = false
    ): Either<FeedbackFailure, ResponseWithValidations<Tree.Root<*>>> {
        return try {
            val validations = validateFeedbackOrders()

            if (validations.any { it is Validation.DataElementError }) {
                Either.Left(FeedbackFailure.ValidationsWithError(validations))
            } else {
                val events = getEvents()
                if (events.isEmpty()) {
                    Either.Left(FeedbackFailure.NotFound)
                } else {

                    val feedback =
                        createFeedback(feedbackMode, events, criticalFilter, onlyFailedFilter)


                    Either.Right(ResponseWithValidations(feedback, validations))
                }
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
    ): Tree.Root<*> {
        val feedback =
            when (feedbackMode) {
                is FeedbackMode.ByEvent -> createFeedbackByEvent(teiEvents)
                is FeedbackMode.ByTechnicalArea -> createFeedbackByTechnicalArea(teiEvents)
            }

        val filteredFeedback =
            filterFeedback(criticalFilter, onlyFailedFilter, feedback)

        return filteredFeedback.copy(
            children = (filteredFeedback.children as List<Tree.Node<FeedbackItem>>)
                .filter {
                    it.children.any { child -> child.content is FeedbackItem }
                })
    }

    private fun createFeedbackByEvent(
        teiEvents: List<Event>
    ): Tree.Root<*> {
        return root(null, teiEvents.map { event ->
            val children = mapValuesToTreeNodes(event.values)

            Tree.Node(FeedbackItem(event.name, null, event.uid), 1, children)
        })
    }

    private fun createFeedbackByTechnicalArea(
        teiEvents: List<Event>
    ): Tree.Root<*> {

        val level0DistinctValues = teiEvents
            .flatMap { it.values }
            .distinctBy { it.dataElement }
            .filter { it.feedbackOrder.level == 0 }
            .sortedBy { it.feedbackOrder }

        return root(null, level0DistinctValues.map {
            val eventsChildren =
                createEventsChildrenByDataElement(it.dataElement, teiEvents)

            val finalChildren = if (it.feedbackHelp != null)
                listOf(Tree.Leaf(FeedbackHelpItem(it.feedbackHelp))) + eventsChildren else eventsChildren

            Tree.Node(FeedbackItem(it.name, null, it.dataElement), 1, finalChildren)
        })
    }

    private fun filterFeedback(
        criticalFilter: Boolean?,
        onlyFailed: Boolean,
        root: Tree.Root<*>
    ): Tree.Root<*> {
        val onlyFailedPredicate = { node: Tree<*> ->
            ((node.content is FeedbackItem && node.content.value != null &&
                !node.content.value.success && onlyFailed) || !onlyFailed)
        }

        val criticalPredicate = { node: Tree<*> ->
            (node is Tree.Node && node.content is FeedbackItem && node.content.value != null &&
                node.level != 2 && node.content.value.critical == criticalFilter)
                || criticalFilter == null
        }

        val predicate = { node: Tree<*> ->
            (onlyFailedPredicate(node) && criticalPredicate(node)) ||
                node.content is FeedbackHelpItem ||
                node is Tree.Node && anyChildrenIsFailed(criticalFilter, onlyFailed, node)
        }

        return root.filter { predicate(it) }
    }

    private fun anyChildrenIsFailed(
        criticalFilter: Boolean?,
        onlyFailed: Boolean,
        node: Tree<*>
    ): Boolean {
        return node is Tree.Node && node.children.any { child ->
            (((child.content is FeedbackItem && child.content.value != null &&
                !child.content.value.success && onlyFailed) || !onlyFailed) &&
                ((child.content is FeedbackItem && child.content.value != null &&
                    child.content.value.critical == criticalFilter) || criticalFilter == null)) ||
                (child is Tree.Node && anyChildrenIsFailed(criticalFilter, onlyFailed, child))

        }
    }

    private fun mapValuesToTreeNodes(
        values: List<Value>,
        topLevel: Int = 0
    ): List<Tree.Node<FeedbackItem>> {
        val treeNodesByParent = HashMap<String?, MutableList<Tree.Node<FeedbackItem>>>()

        values
            .sortedWith(compareByDescending<Value> { it.feedbackOrder.level }.thenBy { it.feedbackOrder })
            .forEach { eventValue ->
                val children =
                    treeNodesByParent[eventValue.feedbackOrder.value] ?: listOf<Tree<*>>()

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
        : List<Tree.Node<FeedbackItem>> {
        return eventsToLeaf.filter { event ->
            event.values.any { it.dataElement == dataElement }
        }.map { event ->
            val eventValueLevel0 = event.values.first { it.dataElement == dataElement }

            val descendantLevel0Values = event.values.filter { value ->
                value.feedbackOrder.isDescendantOf(eventValueLevel0.feedbackOrder)
            }

            val children = mapValuesToTreeNodes(descendantLevel0Values, 1)

            Tree.Node(
                FeedbackItem(
                    event.name,
                    FeedbackItemValue(
                        eventValueLevel0.value,
                        eventValueLevel0.colorByLegend,
                        eventValueLevel0.success,
                        eventValueLevel0.critical,
                        eventValueLevel0.isNumeric
                    ),
                    event.uid
                ), eventValueLevel0.feedbackOrder.level + 2, children
            )
        }
    }

    private fun mapValueToTreeNode(
        eventValue: Value,
        withValue: Boolean,
        children: List<Tree<*>>
    ): Tree.Node<FeedbackItem> {

        val finalChildren = if (eventValue.feedbackHelp != null)
            listOf(Tree.Leaf(FeedbackHelpItem(eventValue.feedbackHelp))) + children else children

        return Tree.Node(
            FeedbackItem(
                eventValue.name,
                if (withValue) FeedbackItemValue(
                    eventValue.value,
                    eventValue.colorByLegend,
                    eventValue.success,
                    eventValue.critical,
                    eventValue.isNumeric
                ) else null,
                eventValue.dataElement
            ), eventValue.feedbackOrder.level + 2, finalChildren
        )
    }

    private fun getEvents(): List<Event> {
        val enrolmentEvents = getEnrollmentEvents()

        return enrolmentEvents.map {
            val values = valuesRepository.getByEvent(it.event!!.uid())

            Event(it.event!!.uid(), it.stage?.displayName()!!, it.stage!!.uid(), values)
        }
    }

    private fun validateFeedbackOrders(): List<Validation> {
        val validations = mutableListOf<Validation>()
        val events = getEnrollmentEvents()
        val programStages = events.map { it.stage!!.uid() }.distinct()

        programStages.forEach { programStage ->
            val dataElementsResponse =
                dataElementRepository.getWithFeedbackOrderByProgramStage(programStage)

            dataElementsResponse.fold(
                { validationErrors -> validations.addAll(validationErrors) },
                { dataElements ->
                    val feedbackOrderWarnings = dataElements.map { dataElement ->
                        dataElement.feedbackOrder?.warnings?.map {
                            Validation.DataElementWarning(dataElement.uid, it)
                        } ?: listOf()
                    }.flatten()

                    validations.addAll(feedbackOrderWarnings)

                    val deWithDuplicates = getDEWithDuplicateFeedbackOrder(dataElements)

                    if (deWithDuplicates.isNotEmpty()) {
                        validations.add(
                            Validation.DataElementWarning(
                                deWithDuplicates,
                                FeedbackOrderDuplicate
                            )
                        )
                    }

                    if (existsFeedbackOrderGaps(dataElements)) {
                        validations.add(
                            Validation.ProgramStageWarning(
                                programStage,
                                FeedbackOrderGap
                            )
                        )
                    }
                }
            )

        }

        return validations
    }

    private fun existsFeedbackOrderGaps(dataElements: List<DataElement>): Boolean {
        val feedbackOrders = dataElements.sortedBy { it.feedbackOrder }.map { it.feedbackOrder }

        val feedbackOrderByParent = HashMap<String?, MutableList<FeedbackOrder>>()

        feedbackOrders.forEach { order ->
            val parent = order!!.parent

            val children = feedbackOrderByParent[parent] ?: mutableListOf()

            children.add(order)

            feedbackOrderByParent[parent] = children
        }

        return feedbackOrderByParent.map { entry ->
            entry.value.mapIndexed { index, order ->
                if (index == 0) {
                    false
                } else {
                    order.lastNumber - entry.value[index - 1].lastNumber > 1
                }
            }.any { it }
        }.any { it }
    }

    private fun getDEWithDuplicateFeedbackOrder(dataElements: List<DataElement>): String {
        val feedbackOrderDuplicates =
            dataElements.groupingBy { it.feedbackOrder }.eachCount()
                .filter { it.value > 1 }.keys

        val dataElementsWithDuplicates =
            dataElements.filter { feedbackOrderDuplicates.contains(it.feedbackOrder) }

        return if (dataElementsWithDuplicates.isNotEmpty()) {
            dataElementsWithDuplicates.joinToString(",") { it.uid }
        } else {
            ""
        }
    }

    private fun getEnrollmentEvents(): List<EventViewModel> {
        return teiDataRepository.getTEIEnrollmentEvents(
            null,
            groupedByStage = false,
            replaceProgramStageName = true,
        ).blockingGet()
    }
}

interface ValuesRepository {
    fun getByEvent(eventUid: String): List<Value>
}

interface DataElementRepository {
    fun getWithFeedbackOrderByProgramStage(eventUid: String): Either<List<Validation>, List<DataElement>>
}