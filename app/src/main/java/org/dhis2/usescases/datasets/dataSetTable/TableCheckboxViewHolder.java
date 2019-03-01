package org.dhis2.usescases.datasets.dataSetTable;


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
        /*RadioButton radio = new RadioButton(binding.getRoot().getContext());
        radio.setText(title);

        radio.setTextColor(binding.getRoot().getContext().getResources().getColor(R.color.white));
*/
        binding.setTitle(title);
        if(position != selectedPosition)
            binding.radioButton.setChecked(false);
        binding.radioButton.setOnCheckedChangeListener((checkButton, isChecked) ->{
            if(isChecked) {
                presenter.onClickSelectTable(this.getAdapterPosition());

            }
        } );

        binding.executePendingBindings();

       /* if(position == 0)
            radio.setChecked(true);

        binding.radioGroup.addView(radio);*/
        //binding.setTitle(title);
    }
}
