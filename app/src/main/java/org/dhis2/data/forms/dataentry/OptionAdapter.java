package org.dhis2.data.forms.dataentry;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.dhis2.R;
import org.dhis2.databinding.SpinnerLayoutBinding;
import org.dhis2.databinding.SpinnerLayoutItemsBinding;

import org.hisp.dhis.android.core.option.OptionModel;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 07/11/2017.
 */

public class OptionAdapter extends ArrayAdapter<OptionModel> {

    private List<OptionModel> options;
    private String optionSetName;

    public OptionAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<OptionModel> objects, String optionSetName) {
        super(context, resource, textViewResourceId, objects);
        this.options = objects;
        this.optionSetName = optionSetName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_layout, parent, false);
        if (position != 0)
            binding.setOption(options.get(position - 1).displayName());
        binding.setOptionSetName(optionSetName);
        binding.spinnerText.setTextColor(ContextCompat.getColor(binding.spinnerText.getContext(), R.color.white_faf));
        return binding.getRoot();

    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerLayoutItemsBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_layout_items, parent, false);
        if (position != 0)
            binding.setOption(options.get(position - 1).displayName());

        binding.setOptionSetName(optionSetName);
        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    public int getOptionCount() {
        return options.size();
    }

}
