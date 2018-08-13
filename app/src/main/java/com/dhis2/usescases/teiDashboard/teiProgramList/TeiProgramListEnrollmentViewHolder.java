package com.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.android.databinding.library.baseAdapters.BR;
import com.dhis2.R;
import com.dhis2.databinding.ItemTeiProgramsEnrollmentBinding;
import com.dhis2.databinding.ItemTeiProgramsEnrollmentInactiveBinding;
import com.dhis2.utils.ColorUtils;

import org.hisp.dhis.android.core.program.ProgramModel;

/**
 * Created by Cristian on 13/02/2018.
 */

public class TeiProgramListEnrollmentViewHolder extends RecyclerView.ViewHolder {

    private ViewDataBinding binding;

    TeiProgramListEnrollmentViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TeiProgramListContract.Presenter presenter, EnrollmentViewModel enrollment, ProgramModel programModel) {
        binding.setVariable(BR.enrollment, enrollment);
        binding.setVariable(BR.program, programModel);
        binding.setVariable(BR.presenter, presenter);

        if (enrollment != null) {

            ImageView programImage;
            if (binding instanceof ItemTeiProgramsEnrollmentBinding)
                programImage = ((ItemTeiProgramsEnrollmentBinding) binding).programImage;
            else
                programImage = ((ItemTeiProgramsEnrollmentInactiveBinding) binding).programImage;

            int color = enrollment != null ? ColorUtils.getColorFrom(itemView.getContext(), enrollment.color()) : ColorUtils.getPrimaryColor(itemView.getContext(), ColorUtils.ColorType.PRIMARY_LIGHT);
            int icon;
            if (enrollment != null && enrollment.icon() != null) {
                Resources resources = itemView.getContext().getResources();
                String iconName = enrollment.icon().startsWith("ic_") ? enrollment.icon() : "ic_" + enrollment.icon();
                icon = resources.getIdentifier(iconName, "drawable", itemView.getContext().getPackageName());
            } else {
                icon = R.drawable.ic_program_default;
            }

            Drawable iconImage = ContextCompat.getDrawable(itemView.getContext(), icon);

            programImage.setImageDrawable(ColorUtils.tintDrawableReosurce(iconImage, color));

            Drawable bgImage = ContextCompat.getDrawable(itemView.getContext(), R.drawable.photo_temp_gray);
            programImage.setBackground(ColorUtils.tintDrawableWithColor(bgImage,color));

        }

        binding.executePendingBindings();
    }
}