package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.core.types.TreeNode

sealed class FeedbackContentState {
    object Loading : FeedbackContentState()
    data class Loaded(val feedback: List<TreeNode<*>>) : FeedbackContentState()
    object NotFound : FeedbackContentState()
    object UnexpectedError : FeedbackContentState()
}

class FeedbackContentPresenter(private val getFeedback: GetFeedback) :
    CoroutineScope by MainScope() {

    private var view: FeedbackContentView? = null

    fun attach(view: FeedbackContentView) {
        this.view = view

        loadFeedback()
    }

    fun detach() {
        this.view = null
        cancel()
    }

    private fun loadFeedback() = launch {
        render(FeedbackContentState.Loading)

        val result = withContext(Dispatchers.IO) { getFeedback() }

        result.fold(
            { failure -> handleFailure(failure) },
            { feedback ->
                render(
                    FeedbackContentState.Loaded(
                        feedback
                    )
                )
            })
    }

    private fun handleFailure(failure: FeedbackFailure) {
        when (failure) {
            is FeedbackFailure.NotFound -> render(FeedbackContentState.NotFound)
            is FeedbackFailure.UnexpectedError -> render(FeedbackContentState.UnexpectedError)
        }
    }

    private fun render(state: FeedbackContentState) {
        view?.render(state)
    }

    interface FeedbackContentView {
        fun render(state: FeedbackContentState)
    }
}