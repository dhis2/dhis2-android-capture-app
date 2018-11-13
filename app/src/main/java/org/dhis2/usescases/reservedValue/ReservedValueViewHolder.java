package org.dhis2.usescases.reservedValue;


import android.support.v7.widget.RecyclerView;
import org.dhis2.databinding.ItemReservedValueBinding;
import com.android.databinding.library.baseAdapters.BR;
public class ReservedValueViewHolder extends RecyclerView.ViewHolder {

    private ItemReservedValueBinding binding;

    public ReservedValueViewHolder(ItemReservedValueBinding binding) {
        super(binding.getRoot());
        this.binding = binding;

    }

    public void bind(ReservedValueContracts.Presenter presenter, ReservedValueModel dataElement){
        //TODO cambiarlo en el xml tambien
        binding.setVariable(BR.dataElement, dataElement);
    }

}
