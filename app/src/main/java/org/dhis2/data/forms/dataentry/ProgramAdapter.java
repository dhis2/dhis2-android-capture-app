package org.dhis2.data.forms.dataentry;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.dhis2.R;
import org.dhis2.databinding.SpinnerProgramLayoutBinding;

import org.hisp.dhis.android.core.program.Program;

import java.util.List;

/**
 * x
 * Created by ppajuelo on 07/11/2017-sdfghsdfh .
 */

public class ProgramAdapter extends ArrayAdapter<Program> {

    private List<Program> programs;
    private String trackedEntityName;

    public ProgramAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Program> programs, String trackedEntityName) {
        super(context, resource, textViewResourceId, programs);
        this.programs = programs;
        this.trackedEntityName = trackedEntityName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            SpinnerProgramLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_program_layout, parent, false);
            convertView = binding.getRoot();
            if (position > 0)
                binding.setProgram(programs.get(position - 1));
            binding.setProgramTitle(String.format(getContext().getString(R.string.all_tei_type), trackedEntityName));
            binding.spinnerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            binding.spinnerText.setTextColor(ContextCompat.getColor(binding.spinnerText.getContext(), R.color.white_faf));
        }
        return convertView;

    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerProgramLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_program_layout, parent, false);
        if (position != 0)
            binding.setProgram(programs.get(position - 1));

        binding.setProgramTitle(String.format(getContext().getString(R.string.all_tei_type), trackedEntityName));
        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

}
