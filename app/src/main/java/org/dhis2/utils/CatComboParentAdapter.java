package org.dhis2.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.dhis2.R;
import org.dhis2.databinding.SpinnerLayoutBinding;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

/**
 * Created by ppajuelo on 12/02/2018.
 */

public class CatComboParentAdapter extends ArrayAdapter<CategoryOptionComboModel> {

    protected List<CategoryOptionComboModel> options;
    protected String catComboName;

    CatComboParentAdapter(@NonNull Context context,
                          int resource,
                          int textViewResourceId,
                          @NonNull List<CategoryOptionComboModel> objects,
                          String categoryOptionName) {
        super(context, resource, textViewResourceId, objects);
        this.options = objects;
        this.catComboName = categoryOptionName;
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