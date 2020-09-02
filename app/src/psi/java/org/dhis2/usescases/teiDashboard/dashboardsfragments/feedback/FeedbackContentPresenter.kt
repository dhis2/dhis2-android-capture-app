package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.core.functional.Either
import org.dhis2.core.ui.tree.TreeNode
import timber.log.Timber

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

    private fun loadFeedback(uid: String) = launch {
        Timber.d("loadFeedback")
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
        val feedbackHelpText =
            "**Quickly plagiarize inexpensive** portals after [bleeding-edge synergy](http://eyeseetea.com). Professionally leverage existing plug-and-play services for cross-media scenarios. Completely customize seamless content through transparent methodologies. Collaboratively reconceptualize vertical processes rather than competitive expertise. Monotonectally reinvent accurate relationships after out-of-the-box data.\nAppropriately pontificate go forward applications via cost effective testing procedures. Appropriately scale installed base best practices whereas distributed imperatives. Synergistically revolutionize adaptive e-markets vis-a-vis cost."

        val children1 = listOf(
            TreeNode.Branch(
                FeedbackItem("Completeness", FeedbackItemValue("Partty", "#FFC700")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Timeliness", FeedbackItemValue("100%", "#0CE922")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem(
                    "Integrity",
                    FeedbackItemValue("No Indication", "#0CE922")
                ), listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Confidentiality", FeedbackItemValue("Partty", "#FFC700")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem(
                    "Precision",
                    FeedbackItemValue("No - no at all", "#BA4E4E")
                ), listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Accuracy", FeedbackItemValue("67 %", "#BA4E4E")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            )
        )

        val children2 = listOf(
            TreeNode.Branch(
                FeedbackItem("Completeness", FeedbackItemValue("Partty", "#FFC700")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Timeliness", FeedbackItemValue("100%", "#0CE922")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem(
                    "Integrity",
                    FeedbackItemValue("No Indication", "#0CE922")
                ), listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Confidentiality", FeedbackItemValue("Partty", "#FFC700")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem(
                    "Precision",
                    FeedbackItemValue("No - no at all", "#BA4E4E")
                ), listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Accuracy", FeedbackItemValue("67 %", "#BA4E4E")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            )
        )

        val children3 = listOf(
            TreeNode.Branch(
                FeedbackItem("Completeness", FeedbackItemValue("Partty", "#FFC700")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Timeliness", FeedbackItemValue("100%", "#0CE922")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem(
                    "Integrity",
                    FeedbackItemValue("No Indication", "#0CE922")
                ), listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Confidentiality", FeedbackItemValue("Partty", "#FFC700")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem(
                    "Precision",
                    FeedbackItemValue("No - no at all", "#BA4E4E")
                ), listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Accuracy", FeedbackItemValue("67 %", "#BA4E4E")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            )
        )

        val children4 = listOf(
            TreeNode.Branch(
                FeedbackItem("Completeness", FeedbackItemValue("Partty", "#FFC700")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Timeliness", FeedbackItemValue("100%", "#0CE922")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem(
                    "Integrity",
                    FeedbackItemValue("No Indication", "#0CE922")
                ), listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Confidentiality", FeedbackItemValue("Partty", "#FFC700")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem(
                    "Precision",
                    FeedbackItemValue("No - no at all", "#BA4E4E")
                ), listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            ),
            TreeNode.Branch(
                FeedbackItem("Accuracy", FeedbackItemValue("67 %", "#BA4E4E")),
                listOf(TreeNode.Leaf(FeedbackHelpItem(feedbackHelpText)))
            )
        )

        val node1 = TreeNode.Branch(FeedbackItem("ART New"), children1)
        val node2 = TreeNode.Branch(FeedbackItem("HTC"), children2)
        val node3 = TreeNode.Branch(FeedbackItem("HTC Count"), children3)
        val node4 = TreeNode.Branch(FeedbackItem("HIVST Stock Count"), children4)

        return Either.Right(listOf(node1, node2, node3, node4))
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