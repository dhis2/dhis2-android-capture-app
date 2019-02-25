package org.dhis2.usescases.searchTrackEntity.adapters;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.databinding.ItemSearchRelationshipTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * QUADRAM. Created by frodriguez on 11/7/2017.
 */

@SuppressWarnings({"squid:S1172", "squid:S1068", "squid:CommentedOutCodeLine"})
public class SearchRelationshipViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchRelationshipTrackedEntityBinding binding;

    SearchRelationshipViewHolder(ItemSearchRelationshipTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(SearchTEContractsModule.SearchTEPresenter presenter, SearchTeiModel teiModel, MetadataRepository metadataRepository) {
        binding.setPresenter(presenter);

        //--------------------------
        //region ATTRI
        setTEIData(teiModel.getAttributeValues());
        //endregion

       /* if (presenter.getProgramModel() != null)
            compositeDisposable.add(
                    metadataRepository.getRelationshipTypeList()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setRelationshipTypeList, Timber::d)
            );*/

        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.addRelationship(teiModel.getTei().uid(), teiModel.isOnline()));
    }

   /* private void setRelationshipTypeList(List<RelationshipTypeModel> relationshipTypesModels) {

        this.relationshipTypes.clear();
        this.relationshipTypes.addAll(relationshipTypesModels);
        RelationshipSpinnerAdapter adapter = new RelationshipSpinnerAdapter(binding.getRoot().getContext(), this.relationshipTypes);
        binding.relationshipSpinner.setAdapter(adapter);
        binding.relationshipSpinner.setSelection(0);
        binding.relationshipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, DataSetTableView view, int position, long id) {
                if (position > 0) {
                    Pair<RelationshipTypeModel, String> selectedRelationShip = (Pair<RelationshipTypeModel, String>) parent.getItemAtPosition(position);
                    presenter.addRelationship(trackedEntityInstanceModel.getTei().uid(), selectedRelationShip.val0().uid(),trackedEntityInstanceModel.isOnline());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }*/

    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }
}
