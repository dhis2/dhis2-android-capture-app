package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ItemIndicatorBinding;
import org.dhis2.utils.Constants;
import org.dhis2.utils.custom_views.CustomDialog;
import org.hisp.dhis.android.core.program.ProgramIndicator;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class IndicatorViewHolder extends RecyclerView.ViewHolder {
    ItemIndicatorBinding binding;

    IndicatorViewHolder(ItemIndicatorBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Trio<ProgramIndicator, String, String> programIndicatorModel) {
        if (programIndicatorModel.val0() == null) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.guideline.getLayoutParams();
            params.guidePercent = 0;
            binding.guideline.setLayoutParams(params);
        } else {
            binding.setVariable(BR.label, programIndicatorModel.val0().displayName());
            binding.setVariable(BR.description, programIndicatorModel.val0().displayDescription());
        }

        binding.setVariable(BR.value, programIndicatorModel.val1());
        binding.setVariable(BR.colorBg, programIndicatorModel.val2().isEmpty() ? -1 : Color.parseColor(programIndicatorModel.val2()));
        binding.executePendingBindings();

        binding.descriptionLabel.setOnClickListener(view -> showDescription(programIndicatorModel.val0()));
    }

    private void showDescription(@NonNull ProgramIndicator programIndicatorModel) {
        new CustomDialog(
                itemView.getContext(),
                programIndicatorModel.displayName(),
                programIndicatorModel.displayDescription(),
                itemView.getContext().getString(R.string.action_accept),
                null,
                Constants.DESCRIPTION_DIALOG,
                null
        ).show();
    }
}
