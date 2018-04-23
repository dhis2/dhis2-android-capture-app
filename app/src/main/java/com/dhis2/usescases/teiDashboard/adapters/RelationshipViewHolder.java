package com.dhis2.usescases.teiDashboard.adapters;

import android.support.v7.widget.RecyclerView;

import com.dhis2.databinding.ItemRelationshipBinding;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.utils.OnErrorHandler;

import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by ppajuelo on 05/12/2017.
 */

public class RelationshipViewHolder extends RecyclerView.ViewHolder {

    private final ItemRelationshipBinding binding;
    private CompositeDisposable compositeDisposable;

    public RelationshipViewHolder(ItemRelationshipBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.compositeDisposable = new CompositeDisposable();
    }

    public void bind(TeiDashboardContracts.Presenter presenter, RelationshipModel relationship) {

        compositeDisposable.add(
                presenter.getTEIMainAttributes(relationship.trackedEntityInstanceA())
                .subscribe(
                        this::setAttributes,
                        OnErrorHandler.create()
                )
        );

        binding.setPresenter(presenter);
        binding.setRelationship(relationship);
        binding.executePendingBindings();

        presenter.subscribeToMainAttr(relationship.trackedEntityInstanceA(), binding.relationShipAttr);
    }

    private void setAttributes(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }
}
