package org.dhis2.utils;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.dhis2.R;
import org.dhis2.databinding.SpinnerLayoutBinding;

import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import java.util.List;

/**
 * Created by ppajuelo on 12/02/2018.
 *
 */

public class CatComboAdapter extends ArrayAdapter<CategoryOptionCombo> {

    private List<CategoryOptionCombo> options;
    private String catComboName;
    private @ColorRes int textColor;

    public CatComboAdapter(@NonNull Context context, int resource, int textViewResourceId,
                           @NonNull List<CategoryOptionCombo> objects,
                           String categoryOptionName,
                           @ColorRes int textColor) {
        super(context, resource, textViewResourceId, objects);
        this.options = objects;
        this.catComboName = categoryOptionName;
        this.textColor = textColor;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerLayoutBinding binding = SpinnerLayoutBinding.inflate(inflater,parent,false);
        if (position != 0)
            binding.setOption(options.get(position - 1).displayName());
        binding.setOptionSetName(catComboName);
        binding.spinnerText.setTextColor(ContextCompat.getColor(binding.spinnerText.getContext(), textColor));
        return binding.getRoot();

    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_layout, parent, false);
        if (position != 0)
            binding.setOption(options.get(position - 1).displayName());

        binding.setOptionSetName(catComboName);
        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }
}