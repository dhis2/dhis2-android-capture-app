package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.core.types.TreeNode
import timber.log.Timber

sealed class FeedbackContentState {
    object Loading : FeedbackContentState()
    data class Loaded(val feedback: List<TreeNode<*>>) : FeedbackContentState()
    object NotFound : FeedbackContentState()
    object UnexpectedError : FeedbackContentState()
}

class FeedbackContentPresenter(private val getFeedback: GetFeedback) :
    CoroutineScope by MainScope() {

    private lateinit var feedbackMode: FeedbackMode
    private var view: FeedbackContentView? = null
    private var lastFeedback: List<TreeNode<FeedbackItem>> = listOf()

    fun attach(view: FeedbackContentView, feedbackMode: FeedbackMode) {
        this.view = view
        this.feedbackMode = feedbackMode;

        loadFeedback()
    }

    fun detach() {
        this.view = null
        cancel()
    }

    private fun loadFeedback() = launch {
        render(FeedbackContentState.Loading)

        val result = withContext(Dispatchers.IO) { getFeedback(feedbackMode) }

        result.fold(
            { failure -> handleFailure(failure) },
            { feedback ->

                if (lastFeedback.isNotEmpty()){
                    tryMaintainCurrentExpandedItems(feedback)
                }

                lastFeedback = feedback
                render(
                    FeedbackContentState.Loaded(
                        feedback
                    )
                )
            })
    }

    private fun tryMaintainCurrentExpandedItems(feedback: List<TreeNode<FeedbackItem>>) {
        val flattedLastFeedback = flatTreeNodes(lastFeedback)
        val flattedFeedback =  flatTreeNodes(feedback)

        flattedFeedback.forEach{ current ->
            val last = flattedLastFeedback.firstOrNull{ last ->
                last.content == current.content && last.content.code == current.content.code
            }

            if (last != null){
                (current as TreeNode.Node).expanded = (last as TreeNode.Node).expanded
            }
        }
    }

    private fun flatTreeNodes(nodes: List<TreeNode<*>>):List<TreeNode<FeedbackItem>> {
        var flattedNodes: MutableList<TreeNode<*>> = mutableListOf()

        for (node in nodes) {
            flattedNodes.add(node)

            if (node is TreeNode.Node && node.children.isNotEmpty()) {
                flattedNodes.addAll(flatTreeNodes(node.children))
            }
        }

        return flattedNodes.filterIsInstance<TreeNode.Node<FeedbackItem>>()
    }

    private fun handleFailure(failure: FeedbackFailure) {
        when (failure) {
            is FeedbackFailure.NotFound -> render(FeedbackContentState.NotFound)
            is FeedbackFailure.UnexpectedError -> {
                render(FeedbackContentState.UnexpectedError)
                Timber.d(failure.error)
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