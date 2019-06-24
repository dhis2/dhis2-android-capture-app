package org.dhis2.usescases.searchTrackEntity.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.chip.Chip;

import org.dhis2.R;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ItemSearchTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.ObjectStyleUtils;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.io.File;
import java.util.List;
import java.util.Random;

import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

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

        binding.trackedEntityImage.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.photo_temp_gray));
        binding.followUp.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_circle_red));

        binding.syncState.setOnClickListener(view -> presenter.onSyncIconClick(searchTeiModel.getTei().uid()));

        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onTEIClick(searchTeiModel.getTei().uid(), searchTeiModel.isOnline()));

        String fileName = searchTeiModel.getTei().uid() + "_" + searchTeiModel.getProfilePictureUid() + ".png";
        File file = new File(itemView.getContext().getFilesDir(), fileName);
        Drawable placeHolderId = ObjectStyleUtils.getIconResource(itemView.getContext(), searchTeiModel.getDefaultTypeIcon(), R.drawable.photo_temp_gray);
        Glide.with(itemView.getContext())
                .load(file)
                .placeholder(placeHolderId)
                .error(placeHolderId)
                .transition(withCrossFade())
                .transform(new CircleCrop())
                .into(binding.trackedEntityImage);

    }


    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }

    private void setEnrollment(List<EnrollmentModel> enrollments) {
//        binding.linearLayout.removeAllViews();
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
            if (/*binding.chipContainer.getChildCount() < 2 &&*/
                    (binding.getPresenter().getProgramModel() == null || !binding.getPresenter().getProgramModel().displayName().equals(enrollmentInfo.val0()))) {

                Chip chip = new Chip(parentContext);
                chip.setText(enrollmentInfo.val0());

                int color = ColorUtils.Companion.getColorFrom(enrollmentInfo.val1(), ColorUtils.Companion.getPrimaryColor(parentContext, ColorUtils.ColorType.PRIMARY_LIGHT));
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
                    iconImage = AppCompatResources.getDrawable(parentContext, icon);
                    iconImage.mutate();
                } catch (Exception e) {
                    Timber.log(1, e);
                    iconImage = AppCompatResources.getDrawable(parentContext, R.drawable.ic_program_default);
                    iconImage.mutate();
                }

                Drawable bgDrawable = AppCompatResources.getDrawable(parentContext, R.drawable.ic_chip_circle_24);

                Drawable wrappedIcon = DrawableCompat.wrap(iconImage);
                Drawable wrappedBg = DrawableCompat.wrap(bgDrawable);

                LayerDrawable finalDrawable = new LayerDrawable(new Drawable[]{wrappedBg, wrappedIcon});

                finalDrawable.mutate();

                finalDrawable.getDrawable(1).setColorFilter(new PorterDuffColorFilter(ColorUtils.Companion.getContrastColor(color), PorterDuff.Mode.SRC_IN));
                finalDrawable.getDrawable(0).setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));

                chip.setChipIcon(finalDrawable);

                binding.chipContainer.addView(chip);
                binding.chipContainer.invalidate();
            }
        }
    }

}
