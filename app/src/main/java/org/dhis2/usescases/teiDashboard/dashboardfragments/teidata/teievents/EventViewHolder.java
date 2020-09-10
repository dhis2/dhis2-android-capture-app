package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.databinding.ItemEventBinding;
import org.dhis2.databinding.ItemFieldValueBinding;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DhisTextUtils;
import org.dhis2.utils.resources.ResourceManager;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

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

    public EventViewHolder(ItemEventBinding binding,
                    Program program,
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
    }

    public void bind(EventViewModel eventModel, Enrollment enrollment, @NotNull Function0<Unit> toggleList) {
        ProgramStage programStage = eventModel.getStage();
        Event event = eventModel.getEvent();
        binding.setEvent(eventModel.getEvent());
        binding.setStage(eventModel.getStage());
        binding.setEnrollment(enrollment);
        binding.setProgram(program);
        binding.executePendingBindings();

        if (eventModel.getGroupedByStage()) {
            binding.programStageName.setVisibility(View.GONE);
            binding.stageIconImage.setVisibility(View.GONE);
            binding.stageIconStatusImage.setVisibility(View.GONE);
            binding.eventStatus.setVisibility(View.VISIBLE);
        } else {
            binding.programStageName.setVisibility(View.VISIBLE);
            binding.stageIconImage.setVisibility(View.VISIBLE);
            binding.stageIconStatusImage.setVisibility(View.VISIBLE);
            binding.eventStatus.setVisibility(View.GONE);
            renderStageIcon(programStage.style());
        }

        String date = DateUtils.getInstance().getPeriodUIString(programStage.periodType(), event.eventDate() != null ? event.eventDate() : event.dueDate(), Locale.getDefault());
        binding.eventDate.setText(date);

        binding.organisationUnit.setText(eventModel.getOrgUnitName());
        if (DhisTextUtils.Companion.isNotEmpty(eventModel.getCatComboName()) && !eventModel.getCatComboName().equals("default")) {
            binding.catCombo.setVisibility(View.VISIBLE);
            binding.catCombo.setText(eventModel.getCatComboName());
        } else {
            binding.catCombo.setVisibility(View.GONE);
        }

        if (eventModel.getDataElementValues() != null && !eventModel.getDataElementValues().isEmpty()) {
            binding.showValuesButton.setVisibility(View.VISIBLE);
            binding.showValuesButton.setOnClickListener(view -> toggleList.invoke());
            initValues(eventModel.getValueListIsOpen(), eventModel.getDataElementValues());
        } else {
            binding.showValuesButton.setVisibility(View.GONE);
            binding.dataElementListGuideline.setVisibility(View.GONE);
            binding.dataElementList.setVisibility(View.GONE);
            binding.showValuesButton.setOnClickListener(null);
        }

        binding.syncIcon.setOnClickListener(view -> onSyncClick.invoke(event.uid()));

        itemView.setOnClickListener(view -> {
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
                    onEventSelected.invoke(event.uid(),event.organisationUnit(), event.status(), binding.sharedView);
                    break;
            }
        });
    }

    private void renderStageIcon(ObjectStyle style) {
        int color = ColorUtils.getColorFrom(
                style.color(),
                ColorUtils.getPrimaryColor(binding.stageIconImage.getContext(), ColorUtils.ColorType.PRIMARY)
        );

        binding.stageIconImage.setBackground(
                ColorUtils.tintDrawableWithColor(
                        binding.stageIconImage.getBackground(),
                        color
                ));

        binding.stageIconImage.setImageResource(
                new ResourceManager(itemView.getContext()).getObjectStyleDrawableResource(
                        style.icon(),
                        R.drawable.ic_program_default
                ));
        binding.stageIconImage.setColorFilter(ColorUtils.getContrastColor(color));
    }

    private void initValues(boolean valueListIsOpen, List<Pair<String, String>> dataElementValues) {
        binding.dataElementList.removeAllViews();
        binding.eventInfo.setText(null);
        binding.showValuesButton.setScaleY(valueListIsOpen ? -1F : 1F);
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
            binding.dataElementListGuideline.setVisibility(View.GONE);
            binding.dataElementList.setVisibility(View.GONE);
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            for (Pair<String, String> nameValuePair : dataElementValues) {
                SpannableString value = new SpannableString(nameValuePair.component2());
                int colorToUse = dataElementValues.indexOf(nameValuePair) % 2 == 0 ? Color.parseColor("#8A333333") : Color.parseColor("#61333333");
                value.setSpan(new ForegroundColorSpan(colorToUse), 0, value.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                stringBuilder.append(value);
                if (dataElementValues.indexOf(nameValuePair) != dataElementValues.size() - 1) {
                    stringBuilder.append(" ");
                }
            }
            binding.eventInfo.setText(stringBuilder);
        }
    }
}
