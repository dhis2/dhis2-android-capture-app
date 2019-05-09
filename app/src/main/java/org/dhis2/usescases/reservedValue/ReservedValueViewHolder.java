package org.dhis2.usescases.reservedValue;


import androidx.recyclerview.widget.RecyclerView;
import org.dhis2.databinding.ItemReservedValueBinding;
import org.dhis2.BR;
import org.dhis2.utils.NetworkUtils;
public class ReservedValueViewHolder extends RecyclerView.ViewHolder {

    private ItemReservedValueBinding binding;
    private ReservedValueContracts.Presenter presenter;

    public ReservedValueViewHolder(ItemReservedValueBinding binding, ReservedValueContracts.Presenter presenter) {
        super(binding.getRoot());
        this.binding = binding;
        this.presenter = presenter;

    }

    public void bind(ReservedValueContracts.Presenter presenter, ReservedValueModel dataElement){
        //TODO cambiarlo en el xml tambien
        binding.setVariable(BR.dataElement, dataElement);
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.isConnected, NetworkUtils.isOnline(binding.reservedValue.getContext()));
    }

}
