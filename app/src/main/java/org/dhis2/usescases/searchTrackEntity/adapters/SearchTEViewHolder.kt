package org.dhis2.usescases.searchTrackEntity.adapters

import android.widget.Toast
import org.dhis2.R
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.ItemSearchTrackedEntityBinding

class SearchTEViewHolder(
    private val binding: ItemSearchTrackedEntityBinding,
    private val onSyncIconClick: (enrollmentUid: String) -> Unit,
    private val onDownloadTei: (teiUid: String, enrollmentUid: String?) -> Unit,
    private val colorUtils: ColorUtils,
    private val onTeiClick: (teiUid: String, enrollmentUid: String?, isOnline: Boolean) -> Unit,
) : BaseTeiViewHolder(binding, colorUtils) {

    override fun itemConfiguration() {
        binding.sortingFieldName.text = teiModel.sortingKey
        binding.sortingFieldValue.text = teiModel.sortingValue
    }

    override fun itemViewClick() {
        binding.syncState.setOnClickListener {
            if (teiModel.tei.deleted()!! ||
                teiModel.selectedEnrollment != null &&
                teiModel.selectedEnrollment.deleted()!!
            ) {
                Toast.makeText(
                    itemView.context,
                    itemView.context.getString(R.string.record_marked_for_deletion),
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                onSyncIconClick(teiModel.selectedEnrollment.uid())
            }
        }

        binding.download.setOnClickListener {
            onDownloadTei(
                teiModel.tei.uid(),
                teiModel.selectedEnrollment?.uid(),
            )
        }

        binding.cardView.setOnClickListener {
            if (teiModel.isOnline) {
                onDownloadTei(
                    teiModel.tei.uid(),
                    teiModel.selectedEnrollment?.uid(),
                )
            } else {
                onTeiClick(
                    teiModel.tei.uid(),
                    teiModel.selectedEnrollment?.uid(),
                    teiModel.isOnline,
                )
            }
        }
    }
}
