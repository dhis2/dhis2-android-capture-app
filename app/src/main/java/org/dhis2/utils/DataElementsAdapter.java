package org.dhis2.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.dhis2.databinding.SpinnerDataelementLayoutBinding;
import org.hisp.dhis.android.core.dataelement.DataElement;

import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Created by ppajuelo on 12/02/2018.
 *
 */

public class DataElementsAdapter extends ArrayAdapter<DataElement> {

    private final List<DataElement> dataElements;
    private @ColorRes int textColor;
    private final String defaultText;

    public DataElementsAdapter(@NonNull Context context, int resource, int textViewResourceId,
                           @NonNull List<DataElement> objects, String defaultText,
                           @ColorRes int textColor) {
        super(context, resource, textViewResourceId, objects);
        this.dataElements = objects;
        this.defaultText = defaultText;
        this.textColor = textColor;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerDataelementLayoutBinding binding = SpinnerDataelementLayoutBinding.inflate(inflater,parent,false);
        if (position != 0)
            binding.setDataElement(dataElements.get(position - 1));
        //binding.setDefaultTitle(defaultText);
        binding.spinnerText.setTextColor(ContextCompat.getColor(binding.spinnerText.getContext(), textColor));
        return binding.getRoot();

    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerDataelementLayoutBinding binding = SpinnerDataelementLayoutBinding.inflate(inflater, parent, false);
        if (position != 0)
            binding.setDataElement(dataElements.get(position - 1));

        //binding.setDefaultTitle(defaultText);
        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }
}