package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.core.types.Tree
import org.dhis2.core.types.expand
import org.dhis2.usescases.teiDashboard.dashboardsfragments.enrollment.EnrollmentInfo
import org.dhis2.usescases.teiDashboard.dashboardsfragments.enrollment.GetEnrollmentInfo
import org.dhis2.usescases.teiDashboard.dashboardsfragments.systemInfo.GetSystemInfo
import timber.log.Timber

sealed class FeedbackContentState {
    object Loading : FeedbackContentState()
    data class Loaded(
        val feedback: Tree.Root<*>,
        val onlyFailedFilter: Boolean,
        val validations: List<Validation>
    ) :
        FeedbackContentState()

    data class ValidationsWithError(val validations: List<Validation>) : FeedbackContentState()
    object NotFound : FeedbackContentState()
    object UnexpectedError : FeedbackContentState()
    data class SharingFeedback(
        val enrollmentInfo: EnrollmentInfo,
        val serverUrl: String
    ) :
        FeedbackContentState()
}

class FeedbackContentPresenter(
    private val getFeedback: GetFeedback,
    private val getSystemInfo: GetSystemInfo,
    private val getEnrollmentInfo: GetEnrollmentInfo
) :
    CoroutineScope by MainScope() {

    private var view: FeedbackContentView? = null
    private lateinit var enrollmentUid: String
    private lateinit var feedbackMode: FeedbackMode
    private var criticalFilter: Boolean? = null
    private var lastLoaded: FeedbackContentState.Loaded? = null

    fun attach(
        view: FeedbackContentView,
        enrollmentUid: String,
        feedbackMode: FeedbackMode,
        criticalFilter: Boolean?,
        onlyFailedFilter: Boolean
    ) {
        this.view = view
        this.enrollmentUid = enrollmentUid
        this.feedbackMode = feedbackMode
        this.criticalFilter = criticalFilter

        loadFeedback(onlyFailedFilter)
    }

    fun detach() {
        this.view = null
        cancel()
    }

    fun changeOnlyFailedFilter(value: Boolean) {
        loadFeedback(value)
    }

    fun shareFeedback(onlyFailedFilter: Boolean) = launch {
        val enrollmentInfo = getEnrollmentInfo(enrollmentUid)
        val systemInfo = getSystemInfo()
        val serverUrl = systemInfo.contextPath

        render(FeedbackContentState.SharingFeedback(enrollmentInfo, serverUrl))
    }

    fun expand(node: Tree<*>) {
        if (lastLoaded != null && node is Tree.Node) {
            lastLoaded =
                lastLoaded!!.copy(feedback = lastLoaded!!.feedback.expand(node))
            render(lastLoaded!!)
        }
    }

    private fun loadFeedback(onlyFailedFilter: Boolean) = launch {
        render(FeedbackContentState.Loading)

        val result = withContext(Dispatchers.IO) {
            getFeedback(feedbackMode, criticalFilter, onlyFailedFilter)
        }

        result.fold(
            { failure -> handleFailure(failure) },
            { feedbackResponse ->

                val finalFeedback = if (lastLoaded != null)
                    tryMaintainCurrentExpandedItems(feedbackResponse.data) else feedbackResponse.data

                lastLoaded = FeedbackContentState.Loaded(
                    finalFeedback,
                    onlyFailedFilter,
                    feedbackResponse.validations
                )
                render(lastLoaded!!)
            })
    }

    private fun tryMaintainCurrentExpandedItems(feedback: Tree.Root<*>): Tree.Root<*> {
        val flattedLastFeedback = flatTreeNodes(lastLoaded!!.feedback.children)
        val flattedFeedback = flatTreeNodes(feedback.children)

        var root: Tree.Root<*> = feedback

        flattedFeedback.forEach { current ->
            val last = flattedLastFeedback.firstOrNull { last ->
                last.content == current.content && last.content.code == current.content.code
            }

            if (last != null && last.expanded) {
                root = root.expand(current)
            }
        }

        return root
    }

    private fun flatTreeNodes(nodes: List<Tree<*>>): List<Tree.Node<FeedbackItem>> {
        var flattedNodes: MutableList<Tree<*>> = mutableListOf()

        for (node in nodes) {
            flattedNodes.add(node)

            if (node is Tree.Node && node.children.isNotEmpty()) {
                flattedNodes.addAll(flatTreeNodes(node.children))
            }
        }

        return flattedNodes.filterIsInstance<Tree.Node<FeedbackItem>>()
    }

    private fun handleFailure(failure: FeedbackFailure) {
        when (failure) {
            is FeedbackFailure.NotFound -> render(FeedbackContentState.NotFound)
            is FeedbackFailure.UnexpectedError -> {
                render(FeedbackContentState.UnexpectedError)
                Timber.d(failure.error)
            }
            is FeedbackFailure.ValidationsWithError -> {
                render(FeedbackContentState.ValidationsWithError(failure.validations))
            }
        }
    }

    private fun render(state: FeedbackContentState) {
        view?.render(state)
    }

    interface FeedbackContentView {
        fun render(state: FeedbackContentState)
    }
}