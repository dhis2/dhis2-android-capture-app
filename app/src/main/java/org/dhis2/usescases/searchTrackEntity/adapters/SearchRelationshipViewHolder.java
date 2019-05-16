package org.dhis2.usescases.searchTrackEntity.adapters;

import org.dhis2.databinding.ItemSearchRelationshipTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by frodriguez on 13/5/2019.
 */

public class SearchRelationshipViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchRelationshipTrackedEntityBinding binding;

    SearchRelationshipViewHolder(ItemSearchRelationshipTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(SearchTEContractsModule.Presenter presenter, SearchTeiModel teiModel) {
        binding.setPresenter(presenter);

        setTEIData(teiModel.getAttributeValueModels());
        binding.executePendingBindings();
        itemView.setOnClickListener(view -> presenter.addRelationship(teiModel.getTei().uid(), teiModel.isOnline()));
    }

    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }


}
