package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.core.functional.Either
import tellh.com.recyclertreeview_lib.LayoutItemType
import tellh.com.recyclertreeview_lib.TreeNode

sealed class FeedbackContentState {
    object Loading : FeedbackContentState()
    data class Loaded(val feedback: List<TreeNode<*>>) : FeedbackContentState()

    //object NotFound : FeedbackContentState()
    object UnexpectedError : FeedbackContentState()
}

class FeedbackContentPresenter() :
    CoroutineScope by MainScope() {

    private var view: FeedbackContentView? = null

    fun attach(view: FeedbackContentView, uid: String) {
        this.view = view

        loadFeedback(uid);
    }

    fun detach() {
        this.view = null
        cancel()
    }

    private fun loadFeedback(uid: String) = launch() {
        render(FeedbackContentState.Loading)

        val result = withContext(Dispatchers.IO) { getFeedback() }

        result.fold(
            { failure -> handleFailure() },
            { feedback ->
                render(
                    FeedbackContentState.Loaded(
                        feedback
                    )
                )
            })
    }

    private fun getFeedback(): Either<Exception, List<TreeNode<*>>> {
        val feedbackHelpText = "**Quickly plagiarize inexpensive** portals after [bleeding-edge synergy](http://eyeseetea.com). Professionally leverage existing plug-and-play services for cross-media scenarios. Completely customize seamless content through transparent methodologies. Collaboratively reconceptualize vertical processes rather than competitive expertise. Monotonectally reinvent accurate relationships after out-of-the-box data.\nAppropriately pontificate go forward applications via cost effective testing procedures. Appropriately scale installed base best practices whereas distributed imperatives. Synergistically revolutionize adaptive e-markets vis-a-vis cost."

        val nodes: MutableList<TreeNode<*>> = ArrayList()
        val children = listOf(
            TreeNode(FeedbackItem("Completeness. Quickly plagiarize inexpensive portals af<ter bleeding-edge synergy.", FeedbackItemValue("Partty","#FFC700"))).addChild(TreeNode(FeedbackHelpItem(feedbackHelpText))),
            TreeNode(FeedbackItem("Timeliness", FeedbackItemValue("100%","#0CE922"))).addChild(TreeNode(FeedbackHelpItem(feedbackHelpText))),
            TreeNode(FeedbackItem("Integrity", FeedbackItemValue("No Indication","#0CE922"))).addChild(TreeNode(FeedbackHelpItem(feedbackHelpText))),
            TreeNode(FeedbackItem("Confidentiality", FeedbackItemValue("Partty","#FFC700"))).addChild(TreeNode(FeedbackHelpItem(feedbackHelpText))),
            TreeNode(FeedbackItem("Precision", FeedbackItemValue("No - no at all","#BA4E4E"))).addChild(TreeNode(FeedbackHelpItem(feedbackHelpText))),
            TreeNode(FeedbackItem("Accuracy", FeedbackItemValue("67 %","#BA4E4E"))).addChild(TreeNode(FeedbackHelpItem(feedbackHelpText)))
        )

        val node1 = TreeNode(FeedbackItem("ART New"))
        children.forEach {
            node1.addChild(it)
        }
        nodes.add(node1)

        val node2 = TreeNode(FeedbackItem("HTC"))
        children.forEach {
            node2.addChild(it)
        }
        nodes.add(node2)

        val node3 = TreeNode(FeedbackItem("HTC Count"))
        children.forEach {
            node3.addChild(it)
        }
        nodes.add(node3)

        val node4 = TreeNode(FeedbackItem("HIVST Stock Count"))
        children.forEach {
            node4.addChild(it)
        }
        nodes.add(node4)

        return Either.Right(nodes)
    }

    private fun handleFailure() {
        render(FeedbackContentState.UnexpectedError)
/*        when (failure) {
            is FeedbackProgramFailure.ConfigurationError -> {
                render(
                    FeedbackState.ConfigurationError(failure.programUid)
                )
            }
            is FeedbackProgramFailure.UnexpectedError -> {
                render(FeedbackState.UnexpectedError)
            }
        }*/
    }

    private fun render(state: FeedbackContentState) {
        view?.render(state);
    }

    interface FeedbackContentView {
        fun render(state: FeedbackContentState)
    }
}