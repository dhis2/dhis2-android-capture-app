package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.commons.data.EventViewModel;
import org.dhis2.commons.databinding.ItemFieldValueBinding;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.databinding.ItemEventBinding;
import org.dhis2.ui.MetadataIconData;
import org.dhis2.ui.MetadataIconKt;
import org.dhis2.utils.DhisTextUtils;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import kotlin.Pair;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function4;

public class EventViewHolder extends RecyclerView.ViewHolder {
    private final Program program;
    private final Function1<String, Unit> onSyncClick;
    private final Function2<String, View, Unit> onScheduleClick;
    private final Function4<String, String, EventStatus, View, Unit> onEventSelected;
    private ItemEventBinding binding;
    private ColorUtils colorUtils;

    public EventViewHolder(ItemEventBinding binding,
                           Program program,
                           ColorUtils colorUtils,
                           Function1<String, Unit> syncClick,
                           Function2<String, View, Unit> scheduleClick,
                           Function4<String, String, EventStatus, View, Unit> onEventSelected
    ) {
        super(binding.getRoot());
        this.binding = binding;
        this.program = program;
        this.onSyncClick = syncClick;
        this.onScheduleClick = scheduleClick;
        this.onEventSelected = onEventSelected;
        this.colorUtils = colorUtils;
        MetadataIconKt.handleComposeDispose(binding.composeStageIcon);
    }

    public void bind(EventViewModel eventModel, Enrollment enrollment, @NotNull Function0<Unit> toggleList) {
        Event event = eventModel.getEvent();
        binding.setEvent(eventModel.getEvent());
        binding.setStage(eventModel.getStage());
        binding.setEnrollment(enrollment);
        binding.setProgram(program);
        binding.executePendingBindings();

        if (eventModel.getGroupedByStage()) {
            binding.eventCard.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(),
                            program.programType() == ProgramType.WITH_REGISTRATION ? R.color.form_field_background : R.color.white));
            binding.programStageName.setVisibility(View.GONE);
            binding.composeStageIcon.setVisibility(View.INVISIBLE);
            binding.stageIconStatusImage.setVisibility(View.INVISIBLE);
            binding.eventStatus.setVisibility(View.VISIBLE);
        } else {
            binding.eventCard.setCardBackgroundColor(Color.WHITE);
            binding.programStageName.setVisibility(View.VISIBLE);
            binding.composeStageIcon.setVisibility(View.VISIBLE);
            binding.stageIconStatusImage.setVisibility(View.VISIBLE);
            binding.eventStatus.setVisibility(View.GONE);
            renderStageIcon(eventModel.getMetadataIconData());
        }

        String date = eventModel.getDisplayDate();
        binding.eventDate.setText(date);

        binding.organisationUnit.setText(eventModel.getOrgUnitName());
        if (DhisTextUtils.Companion.isNotEmpty(eventModel.getCatComboName()) && !eventModel.getCatComboName().equals("default")) {
            binding.catCombo.setVisibility(View.VISIBLE);
            binding.catCombo.setText(eventModel.getCatComboName());
        } else {
            binding.catCombo.setVisibility(View.GONE);
        }

        if (eventModel.getDataElementValues() != null && !eventModel.getDataElementValues().isEmpty()) {
            setEventValueLayout(eventModel, toggleList);
        } else {
            hideEventValueLayout();
        }

        binding.syncIcon.setOnClickListener(view -> onSyncClick.invoke(event.uid()));

        binding.eventCard.setOnClickListener(view -> {
            switch (eventModel.getEvent().status()) {
                case SCHEDULE:
                case OVERDUE:
                case SKIPPED:
                    onScheduleClick.invoke(event.uid(), binding.sharedView);
                    break;
                case VISITED:
                    break;
                case ACTIVE:
                case COMPLETED:
                    onEventSelected.invoke(event.uid(), event.organisationUnit(), event.status(), binding.sharedView);
                    break;
            }
        });

        showShadows(eventModel);
    }

    private void setEventValueLayout(EventViewModel eventModel, @NotNull Function0<Unit> toggleList) {
        binding.showValuesButton.setVisibility(View.VISIBLE);
        binding.showValuesButton.setOnClickListener(view -> toggleList.invoke());
        initValues(eventModel.getValueListIsOpen(), eventModel.getDataElementValues());
    }

    private void hideEventValueLayout() {
        binding.showValuesButton.setVisibility(View.GONE);
        binding.dataElementListGuideline.setVisibility(View.INVISIBLE);
        binding.dataElementList.setVisibility(View.GONE);
        binding.showValuesButton.setOnClickListener(null);
    }

    private void renderStageIcon(MetadataIconData metadataIconData) {
        MetadataIconKt.setUpMetadataIcon(
                binding.composeStageIcon,
                metadataIconData,
                false
        );
    }

    private void initValues(boolean valueListIsOpen, List<Pair<String, String>> dataElementValues) {
        binding.dataElementList.removeAllViews();
        binding.eventInfo.setText(null);
        binding.showValuesButton.setScaleY(valueListIsOpen ? 1F : -1F);
        binding.showValuesButton
                .animate()
                .scaleY(valueListIsOpen ? -1F : 1F)
                .setDuration(500)
                .withStartAction(() -> binding.showValuesButton.setScaleY(valueListIsOpen ? 1F : -1F))
                .withEndAction(() -> binding.showValuesButton.setScaleY(valueListIsOpen ? -1F : 1F))
                .start();

        if (valueListIsOpen) {
            binding.dataElementListGuideline.setVisibility(View.VISIBLE);
            binding.dataElementList.setVisibility(View.VISIBLE);
            for (Pair<String, String> nameValuePair : dataElementValues) {
                ItemFieldValueBinding fieldValueBinding = ItemFieldValueBinding.inflate(LayoutInflater.from(binding.dataElementList.getContext()));
                fieldValueBinding.setName(nameValuePair.component1());
                fieldValueBinding.setValue(nameValuePair.component2());
                binding.dataElementList.addView(fieldValueBinding.getRoot());
            }
        } else {
            binding.dataElementListGuideline.setVisibility(View.INVISIBLE);
            binding.dataElementList.setVisibility(View.GONE);
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            for (Pair<String, String> nameValuePair : dataElementValues) {
                if (nameValuePair.component2() != null && !Objects.equals(nameValuePair.component2(), "-")) {
                    SpannableString value = new SpannableString(nameValuePair.component2());
                    int colorToUse = dataElementValues.indexOf(nameValuePair) % 2 == 0 ?
                            ContextCompat.getColor(itemView.getContext(), R.color.textPrimary) : ContextCompat.getColor(itemView.getContext(), R.color.textSecondary);
                    value.setSpan(new ForegroundColorSpan(colorToUse), 0, value.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    stringBuilder.append(value);
                    if (dataElementValues.indexOf(nameValuePair) != dataElementValues.size() - 1) {
                        stringBuilder.append(" ");
                    }
                }
            }

            if (stringBuilder.toString().isEmpty()) {
                hideEventValueLayout();
            } else {
                binding.eventInfo.setText(stringBuilder);
            }
        }
    }

    public void showShadows(EventViewModel eventViewModel) {
        binding.shadowTop.setVisibility(eventViewModel.getGroupedByStage() && eventViewModel.getShowTopShadow() ? View.VISIBLE : View.GONE);
        binding.shadowBottom.setVisibility(eventViewModel.getGroupedByStage() && eventViewModel.getShowBottomShadow() ? View.VISIBLE : View.GONE);
    }
}
