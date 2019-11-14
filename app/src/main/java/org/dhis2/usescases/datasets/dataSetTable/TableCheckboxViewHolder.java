package org.dhis2.usescases.datasets.dataSetTable;


import org.dhis2.databinding.ItemTableCheckboxBinding;
import androidx.recyclerview.widget.RecyclerView;

public class TableCheckboxViewHolder extends RecyclerView.ViewHolder{

    private ItemTableCheckboxBinding binding;
    private DataSetTablePresenter presenter;

    public TableCheckboxViewHolder(ItemTableCheckboxBinding binding, DataSetTablePresenter presenter) {
        super(binding.getRoot());
        this.binding = binding;
        this.presenter = presenter;
    }

    public void bind(String title, int position, int selectedPosition) {

        binding.setTitle(title);

        if(position != selectedPosition) {
            binding.radioButton.setChecked(false);
        }

        binding.radioButton.setOnCheckedChangeListener((checkButton, isChecked) ->{
            if(isChecked) {
                binding.radioButton.setChecked(true);
                presenter.onClickSelectTable(this.getAdapterPosition());

            }else
                binding.radioButton.setChecked(false);
        } );

        binding.executePendingBindings();
    }
}
