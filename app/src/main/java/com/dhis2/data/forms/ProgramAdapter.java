package com.dhis2.data.forms;

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
import com.dhis2.databinding.SpinnerProgramLayoutBinding;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

/**
 * Created by ppajuelo on 07/11/2017.
 */

public class ProgramAdapter extends ArrayAdapter<ProgramModel> {

    List<ProgramModel> options;

    public ProgramAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<ProgramModel> objects) {
        super(context, resource, textViewResourceId, objects);
        this.options = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerProgramLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_program_layout, parent, false);
        if (position != 0)
            binding.setProgram(options.get(position - 1));
        binding.setProgramTitle("Programs");
        binding.spinnerText.setTextColor(ContextCompat.getColor(binding.spinnerText.getContext(), R.color.white_faf));
        return binding.getRoot();

    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerProgramLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_program_layout, parent, false);
        if (position != 0)
            binding.setProgram(options.get(position - 1));

        binding.setProgramTitle("Programs");
        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }
}
