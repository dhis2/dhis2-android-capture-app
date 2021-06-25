package org.dhis2.utils.customviews;

import android.content.Context;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;

import org.dhis2.App;
import org.dhis2.data.forms.dataentry.fields.spinner.OptionSetView;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.utils.optionset.OptionSetOptionsHandler;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.option.Option;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Pair;
import timber.log.Timber;


public class OptionSetPopUp extends PopupMenu {

    private final D2 d2;
    private final CompositeDisposable disposable;
    private HashMap<String, Option> optionsMap;
    private final OptionSetOptionsHandler optionSetOptionsHandler;

    public OptionSetPopUp(Context context, View anchor, SpinnerViewModel model,
                          OptionSetView optionSetView) {
        super(context, anchor);
        d2 = ((App) context.getApplicationContext()).serverComponent().userManager().getD2();
        optionSetOptionsHandler = new OptionSetOptionsHandler(
                model.getOptionsToHide(),
                model.getOptionGroupsToShow(),
                model.getOptionGroupsToHide());
        setOnDismissListener(menu -> dismiss());
        setOnMenuItemClickListener(item -> {
            dismiss();
            Option selectedOption = optionsMap.get(item.getTitle().toString());
            optionSetView.onSelectOption(selectedOption);
            return true;
        });
        disposable = new CompositeDisposable();

        disposable.add(
                Single.fromCallable(() -> d2.optionModule().options()
                        .byOptionSetUid().eq(model.optionSet()))
                        .map(optionRepository -> {
                            Pair<List<String>, List<String>> handlerOptionsResult = optionSetOptionsHandler.handleOptions();
                            List<String> finalOptionsToHide = handlerOptionsResult.component1();
                            List<String> finalOptionsToShow = handlerOptionsResult.component2();

                            if (!finalOptionsToShow.isEmpty())
                                optionRepository = optionRepository
                                        .byUid().in(finalOptionsToShow);

                            if (!finalOptionsToHide.isEmpty())
                                optionRepository = optionRepository
                                        .byUid().notIn(finalOptionsToHide);

                            return optionRepository.blockingGet();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::setOptions,
                                Timber::e
                        )
        );

    }

    public void setOptions(List<Option> options) {
        optionsMap = new HashMap<>();
        Collections.sort(options, (option1, option2) -> option1.sortOrder().compareTo(option2.sortOrder()));
        for (Option option : options) {
            optionsMap.put(option.displayName(), option);
            getMenu().add(Menu.NONE, Menu.NONE, options.indexOf(option) + 1, option.displayName());
        }
        show();
    }

    @Override
    public void dismiss() {
        disposable.clear();
        super.dismiss();
    }
}