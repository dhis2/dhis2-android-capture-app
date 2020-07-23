package org.dhis2.usescases.searchTrackEntity.adapters

import org.dhis2.databinding.ItemSearchTrackedEntityBinding

class SearchRelationshipViewHolder(
    binding: ItemSearchTrackedEntityBinding
) : BaseViewHolder(binding) {

    override fun itemViewClick() {
        itemView.setOnClickListener {
            presenter.addRelationship(teiModel.tei.uid(), null, teiModel.isOnline)
        }
    }
}
