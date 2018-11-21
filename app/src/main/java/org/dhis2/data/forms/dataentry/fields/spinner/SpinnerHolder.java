package org.dhis2.data.forms.dataentry.fields.spinner;

import android.databinding.ViewDataBinding;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.CustomViews.OptionSetDialog;
import org.dhis2.utils.CustomViews.OptionSetOnClickListener;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 07/11/2017.
 */

public class SpinnerHolder extends FormViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, OptionSetOnClickListener {

    private final CompositeDisposable disposable;
    private final FlowableProcessor<RowAction> processor;
    private final FlowableProcessor<Pair<String,String>> processorOptionSet;
    private final ImageView iconView;
    private final TextInputEditText editText;
    private final TextInputLayout inputLayout;
    private final ViewDataBinding binding;
    /* @NonNull
     private BehaviorProcessor<SpinnerViewModel> model;*/
    private SpinnerViewModel viewModel;
    List<OptionModel> options;
    private OptionSetDialog dialog;

    SpinnerHolder(ViewDataBinding mBinding, FlowableProcessor<RowAction> processor,FlowableProcessor<Pair<String,String>> processorOptionSet, boolean isBackgroundTransparent, String renderType) {
        super(mBinding);
        binding = mBinding;
        this.editText = mBinding.getRoot().findViewById(R.id.input_editText);
        this.iconView = mBinding.getRoot().findViewById(R.id.renderImage);
        this.inputLayout = mBinding.getRoot().findViewById(R.id.input_layout);
        this.processor = processor;
        this.processorOptionSet = processorOptionSet;

        if (renderType != null && !renderType.equals(ProgramStageSectionRenderingType.LISTING.name()))
            iconView.setVisibility(View.VISIBLE);

        editText.setOnClickListener(this);

        this.disposable = new CompositeDisposable();

    }

    public void update(SpinnerViewModel viewModel) {

        this.viewModel = viewModel;
        options = Bindings.setOptionSet(viewModel.optionSet());

        Bindings.setObjectStyle(iconView, itemView, viewModel.uid());
        editText.setEnabled(viewModel.editable());
        editText.setFocusable(false);
        editText.setClickable(viewModel.editable());


        editText.setText(viewModel.value()); //option code is already transformed to value in the fieldviewmodelfactory implementation


        if (!isEmpty(viewModel.warning())) {
            inputLayout.setError(viewModel.warning());
        } else if (!isEmpty(viewModel.error())) {
            inputLayout.setError(viewModel.error());
        } else
            inputLayout.setError(null);

        if (inputLayout.getHint() == null || !inputLayout.getHint().toString().equals(viewModel.label())) {
            label = new StringBuilder(viewModel.label());
            if (viewModel.mandatory())
                label.append("*");
            inputLayout.setHint(label);
        }

        descriptionText = viewModel.description();
    }

    public void dispose() {
        disposable.clear();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        String value = item.getTitle().toString();
        String code = null;
        for (OptionModel optionModel : options)
            if (value.equals(optionModel.displayName()))
                code = optionModel.code();
        processor.onNext(
                RowAction.create(viewModel.uid(), code)
        );
        return false;
    }

    @Override
    public void onClick(View v) {
        closeKeyboard(v);
        if(options.size() > 15){
            OptionSetDialog dialog = OptionSetDialog.newInstance();
            dialog.setProcessor(processorOptionSet).setOnClick(this)
                    .setCancelListener(view -> dialog.dismiss())
                    .setClearListener(view -> {processor.onNext(
                            RowAction.create(viewModel.uid(), ""));
                            dialog.dismiss();
                    }
                    ).show(((FragmentActivity)binding.getRoot().getContext()).getSupportFragmentManager(), null);
            dialog.setOptionSetUid(viewModel);
        }else {
            PopupMenu menu = new PopupMenu(itemView.getContext(), v);
            menu.setOnMenuItemClickListener(this);
            for (OptionModel optionModel : options)
                menu.getMenu().add(Menu.NONE, Menu.NONE, options.indexOf(optionModel) + 1, optionModel.displayName());
            menu.show();
        }
    }

    @Override
    public void onSelectOption(String option) {
        String code = "";
        for (OptionModel optionModel : options)
            if (option.equals(optionModel.displayName()))
                code = optionModel.code();
        processor.onNext(
                RowAction.create(viewModel.uid(), code)
        );

        OptionSetDialog.newInstance().dismiss();
    }
}
