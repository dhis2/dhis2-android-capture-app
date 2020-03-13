package org.dhis2.usescases.reservedValue;


import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.databinding.ItemReservedValueBinding;
import org.dhis2.utils.NetworkUtils;
import org.hisp.dhis.android.core.trackedentity.ReservedValueSummary;

public class ReservedValueViewHolder extends RecyclerView.ViewHolder {

    private ItemReservedValueBinding binding;
    private ReservedValuePresenter presenter;

    public ReservedValueViewHolder(ItemReservedValueBinding binding, ReservedValuePresenter presenter) {
        super(binding.getRoot());
        this.binding = binding;
        this.presenter = presenter;

    }

    public void bind(ReservedValueSummary reservedValue) {
        binding.displayName.setText(reservedValue.trackedEntityAttribute().displayName());
        if (reservedValue.organisationUnit() != null) {
            binding.orgUnit.setVisibility(View.VISIBLE);
            binding.orgUnit.setText(reservedValue.organisationUnit().displayName());
        } else {
            binding.orgUnit.setVisibility(View.GONE);
        }
        binding.reservedValue.setText(
                String.format(
                        itemView.getContext().getString(R.string.reserved_values_left),
                        String.valueOf(reservedValue.count())));
        binding.refill.setOnClickListener(view -> presenter.onClickRefill(reservedValue));
        binding.setIsConnected(NetworkUtils.isOnline(binding.reservedValue.getContext()));
    }

}
