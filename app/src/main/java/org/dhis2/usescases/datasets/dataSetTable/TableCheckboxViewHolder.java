package org.dhis2.usescases.datasets.dataSetTable;


import android.widget.RadioButton;

import org.dhis2.R;
import org.dhis2.databinding.ItemTableCheckboxBinding;

import androidx.recyclerview.widget.RecyclerView;

public class TableCheckboxViewHolder extends RecyclerView.ViewHolder{

    private ItemTableCheckboxBinding binding;
    private DataSetTableContract.Presenter presenter;
    private boolean isFirst = true;
    public TableCheckboxViewHolder(ItemTableCheckboxBinding binding, DataSetTableContract.Presenter presenter) {
        super(binding.getRoot());
        this.binding = binding;
        this.presenter = presenter;
        isFirst = true;
    }

    public void bind(String title) {
        RadioButton radio = new RadioButton(binding.getRoot().getContext());
        radio.setText(title);

        radio.setTextColor(binding.getRoot().getContext().getResources().getColor(R.color.white));

        radio.setOnCheckedChangeListener((checkButton, isChecked) ->{
            if(isChecked)
                presenter.onClickSelectTable(this.getAdapterPosition());
        } );

        if(isFirst)
            radio.setChecked(true);

        isFirst = false;
        binding.radioGroup.addView(radio);
        //binding.setTitle(title);
    }
}
