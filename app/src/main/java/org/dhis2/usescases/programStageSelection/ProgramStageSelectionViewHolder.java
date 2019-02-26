package org.dhis2.usescases.programStageSelection;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;

import org.dhis2.BR;
import org.dhis2.Bindings.Bindings;
import org.dhis2.databinding.ItemProgramStageBinding;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramStageSelectionViewHolder extends RecyclerView.ViewHolder {

    private ItemProgramStageBinding binding;

    ProgramStageSelectionViewHolder(ItemProgramStageBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramStageSelectionContract.Presenter presenter, ProgramStageModel programStage, ObjectStyleModel data) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.programStage, programStage);
        binding.executePendingBindings();

        if (data.icon() != null) {
            Resources resources = binding.programStageIcon.getContext().getResources();
            String iconName = data.icon().startsWith("ic_") ? data.icon() : "ic_" + data.icon();
            int icon = resources.getIdentifier(iconName, "drawable", binding.programStageIcon.getContext().getPackageName());
            binding.programStageIcon.setImageResource(icon);
        }

        if (data.color() != null) {
            String color = data.color().startsWith("#") ? data.color() : "#" + data.color();
            int colorRes = Color.parseColor(color);
            ColorStateList colorStateList = ColorStateList.valueOf(colorRes);
            ViewCompat.setBackgroundTintList(binding.programStageIcon, colorStateList);
            Bindings.setFromResBgColor(binding.programStageIcon, colorRes);
        }

        itemView.setOnClickListener(view -> {
            if (programStage.accessDataWrite())
                presenter.onProgramStageClick(programStage);
            else
                presenter.displayMessage(null);
        });
    }
}
