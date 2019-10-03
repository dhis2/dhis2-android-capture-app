package org.dhis2.usescases.main.program;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import org.dhis2.R;
import org.dhis2.databinding.ItemProgramModelBinding;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Period;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

public class ProgramModelHolder extends RecyclerView.ViewHolder {

    private final ItemProgramModelBinding binding;

    public ProgramModelHolder(ItemProgramModelBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramContract.Presenter presenter, ProgramViewModel programViewModel) {
        binding.setProgram(programViewModel);
        binding.setPresenter(presenter);

        int color = ColorUtils.getColorFrom(programViewModel.color(), ColorUtils.getPrimaryColor(binding.programImage.getContext(), ColorUtils.ColorType.PRIMARY));
        int icon;
        if (programViewModel.icon() != null) {
            Resources resources = binding.programImage.getResources();
            String iconName = programViewModel.icon().startsWith("ic_") ? programViewModel.icon() : "ic_" + programViewModel.icon();
            icon = resources.getIdentifier(iconName, "drawable", binding.programImage.getContext().getPackageName());
        } else {
            icon = R.drawable.ic_program_default;
        }
        Drawable iconImage = AppCompatResources.getDrawable(binding.programImage.getContext(), R.drawable.ic_program_default);
        try {
            iconImage = AppCompatResources.getDrawable(binding.programImage.getContext(), icon);
        } catch (Exception e) {
            Timber.log(1, e);
        }

        if (iconImage != null) {
            iconImage.mutate();
        }

        binding.programImage.setImageDrawable(iconImage);
        binding.programImage.setColorFilter(ColorUtils.getContrastColor(color));
        binding.programImage.setBackgroundColor(color);

    }
}