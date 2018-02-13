package com.dhis2.utils;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.dhis2.R;
import com.dhis2.databinding.SpinnerLayoutBinding;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;

import java.util.List;

/**
 * Created by ppajuelo on 12/02/2018.
 */

public class CatComboAdapter extends ArrayAdapter<CategoryOptionComboModel> {

    List<CategoryOptionComboModel> options;
    String catComboName;

    public CatComboAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<CategoryOptionComboModel> objects, String categoryOptionName) {
        super(context, resource, textViewResourceId, objects);
        this.options = objects;
        this.catComboName = categoryOptionName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_layout, parent, false);
        if (position != 0)
            binding.setOption(options.get(position - 1).displayName());
        binding.setOptionSetName(catComboName);
        binding.spinnerText.setTextColor(ContextCompat.getColor(binding.spinnerText.getContext(), R.color.white_faf));
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
