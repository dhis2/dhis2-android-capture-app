package org.dhis2.usescases.searchTrackEntity.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.google.android.material.chip.Chip;

import org.dhis2.R;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ItemSearchTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.dhis2.utils.ColorUtils;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by frodriguez on 11/7/2017.
 */

public class SearchTEViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchTrackedEntityBinding binding;

    SearchTEViewHolder(ItemSearchTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }


    public void bind(SearchTEContractsModule.Presenter presenter, SearchTeiModel searchTeiModel) {
        binding.setPresenter(presenter);
        binding.setOverdue(searchTeiModel.isHasOverdue());
        binding.setIsOnline(searchTeiModel.isOnline());
        binding.setSyncState(searchTeiModel.getTei().state());

        setEnrollment(searchTeiModel.getEnrollmentModels());
        setEnrollmentInfo(searchTeiModel.getEnrollmentInfo());

        setTEIData(searchTeiModel.getAttributeValueModels());

        binding.trackedEntityImage.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.photo_temp_gray));
        binding.followUp.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_circle_red));

        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onTEIClick(searchTeiModel.getTei().uid(), searchTeiModel.isOnline()));

    }


    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }

    private void setEnrollment(List<EnrollmentModel> enrollments) {
        binding.linearLayout.removeAllViews();
        boolean isFollowUp = false;
        for (EnrollmentModel enrollment : enrollments) {
            if (enrollment.followUp() != null && enrollment.followUp())
                isFollowUp = true;
        }

        binding.setFollowUp(isFollowUp);
    }


    private void setEnrollmentInfo(List<Trio<String, String, String>> enrollmentsInfo) {
        binding.chipContainer.removeAllViews();

        Context parentContext = binding.chipContainer.getContext();
        for (Trio<String, String, String> enrollmentInfo : enrollmentsInfo) {
            if (binding.linearLayout.getChildCount() < 2 &&
                    (binding.getPresenter().getProgramModel() == null || !binding.getPresenter().getProgramModel().displayName().equals(enrollmentInfo.val0()))) {

                Chip chip = new Chip(parentContext);
                chip.setText(enrollmentInfo.val0());

                int color = ColorUtils.getColorFrom(enrollmentInfo.val1(), ColorUtils.getPrimaryColor(parentContext, ColorUtils.ColorType.PRIMARY_LIGHT));
                int icon;
                if (!isEmpty(enrollmentInfo.val2())) {
                    Resources resources = parentContext.getResources();
                    String iconName = enrollmentInfo.val2().startsWith("ic_") ? enrollmentInfo.val2() : "ic_" + enrollmentInfo.val2();
                    icon = resources.getIdentifier(iconName, "drawable", parentContext.getPackageName());
                } else {
                    icon = R.drawable.ic_program_default;
                }

                Drawable iconImage;
                try {
                    iconImage = ContextCompat.getDrawable(parentContext, icon);
                    iconImage.mutate();
                } catch (Exception e) {
                    Timber.log(1, e);
                    iconImage = ContextCompat.getDrawable(parentContext, R.drawable.ic_program_default);
                    iconImage.mutate();
                }

                Drawable bgDrawable = ContextCompat.getDrawable(parentContext, R.drawable.ic_chip_circle_24);

                Drawable wrappedIcon = DrawableCompat.wrap(iconImage);
                Drawable wrappedBg = DrawableCompat.wrap(bgDrawable);

                LayerDrawable finalDrawable = new LayerDrawable(new Drawable[]{wrappedBg, wrappedIcon});

                finalDrawable.mutate();

                finalDrawable.getDrawable(1).setColorFilter(new PorterDuffColorFilter(ColorUtils.getContrastColor(color), PorterDuff.Mode.SRC_IN));
                finalDrawable.getDrawable(0).setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));

                chip.setChipIcon(finalDrawable);

                binding.chipContainer.addView(chip);
                binding.chipContainer.invalidate();
            }
        }
    }

}
