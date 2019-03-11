package org.dhis2.utils.custom_views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.usescases.datasets.datasetInitial.DateRangeInputPeriodModel;
import org.dhis2.utils.DateUtils;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

public class PeriodDialogInputPeriod extends PeriodDialog {

    private List<DateRangeInputPeriodModel> inputPeriod;
    private boolean isNext = false;
    private boolean isPrevious = false;

    public PeriodDialogInputPeriod setInputPeriod(List<DateRangeInputPeriodModel> inputPeriod) {
        this.inputPeriod = inputPeriod;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_period, container, false);

        binding.title.setText(getTitle());
        binding.acceptButton.setOnClickListener(view -> {

            getPossitiveListener().onDateSet(getCurrentDate());
            dismiss();
        });
        binding.clearButton.setOnClickListener(view -> dismiss());

        binding.periodSubtitle.setText(getPeriod().name());
        boolean isAllowed = false;

        for (DateRangeInputPeriodModel inputPeriodModel : inputPeriod) {
            do{
                if (getCurrentDate().after(inputPeriodModel.initialPeriodDate()) && getCurrentDate().before(inputPeriodModel.endPeriodDate())
                        && (inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                        && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate()))))
                    isAllowed = true;
                else
                    previousPeriod();
            }while (!isAllowed);
            setCurrentDate(getCurrentDate());
            if(isAllowed) break;
        }

        checkNextPeriod();

        binding.selectedPeriod.setText(DateUtils.getInstance().getPeriodUIString(getPeriod(),getCurrentDate(), Locale.getDefault()));

        binding.periodBefore.setOnClickListener(view -> {
            Date current = getCurrentDate();

            boolean isPreviousAllowed = false;
            previousPeriod();
            for (DateRangeInputPeriodModel inputPeriodModel : inputPeriod) {
                do{
                    if (getCurrentDate().after(inputPeriodModel.initialPeriodDate()) && getCurrentDate().before(inputPeriodModel.endPeriodDate())
                            && (inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                            && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate()))))
                        isPreviousAllowed = true;
                    else if(getCurrentDate().before(inputPeriodModel.initialPeriodDate()))
                        break;
                    else
                        previousPeriod();
                }while (!isPreviousAllowed);
                setCurrentDate(current);
                if(isPreviousAllowed)
                    break;
            }

            checkConstraintDates();

            if(inputPeriod.size() != 0 && !isPreviousAllowed) {
                setCurrentDate(current);
                binding.selectedPeriod.setText(DateUtils.getInstance().getPeriodUIString(getPeriod(), current, Locale.getDefault()));
                binding.periodBefore.setEnabled(false);
            }
        });
        binding.periodNext.setOnClickListener(view -> {
            Date current = getCurrentDate();

            boolean isNextAllowed = false;
            nextPeriod();

            for (DateRangeInputPeriodModel inputPeriodModel : inputPeriod) {
                do{
                    if (getCurrentDate().after(inputPeriodModel.initialPeriodDate()) && getCurrentDate().before(inputPeriodModel.endPeriodDate())
                            && (inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                            && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate()))))
                        isNextAllowed = true;
                    else if(getCurrentDate().after(inputPeriodModel.endPeriodDate()))
                        break;
                    else
                        nextPeriod();
                }while (!isNextAllowed);
                setCurrentDate(current);
                if(isNextAllowed)
                    break;
            }
            checkConstraintDates();
            if(inputPeriod.size() != 0 && !isNextAllowed) {
                setCurrentDate(current);
                binding.selectedPeriod.setText(DateUtils.getInstance().getPeriodUIString(getPeriod(), current, Locale.getDefault()));
                binding.periodNext.setEnabled(false);
            }
        });

        return binding.getRoot();
    }

    private void checkNextPeriod(){
        if (getMinDate() == null || getCurrentDate().after(getMinDate()))
            setCurrentDate(DateUtils.getInstance().getNextPeriod(getPeriod(), getCurrentDate(), 0));
        else if (getMinDate() != null && getCurrentDate().before(getMinDate()))
            setCurrentDate(DateUtils.getInstance().getNextPeriod(getPeriod(), getMinDate(), 0));
        else
            setCurrentDate(DateUtils.getInstance().getNextPeriod(getPeriod(), getCurrentDate(), 0));
    }
}
