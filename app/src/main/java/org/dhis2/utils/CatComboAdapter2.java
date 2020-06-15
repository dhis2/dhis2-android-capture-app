package org.dhis2.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.dhis2.R;
import org.dhis2.databinding.SpinnerLayoutBinding;
import org.dhis2.databinding.SpinnerTitleLayoutBinding;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;

import java.util.List;

/**
 * Created by ppajuelo on 12/02/2018.
 */

public class CatComboAdapter2 extends ArrayAdapter<CategoryOptionCombo> {

    private List<CategoryOptionCombo> options;
    private String catComboName;

    public CatComboAdapter2(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<CategoryOptionCombo> objects, String categoryOptionName) {
        super(context, resource, textViewResourceId, objects);
        this.options = objects;
        this.catComboName = categoryOptionName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerTitleLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_title_layout, parent, false);
        if (position != 0)
            binding.setOption(options.get(position - 1).displayName());
        binding.setOptionSetName(catComboName);
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