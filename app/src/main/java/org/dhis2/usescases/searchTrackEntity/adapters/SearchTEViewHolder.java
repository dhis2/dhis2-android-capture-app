package org.dhis2.usescases.searchTrackEntity.adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
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
import org.dhis2.databinding.ItemFieldValueBinding;
import org.dhis2.databinding.ItemSearchTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.dhis2.utils.ColorUtils;
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
        setAttributeList(searchTeiModel.getAttributeValues());
        setEnrollmentStatusText(searchTeiModel.getSelectedEnrollment(), binding.getOverdue(), searchTeiModel.getOverdueDate());
        setTeiImage(searchTeiModel);

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


    }

    private void setAttributeList(LinkedHashMap<String, TrackedEntityAttributeValue> attributeValues) {
        binding.attributeList.removeAllViews();
        if (attributeValues.size() > 3) {
            for (int pos = 1; pos < attributeValues.size(); pos++) {
                String fieldName = attributeValues.keySet().toArray(new String[attributeValues.size()])[pos];
                String fieldValue = attributeValues.get(fieldName).value();
                ItemFieldValueBinding itemFieldValueBinding = ItemFieldValueBinding.inflate(LayoutInflater.from(binding.attributeList.getContext()));
                itemFieldValueBinding.setName(fieldName);
                itemFieldValueBinding.setValue(fieldValue);
                itemFieldValueBinding.getRoot().setTag(getAdapterPosition() + "_" + fieldName);
                binding.attributeList.addView(itemFieldValueBinding.getRoot());
            }
            binding.showAttributesButton.setOnClickListener(view -> {
                binding.showAttributesButton.animate()
                        .scaleY(binding.attributeList.getVisibility() == View.VISIBLE ? 1 : -1)
                        .setDuration(200).start();
                boolean shouldShowAttributeList = binding.attributeList.getVisibility() == View.GONE;
                if (shouldShowAttributeList) {
                    showAttributeList();
                } else {
                    hideAttributeList();
                }
            });
        } else {
            binding.showAttributesButton.setOnClickListener(null);
        }
    }

    private void showAttributeList() {
        binding.attributeBName.setVisibility(View.GONE);
        binding.attributeCName.setVisibility(View.GONE);
        binding.sortingFieldName.setVisibility(View.GONE);
        binding.entityAttribute2.setVisibility(View.GONE);
        binding.entityAttribute3.setVisibility(View.GONE);
        binding.sortingFieldValue.setVisibility(View.GONE);
        binding.attributeList.setVisibility(View.VISIBLE);
    }

    private void hideAttributeList() {
        binding.attributeList.setVisibility(View.GONE);
        binding.attributeBName.setVisibility(View.VISIBLE);
        binding.attributeCName.setVisibility(View.VISIBLE);
        binding.sortingFieldName.setVisibility(View.VISIBLE);
        binding.entityAttribute2.setVisibility(View.VISIBLE);
        binding.entityAttribute3.setVisibility(View.VISIBLE);
        binding.sortingFieldValue.setVisibility(View.VISIBLE);
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
            textToShow = DateExtensionsKt.toUiText(dueDate,itemView.getContext());
            color = Color.parseColor("#E91E63");
        } else if (selectedEnrollment.status() == EnrollmentStatus.CANCELLED) {
            textToShow = itemView.getContext().getString(R.string.cancelled);
            color = Color.parseColor("#E91E63");
        } else if (selectedEnrollment.status() == EnrollmentStatus.COMPLETED) {
            textToShow = itemView.getContext().getString(R.string.completed);
            color = Color.parseColor("#8A333333");
        }
        binding.enrollmentStatus.setVisibility(textToShow == null ? View.GONE : View.VISIBLE);

        binding.enrollmentStatus.setText(textToShow);
        binding.enrollmentStatus.setTextColor(color);
        GradientDrawable bgDrawable = (GradientDrawable) AppCompatResources.getDrawable(itemView.getContext(), R.drawable.round_border_box_2);
        bgDrawable.setStroke(2, color);
        binding.enrollmentStatus.setBackground(bgDrawable);
    }

    private void setTeiImage(SearchTeiModel searchTeiModel) {
        Drawable imageBg = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.photo_temp_gray);
        imageBg.setColorFilter(new PorterDuffColorFilter(ColorUtils.getPrimaryColor(itemView.getContext(), ColorUtils.ColorType.PRIMARY), PorterDuff.Mode.SRC_IN));
        binding.trackedEntityImage.setBackground(imageBg);

        File file = new File(searchTeiModel.getProfilePicturePath());
        int placeHolderId = new ResourceManager(itemView.getContext()).getObjectStyleDrawableResource(searchTeiModel.getDefaultTypeIcon(), -1);
        if (file.exists()) {
            binding.imageText.setVisibility(View.GONE);
            Glide.with(itemView.getContext())
                    .load(file)
                    .placeholder(placeHolderId)
                    .error(placeHolderId)
                    .transition(withCrossFade())
                    .transform(new CircleCrop())
                    .into(binding.trackedEntityImage);
        } else if (placeHolderId != -1) {
            binding.imageText.setVisibility(View.GONE);
            binding.trackedEntityImage.setImageResource(placeHolderId);
        } else if (searchTeiModel.getAttributeValues() != null && searchTeiModel.getAttributeValues().values().size() > 0) {
            binding.trackedEntityImage.setImageDrawable(null);
            binding.imageText.setVisibility(View.VISIBLE);
            String valueToShow = new ArrayList<>(searchTeiModel.getAttributeValues().values()).get(0).value();
            binding.imageText.setText(valueToShow != null ? String.valueOf(valueToShow.charAt(0)) : null);
            binding.imageText.setTextColor(ColorUtils.getContrastColor(ColorUtils.getPrimaryColor(itemView.getContext(), ColorUtils.ColorType.PRIMARY)));
        } else {
            binding.imageText.setVisibility(View.GONE);
        }
    }
}
