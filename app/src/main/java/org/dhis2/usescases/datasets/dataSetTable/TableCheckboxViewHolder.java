package org.dhis2.usescases.datasets.dataSetTable;


import android.os.Build;
import android.widget.RadioButton;

import org.dhis2.R;
import org.dhis2.databinding.ItemTableCheckboxBinding;

import androidx.recyclerview.widget.RecyclerView;

public class TableCheckboxViewHolder extends RecyclerView.ViewHolder{

    private ItemTableCheckboxBinding binding;
    private DataSetTableContract.Presenter presenter;

    public TableCheckboxViewHolder(ItemTableCheckboxBinding binding, DataSetTableContract.Presenter presenter) {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    binding.radioButton.setButtonDrawable(binding.getRoot().getContext().getResources().getDrawable(R.drawable.ic_check_circle_36, null));
                }
                presenter.onClickSelectTable(this.getAdapterPosition());

            }else
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    binding.radioButton.setButtonDrawable(binding.getRoot().getContext().getResources().getDrawable(android.R.color.transparent, null));
                }

        } );

        binding.executePendingBindings();

    }
}
