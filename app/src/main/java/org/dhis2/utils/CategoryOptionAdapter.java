package org.dhis2.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.dhis2.R;
import org.dhis2.databinding.SpinnerLayoutBinding;
import org.hisp.dhis.android.core.category.CategoryOption;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

/**
 * Created by ppajuelo on 12/02/2018.
 */

public class CategoryOptionAdapter extends ArrayAdapter<String> {

    private List<CategoryOption> options;
    private String catName;
    private @ColorRes
    int textColor;

    public CategoryOptionAdapter(@NonNull Context context, int resource, int textViewResourceId,
                                 @NonNull List<CategoryOption> objects,
                                 String categoryName,
                                 @ColorRes int textColor) {
        super(context, resource, textViewResourceId, transformToStringArray(objects));
        this.options = objects;
        this.catName = categoryName;
        this.textColor = textColor;

    }

    private static List<String> transformToStringArray(List<CategoryOption> categoryOptions){
        List<String> optionsByName = new ArrayList<>();
        for (CategoryOption catOpt : categoryOptions)
            optionsByName.add(catOpt.displayName());
        return optionsByName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_layout, parent, false);
        if (position != 0)
            binding.setOption(options.get(position - 1).displayName());
        binding.setOptionSetName(catName);
        binding.spinnerText.setTextColor(ContextCompat.getColor(binding.spinnerText.getContext(), textColor));
        return binding.getRoot();

    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_layout, parent, false);
        if (position != 0)
            binding.setOption(options.get(position - 1).displayName());

        binding.setOptionSetName(catName);
        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    public CategoryOption getSelectedOption(int position) {
        return options.get(position);
    }

}