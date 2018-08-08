package com.dhis2.usescases.main.program;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import com.dhis2.R;
import com.dhis2.databinding.ItemProgramModelBinding;
import com.dhis2.utils.ColorUtils;
import com.dhis2.utils.Period;

import org.hisp.dhis.android.core.program.ProgramType;

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

public class ProgramModelHolder extends RecyclerView.ViewHolder {

    private final ItemProgramModelBinding binding;

    public ProgramModelHolder(ItemProgramModelBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramContract.Presenter presenter, ProgramViewModel programViewModel, Period currentPeriod) {
        binding.setProgram(programViewModel);
        binding.setPresenter(presenter);
        binding.setCurrentPeriod(currentPeriod);

        int color = ColorUtils.getColorFrom(binding.programImage.getContext(), programViewModel.color());
        int icon;
        if (programViewModel.icon() != null) {
            Resources resources = binding.programImage.getResources();
            String iconName = programViewModel.icon().startsWith("ic_") ? programViewModel.icon() : "ic_" + programViewModel.icon();
            icon = resources.getIdentifier(iconName, "drawable", binding.programImage.getContext().getPackageName());
        } else {
            icon = R.drawable.ic_program_default;
        }

        Drawable iconImage = ContextCompat.getDrawable(binding.programImage.getContext(), icon);

        binding.programImage.setImageDrawable(ColorUtils.tintDrawableReosurce(iconImage, color));

        binding.programImage.setBackgroundColor(color);

      /*  SpannableStringBuilder sb = new SpannableStringBuilder(String.format("%s %s", programViewModel.count(), programViewModel.typeName()));
        sb.setSpan(new AbsoluteSizeSpan(18,true), 0, programViewModel.count().toString().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, programViewModel.count().toString().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        binding.eventsNumber.setText(sb);*/
    }
}