package org.dhis2.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.SpinnerTitleLayoutBinding;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

/**
 * Created by ppajuelo on 12/02/2018.
 */

public class CatComboAdapter2 extends CatComboParentAdapter {

    public CatComboAdapter2(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<CategoryOptionComboModel> objects, String categoryOptionName) {
        super(context, resource, textViewResourceId, objects, categoryOptionName);
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
}