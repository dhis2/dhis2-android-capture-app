package org.dhis2.usescases.searchTrackEntity.adapters

import android.widget.Toast
import org.dhis2.R
import org.dhis2.databinding.ItemSearchTrackedEntityBinding

class SearchTEViewHolder(
    private val binding: ItemSearchTrackedEntityBinding
) : BaseTeiViewHolder(binding) {

    override fun itemConfiguration() {
        binding.sortingFieldName.text = teiModel.sortingKey
        binding.sortingFieldValue.text = teiModel.sortingValue
    }

    override fun itemViewClick() {
        binding.syncState.setOnClickListener {
            if (teiModel.tei.deleted()!! ||
                teiModel.selectedEnrollment != null &&
                teiModel.selectedEnrollment.deleted()!!
            ) Toast.makeText(
                itemView.context,
                itemView.context.getString(R.string.record_marked_for_deletion),
                Toast.LENGTH_SHORT
            ).show() else presenter.onSyncIconClick(teiModel.tei.uid())
        }

        binding.download.setOnClickListener {
            presenter.downloadTei(
                teiModel.tei.uid(),
                teiModel.selectedEnrollment?.uid()
            )
        }

        binding.cardView.setOnClickListener {
            presenter.onTEIClick(
                teiModel.tei.uid(),
                teiModel.selectedEnrollment?.uid(),
                teiModel.isOnline
            )
        }
    }
}
