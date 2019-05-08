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
    private Integer openFuturePeriods;

    public PeriodDialogInputPeriod setInputPeriod(List<DateRangeInputPeriodModel> inputPeriod) {
        this.inputPeriod = inputPeriod;
        return this;
    }

    public PeriodDialogInputPeriod setOpenFuturePeriods(Integer openFuturePeriods){
        this.openFuturePeriods = openFuturePeriods;
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
        setDateWithOpenFuturePeriod(openFuturePeriods);
        binding.periodSubtitle.setText(getPeriod().name());
        boolean isAllowed = false;

        for (DateRangeInputPeriodModel inputPeriodModel : inputPeriod) {
            do{
                if (getCurrentDate().after(inputPeriodModel.initialPeriodDate()) && getCurrentDate().before(inputPeriodModel.endPeriodDate())
                        && (inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                        && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate()))))
                    isAllowed = true;
                else if(getCurrentDate().before(inputPeriod.get(inputPeriod.size()-1).initialPeriodDate()) ||
                        getCurrentDate().before(inputPeriodModel.initialPeriodDate()))
                    break;
                else
                    previousPeriod();
            }while (!isAllowed );
            if(isAllowed) break;
        }

        if(!isAllowed && !inputPeriod.isEmpty()) {
            binding.selectedPeriod.setText(getString(R.string.there_is_no_available_period));
            binding.acceptButton.setVisibility(View.GONE);
        }else {
            binding.selectedPeriod.setText(DateUtils.getInstance().getPeriodUIString(getPeriod(), getCurrentDate(), Locale.getDefault()));

            binding.periodBefore.setOnClickListener(view -> {

                boolean isPreviousAllowed = false;
                Date currentDate =  getCurrentDate();
                previousPeriod();
                for (DateRangeInputPeriodModel inputPeriodModel : inputPeriod) {
                    do {
                        if (getCurrentDate().after(inputPeriodModel.initialPeriodDate()) && getCurrentDate().before(inputPeriodModel.endPeriodDate())
                                && (inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                                && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate()))))
                            isPreviousAllowed = true;
                        else if (getCurrentDate().before(inputPeriodModel.initialPeriodDate()))
                            break;
                        else
                            previousPeriod();
                    } while (!isPreviousAllowed);
                    if (isPreviousAllowed)
                        break;
                }

                checkConstraintDates();

                if (inputPeriod.size() != 0 && !isPreviousAllowed) {
                    setRealCurrentDate(currentDate);
                    binding.selectedPeriod.setText(DateUtils.getInstance().getPeriodUIString(getPeriod(), currentDate, Locale.getDefault()));
                    binding.periodBefore.setEnabled(false);
                }
            });
            binding.periodNext.setOnClickListener(view -> {

                boolean isNextAllowed = false;
                Date currentDate =  getCurrentDate();
                nextPeriod();

                for (DateRangeInputPeriodModel inputPeriodModel : inputPeriod) {
                    do {
                        if (getCurrentDate().after(inputPeriodModel.initialPeriodDate()) && getCurrentDate().before(inputPeriodModel.endPeriodDate())
                                && (inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                                && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate()))))
                            isNextAllowed = true;
                        else if (getCurrentDate().after(inputPeriodModel.endPeriodDate()))
                            break;
                        else
                            nextPeriod();
                    } while (!isNextAllowed);
                    if (isNextAllowed)
                        break;
                }
                checkConstraintDates();
                if (inputPeriod.size() != 0 && !isNextAllowed) {
                    setRealCurrentDate(currentDate);
                    binding.selectedPeriod.setText(DateUtils.getInstance().getPeriodUIString(getPeriod(), currentDate, Locale.getDefault()));
                    binding.periodNext.setEnabled(false);
                }
            });
        }

        checkConstraintDates();

        return binding.getRoot();
    }

}
