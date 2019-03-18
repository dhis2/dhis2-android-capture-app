package org.dhis2.data.forms.dataentry.tablefields.spinner;

import androidx.databinding.ViewDataBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomTextViewCellBinding;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 07/11/2017.
 */

public class SpinnerHolder extends FormViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private final CompositeDisposable disposable;
    private final FlowableProcessor<RowAction> processor;
    private final EditText editText;


    private SpinnerViewModel viewModel;
    List<OptionModel> options;

    SpinnerHolder(CustomTextViewCellBinding mBinding, FlowableProcessor<RowAction> processor) {
        super(mBinding);
        this.editText = mBinding.inputEditText;
        this.processor = processor;

        editText.setOnClickListener(this);

        this.disposable = new CompositeDisposable();

    }

    public void update(SpinnerViewModel viewModel, boolean accessDataWrite) {

        this.viewModel = viewModel;
        options = Bindings.setOptionSet(viewModel.optionSet());

        if (!viewModel.editable()) {
            editText.setEnabled(false);
            editText.setBackgroundColor(ContextCompat.getColor(editText.getContext(), R.color.bg_black_e6e));
        } else if(accessDataWrite) {
            editText.setEnabled(true);
            editText.setBackgroundColor(ContextCompat.getColor(editText.getContext(), R.color.white));
        }else{
            editText.setEnabled(false);
        }
        editText.setFocusable(false);
        editText.setClickable(viewModel.editable());


        editText.setText(viewModel.value()); //option code is already transformed to value in the fieldviewmodelfactory implementation

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
            if(value.equals(optionModel.displayName())) {
                code = optionModel.code();
                editText.setText(code);
            }
        processor.onNext(
                RowAction.create(viewModel.uid(), code, viewModel.dataElement(), viewModel.listCategoryOption(), viewModel.catCombo(), viewModel.row(), viewModel.column())
        );
        return false;
    }

    @Override
    public void onClick(View v) {
        PopupMenu menu = new PopupMenu(itemView.getContext(), v);
        menu.setOnMenuItemClickListener(this);
//        menu.getMenu().add(Menu.NONE, Menu.NONE, 0, viewModel.label()); Don't show label
        for (OptionModel optionModel : options)
            menu.getMenu().add(Menu.NONE, Menu.NONE, options.indexOf(optionModel) + 1, optionModel.displayName());
        menu.show();
    }
}
