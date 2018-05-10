package com.dhis2.usescases.searchTrackEntity.adapters;

import android.support.v7.widget.RecyclerView;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemSearchTrackedEntityOnlineBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchTEViewHolderOnline extends RecyclerView.ViewHolder {

    private ItemSearchTrackedEntityOnlineBinding binding;


    public SearchTEViewHolderOnline(ItemSearchTrackedEntityOnlineBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }


    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityInstanceModel trackedEntityInstanceModel, MetadataRepository metadataRepository, List<String> teiAttributes) {
        binding.setPresenter(presenter);
        binding.setAdapterPosition(getAdapterPosition());
        binding.setTei(trackedEntityInstanceModel);

        String attr1 = null;
        String attr2 = null;

        if (teiAttributes.size() > 0)
            attr1 = teiAttributes.get(0).isEmpty() ? "UNKNOWN" : teiAttributes.get(0);
        if (teiAttributes.size() > 1)
            attr2 = teiAttributes.get(1).isEmpty() ? "UNKNOWN" : teiAttributes.get(1);

        binding.entityAttribute1.setText(attr1);
        binding.entityAttribute2.setText(attr2);

    }


}
