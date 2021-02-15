package org.dhis2.usescases.programStageSelection;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;

import org.dhis2.BR;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.databinding.ItemProgramStageBinding;
import org.dhis2.utils.resources.ResourceManager;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.program.ProgramStage;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import timber.log.Timber;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramStageSelectionViewHolder extends RecyclerView.ViewHolder {

    private ItemProgramStageBinding binding;

    ProgramStageSelectionViewHolder(ItemProgramStageBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramStageSelectionContract.Presenter presenter, ProgramStage programStage) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.programStage, programStage);
        binding.executePendingBindings();
        ObjectStyle style;
        if(programStage.style() != null)
            style = programStage.style();
        else
            style = ObjectStyle.builder().build();

        if (style.icon() != null) {
            try {
                int icon = new ResourceManager(binding.programStageIcon.getContext())
                        .getObjectStyleDrawableResource(style.icon(), R.drawable.ic_default_icon);
                binding.programStageIcon.setImageResource(icon);
            }catch (Exception e){
                Timber.e(e);
            }
        }

        if (style.color() != null) {
            String color = style.color().startsWith("#") ? style.color() : "#" + style.color();
            int colorRes = Color.parseColor(color);
            ColorStateList colorStateList = ColorStateList.valueOf(colorRes);
            ViewCompat.setBackgroundTintList(binding.programStageIcon, colorStateList);
            Bindings.setFromResBgColor(binding.programStageIcon, colorRes);
        }

        itemView.setOnClickListener(view -> {
            if (programStage.access().data().write())
                presenter.onProgramStageClick(programStage);
            else
                presenter.displayMessage(null);
        });
    }
}
