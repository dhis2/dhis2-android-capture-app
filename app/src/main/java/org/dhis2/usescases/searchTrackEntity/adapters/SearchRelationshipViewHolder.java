package org.dhis2.usescases.searchTrackEntity.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ItemSearchRelationshipTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by frodriguez on 11/7/2017.
 */

public class SearchRelationshipViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchRelationshipTrackedEntityBinding binding;
    private CompositeDisposable compositeDisposable;
    private SearchTEContractsModule.Presenter presenter;
    private SearchTeiModel trackedEntityInstanceModel;

    private RelationshipTypeModel relationshipType;
    private List<RelationshipTypeModel> relationshipTypes = new ArrayList<>();

    SearchRelationshipViewHolder(ItemSearchRelationshipTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        compositeDisposable = new CompositeDisposable();
    }


    public void bind(SearchTEContractsModule.Presenter presenter, SearchTeiModel teiModel, MetadataRepository metadataRepository) {
        this.presenter = presenter;
        this.trackedEntityInstanceModel = teiModel;
        binding.setPresenter(presenter);

        //--------------------------
        //region ATTRI
        setTEIData(teiModel.getAttributeValues());
        //endregion

        if (presenter.getProgramModel() != null)
            compositeDisposable.add(
                    metadataRepository.getRelationshipTypeList()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setRelationshipTypeList, Timber::d)
            );

        binding.executePendingBindings();


    }

    private void setRelationshipTypeList(List<RelationshipTypeModel> relationshipTypesModels) {

        this.relationshipTypes.clear();
        this.relationshipTypes.addAll(relationshipTypesModels);
        RelationshipSpinnerAdapter adapter = new RelationshipSpinnerAdapter(binding.getRoot().getContext(), this.relationshipTypes);
        binding.relationshipSpinner.setAdapter(adapter);
        binding.relationshipSpinner.setSelection(0);
        binding.relationshipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Pair<RelationshipTypeModel, String> selectedRelationShip = (Pair<RelationshipTypeModel, String>) parent.getItemAtPosition(position);
                    presenter.addRelationship(trackedEntityInstanceModel.getTei().uid(), selectedRelationShip.val0().uid(),trackedEntityInstanceModel.isOnline());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }


}
