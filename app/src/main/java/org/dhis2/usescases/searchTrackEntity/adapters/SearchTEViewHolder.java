package org.dhis2.usescases.searchTrackEntity.adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.dhis2.Bindings.DateExtensionsKt;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.R;
import org.dhis2.databinding.ItemSearchTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.ObjectStyleUtils;
import org.dhis2.utils.resources.ResourceManager;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import timber.log.Timber;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class SearchTEViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchTrackedEntityBinding binding;

    SearchTEViewHolder(ItemSearchTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }


    public void bind(SearchTEContractsModule.Presenter presenter, SearchTeiModel searchTeiModel) {
        binding.setOverdue(searchTeiModel.isHasOverdue());
        binding.setIsOnline(searchTeiModel.isOnline());
        binding.setSyncState(searchTeiModel.getTei().state());

        setEnrollment(searchTeiModel.getEnrollments());
        setProgramInfo(searchTeiModel.getProgramInfo(), presenter.getProgram() != null ? presenter.getProgram().uid() : null);
        setTEIData(searchTeiModel.getAttributeValues());
        setEnrollmentStatusText(searchTeiModel.getSelectedEnrollment(), binding.getOverdue(), searchTeiModel.getOverdueDate());

        binding.trackedEntityImage.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.photo_temp_gray));
        binding.followUp.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_circle_red));

        binding.syncState.setOnClickListener(view -> {
            if (searchTeiModel.getTei().deleted() ||
                    searchTeiModel.getSelectedEnrollment() != null && searchTeiModel.getSelectedEnrollment().deleted())
                Toast.makeText(itemView.getContext(), itemView.getContext().getString(R.string.record_marked_for_deletion), Toast.LENGTH_SHORT).show();
            else
                presenter.onSyncIconClick(searchTeiModel.getTei().uid());
        });
        binding.lastUpdated.setText(
                DateExtensionsKt.toDateSpan(
                        searchTeiModel.getTei().lastUpdated(),
                        itemView.getContext()
                ));

        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onTEIClick(
                searchTeiModel.getTei().uid(),
                searchTeiModel.getSelectedEnrollment() != null ? searchTeiModel.getSelectedEnrollment().uid() : null,
                searchTeiModel.isOnline()));

        File file = new File(searchTeiModel.getProfilePicturePath());
        Drawable placeHolderId = ObjectStyleUtils.getIconResource(itemView.getContext(), searchTeiModel.getDefaultTypeIcon(), R.drawable.photo_temp_gray);
        if (file.exists())
            Glide.with(itemView.getContext())
                    .load(file)
                    .placeholder(placeHolderId)
                    .error(placeHolderId)
                    .transition(withCrossFade())
                    .transform(new CircleCrop())
                    .into(binding.trackedEntityImage);
        else
            binding.trackedEntityImage.setImageDrawable(placeHolderId);

    }


    private void setTEIData(LinkedHashMap<String, TrackedEntityAttributeValue> trackedEntityAttributeValues) {
        binding.setAttribute(new ArrayList<>(trackedEntityAttributeValues.values()));
        binding.setAttributeNames(trackedEntityAttributeValues.keySet());
        binding.executePendingBindings();
    }

    private void setEnrollment(List<Enrollment> enrollments) {
        boolean isFollowUp = false;
        for (Enrollment enrollment : enrollments) {
            if (enrollment.followUp() != null && enrollment.followUp())
                isFollowUp = true;
        }

        binding.setFollowUp(isFollowUp);
    }

    private void setProgramInfo(List<Program> programs, String currentProgram) {
        binding.programList.removeAllViews();
        for (Program program : programs) {
            if (currentProgram == null || !currentProgram.equals(program.uid())) {
                int color = ColorUtils.getColorFrom(
                        program.style().color(),
                        ColorUtils.getPrimaryColor(itemView.getContext(), ColorUtils.ColorType.PRIMARY)
                );
                int imageResource = new ResourceManager(itemView.getContext()).getObjectStyleDrawableResource(
                        program.style().icon(),
                        -1
                );
                ImageView imageView = new ImageView(itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ExtensionsKt.getDp(24), ExtensionsKt.getDp(24));
                params.setMarginEnd(4);
                imageView.setLayoutParams(params);
                imageView.setImageDrawable(getProgramDrawable(color, imageResource));
                imageView.setPadding(0, 0, 0, 0);
                binding.programList.addView(imageView);
            }
        }
    }

    private Drawable getProgramDrawable(int color, int icon) {
        Drawable iconImage;
        try {
            iconImage = AppCompatResources.getDrawable(itemView.getContext(), icon);
            iconImage.mutate();
        } catch (Exception e) {
            Timber.log(1, e);
            iconImage = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_program_default);
            iconImage.mutate();
        }

        Drawable bgDrawable = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.rounded_square_r2_24);

        Drawable wrappedIcon = DrawableCompat.wrap(iconImage);
        Drawable wrappedBg = DrawableCompat.wrap(bgDrawable);

        LayerDrawable finalDrawable = new LayerDrawable(new Drawable[]{wrappedBg, wrappedIcon});

        finalDrawable.mutate();

        finalDrawable.getDrawable(1).setColorFilter(new PorterDuffColorFilter(ColorUtils.getContrastColor(color), PorterDuff.Mode.SRC_IN));
        finalDrawable.getDrawable(0).setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));

        return finalDrawable;
    }

    private void setEnrollmentStatusText(Enrollment selectedEnrollment, boolean isOverdue, Date dueDate) {
        String textToShow = null;
        int color = -1;
        if (isOverdue) {
            textToShow = DateExtensionsKt.toUiText(dueDate);
            color = Color.parseColor("#E91E63");
        } else if (selectedEnrollment.status() == EnrollmentStatus.CANCELLED) {
            textToShow = "Cancelled";
            color = Color.parseColor("#E91E63");
        } else if (selectedEnrollment.status() == EnrollmentStatus.COMPLETED) {
            textToShow = "Completed";
            color = Color.parseColor("#8A333333");
        }
        binding.enrollmentStatus.setVisibility(textToShow == null ? View.GONE : View.VISIBLE);

        binding.enrollmentStatus.setText(textToShow);
        binding.enrollmentStatus.setTextColor(color);
        GradientDrawable bgDrawable = (GradientDrawable) AppCompatResources.getDrawable(itemView.getContext(), R.drawable.round_border_box_2);
        bgDrawable.setStroke(2, color);
        binding.enrollmentStatus.setBackground(bgDrawable);

    }

}
