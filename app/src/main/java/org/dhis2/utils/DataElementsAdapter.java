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

public class DataElementsAdapter extends ArrayAdapter<DataElement> {

    private final List<DataElement> dataElements;
    private @ColorRes
    int textColor;

    public DataElementsAdapter(@NonNull Context context, int resource, int textViewResourceId,
            @NonNull List<DataElement> objects, @ColorRes int textColor) {
        super(context, resource, textViewResourceId, objects);
        this.dataElements = objects;
        this.textColor = textColor;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerDataelementLayoutBinding binding = SpinnerDataelementLayoutBinding.inflate(inflater,
                parent, false);
        binding.setDataElement(dataElements.get(position));
        binding.spinnerText.setTextColor(
                ContextCompat.getColor(binding.spinnerText.getContext(), textColor));
        return binding.getRoot();

    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
            @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerDataelementLayoutBinding binding = SpinnerDataelementLayoutBinding.inflate(inflater,
                parent, false);
        binding.setDataElement(dataElements.get(position));

        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return super.getCount();
    }
}