package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.res.Resources;
import androidx.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.databinding.ItemTeiProgramsEnrollmentBinding;
import org.dhis2.databinding.ItemTeiProgramsEnrollmentInactiveBinding;
import org.dhis2.databinding.ItemTeiProgramsProgramsBinding;
import org.dhis2.usescases.main.program.ProgramViewModel;
import org.dhis2.utils.ColorUtils;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class TeiProgramListEnrollmentViewHolder extends RecyclerView.ViewHolder {

    private ViewDataBinding binding;

    TeiProgramListEnrollmentViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TeiProgramListContract.Presenter presenter, EnrollmentViewModel enrollment, ProgramViewModel programModel) {
        binding.setVariable(BR.enrollment, enrollment);
        binding.setVariable(BR.program, programModel);
        binding.setVariable(BR.presenter, presenter);

        if (enrollment != null) {

            ImageView programImage;
            RelativeLayout iconBg;
            if (binding instanceof ItemTeiProgramsEnrollmentBinding) {
                programImage = ((ItemTeiProgramsEnrollmentBinding) binding).programImage;
                iconBg = ((ItemTeiProgramsEnrollmentBinding) binding).iconBg;
            } else {
                programImage = ((ItemTeiProgramsEnrollmentInactiveBinding) binding).programImage;
                iconBg = ((ItemTeiProgramsEnrollmentInactiveBinding) binding).iconBg;
            }

            int color = enrollment != null ? ColorUtils.getColorFrom(enrollment.color(),
                    ColorUtils.getPrimaryColor(itemView.getContext(), ColorUtils.ColorType.PRIMARY)) : ColorUtils.getPrimaryColor(itemView.getContext(), ColorUtils.ColorType.PRIMARY_LIGHT);
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
            iconBg.setBackground(ColorUtils.tintDrawableWithColor(bgImage, color));

        } else if (programModel != null) {
            ImageView programImage;
            RelativeLayout iconBg;
            if (binding instanceof ItemTeiProgramsProgramsBinding) {
                programImage = ((ItemTeiProgramsProgramsBinding) binding).programImage;
                iconBg = ((ItemTeiProgramsProgramsBinding) binding).iconBg;

            } else {
                programImage = ((ItemTeiProgramsEnrollmentInactiveBinding) binding).programImage;
                iconBg = ((ItemTeiProgramsEnrollmentInactiveBinding) binding).iconBg;

            }

            int color = programModel != null ? ColorUtils.getColorFrom(programModel.color(),
                    ColorUtils.getPrimaryColor(itemView.getContext(), ColorUtils.ColorType.PRIMARY)) : ColorUtils.getPrimaryColor(itemView.getContext(), ColorUtils.ColorType.PRIMARY_LIGHT);
            int icon;
            if (programModel != null && programModel.icon() != null) {
                Resources resources = itemView.getContext().getResources();
                String iconName = programModel.icon().startsWith("ic_") ? programModel.icon() : "ic_" + programModel.icon();
                icon = resources.getIdentifier(iconName, "drawable", itemView.getContext().getPackageName());
            } else {
                icon = R.drawable.ic_program_default;
            }

            Drawable iconImage = ContextCompat.getDrawable(itemView.getContext(), icon);

            programImage.setImageDrawable(ColorUtils.tintDrawableReosurce(iconImage, color));

            Drawable bgImage = ContextCompat.getDrawable(itemView.getContext(), R.drawable.photo_temp_gray);
            iconBg.setBackground(ColorUtils.tintDrawableWithColor(bgImage, color));
        }

        binding.executePendingBindings();
    }
}