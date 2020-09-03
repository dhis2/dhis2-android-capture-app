package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import org.dhis2.core.functional.Either
import org.dhis2.core.types.TreeNode
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel

sealed class FeedbackFailure {
    object NotFound : FeedbackFailure()
    object UnexpectedError : FeedbackFailure()
}

class GetFeedback(
    private val teiDataRepository: TeiDataRepository,
    private val valuesRepository: ValuesRepository
) {
    operator fun invoke(): Either<FeedbackFailure, List<TreeNode<FeedbackItem>>> {
        //return getFakeFeedback();

        return try {
            val events = getEnrollmentEvents()

            if (events.isEmpty()) {
                Either.Left(FeedbackFailure.NotFound)
            } else {
                Either.Right(createFeedbackByIndicator(events))
            }
        } catch (e: Exception) {
            Either.Left(FeedbackFailure.UnexpectedError)
        }
    }

    private fun createFeedbackByIndicator(teiEvents: List<EventViewModel>): List<TreeNode<FeedbackItem>> {
        return teiEvents.map { event->
            val eventUid = event.event?.uid()!!
            val values = valuesRepository.getByEvent(eventUid)

            val children = values.map { eventValue ->
                TreeNode.Branch(
                    FeedbackItem(
                        eventValue.name,
                        FeedbackItemValue(eventValue.value, eventValue.colorByLegend )
                    ),
                    if (eventValue.feedbackHelp != null) listOf(
                        TreeNode.Leaf(
                            FeedbackHelpItem(eventValue.feedbackHelp)
                        )
                    ) else mutableListOf()
                )
            }

            TreeNode.Branch(FeedbackItem(event.stage?.displayName()!!),children)
        }
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

    private fun getFakeFeedback(): Either<FeedbackFailure, List<TreeNode<FeedbackItem>>> {
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
}

interface ValuesRepository {
    fun getByEvent(eventUid: String): List<Value>
}