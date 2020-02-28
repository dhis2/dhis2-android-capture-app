package org.dhis2.data.forms.dataentry.tablefields.edittext;

import android.annotation.SuppressLint;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.handler.SelectionHandler;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomTextViewCellBinding;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.customviews.TableFieldDialog;
import org.hisp.dhis.android.core.common.ValueType;

import io.reactivex.processors.FlowableProcessor;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018..
 */

final class EditTextCellCustomHolder extends FormViewHolder {

    private EditText editText;
    private EditTextModel editTextModel;
    private boolean accessDataWrite;
    private CustomTextViewCellBinding customBinding;

    private TableView tableView;
    FlowableProcessor<RowAction> processor;

    @SuppressLint("RxLeakedSubscription")
    EditTextCellCustomHolder(CustomTextViewCellBinding binding, FlowableProcessor<RowAction> processor,
                             ObservableBoolean isEditable, TableView tableView) {
        super(binding);
        editText = binding.inputEditText;
        accessDataWrite = isEditable.get();
        customBinding = binding;
        this.tableView = tableView;
        this.processor = processor;

        editText.setOnEditorActionListener((v, actionId, event) -> {
            selectNext();
            return true;
        });

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus  && editTextModel != null && editTextModel.editable() &&
                    !editText.getText().toString().equals(editTextModel.value()) && validate()) {
                processor.onNext(
                        RowAction.create(
                                editTextModel.uid(),
                                editText.getText().toString(),
                                editTextModel.dataElement(),
                                editTextModel.categoryOptionCombo(),
                                editTextModel.catCombo(),
                                editTextModel.row(),
                                editTextModel.column()
                        )
                );
            }
        });
    }


    public void update(@NonNull FieldViewModel model) {

        this.editTextModel = (EditTextModel) model;
        setInputType(editTextModel.valueType());

        customBinding.inputEditText.setText(model.value());

        if (editTextModel.mandatory())
            customBinding.icMandatory.setVisibility(View.VISIBLE);
        else
            customBinding.icMandatory.setVisibility(View.INVISIBLE);

        if (editTextModel.editable()) {
            if (accessDataWrite) {
                customBinding.inputEditText.setEnabled(true);
            } else {
                customBinding.inputEditText.setEnabled(false);
            }
        } else {
            customBinding.inputEditText.setEnabled(false);
            if (editTextModel.dataElement().isEmpty())
                customBinding.inputEditText.setActivated(true);
        }

        customBinding.executePendingBindings();

        if(tableView.getSelectedRow() == SelectionHandler.UNSELECTED_POSITION){
            closeKeyboard(editText);
            editText.clearFocus();
        } else if(editTextModel.column() == tableView.getSelectedColumn() && editTextModel.row() == tableView.getSelectedRow())
            setSelected(SelectionState.SELECTED);
    }

    private void setInputType(ValueType valueType) {

        editText.setFilters(new InputFilter[]{});


        if (editTextModel.editable())
            switch (valueType) {
                case PHONE_NUMBER:
                    editText.setInputType(InputType.TYPE_CLASS_PHONE);
                    break;
                case EMAIL:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    break;
                case TEXT:
                case LONG_TEXT:
                    editText.setKeyListener(null);
                    editText.setFocusable(false);
                    editText.setOnClickListener(v -> {
                        showEditDialog();
                    });
                    break;
                case LETTER:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    editText.setFilters(new InputFilter[]{
                            new InputFilter.LengthFilter(1),
                            (source, start, end, dest, dstart, dend) -> {
                                if (source.equals(""))
                                    return source;
                                if (source.toString().matches("[a-zA-Z]"))
                                    return source;
                                return "";
                            }});
                    break;
                case NUMBER:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                            InputType.TYPE_NUMBER_FLAG_DECIMAL |
                            InputType.TYPE_NUMBER_FLAG_SIGNED);
                    break;
                case INTEGER_NEGATIVE:
                case INTEGER:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    break;
                case INTEGER_ZERO_OR_POSITIVE:
                case INTEGER_POSITIVE:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setKeyListener(DigitsKeyListener.getInstance(false, false));
                    break;
                case UNIT_INTERVAL:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    break;
                case PERCENTAGE:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                case URL:
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
                    break;
                default:
                    break;
            }
        else {
            editText.setInputType(0);
        }
    }

    private void showEditDialog() {

        View msgView = LayoutInflater.from(editText.getContext()).inflate(R.layout.dialog_edittext, null);
        EditText editDialog = msgView.findViewById(R.id.dialogBody);
        editDialog.setText(editText.getText().toString());
        editDialog.setSelection(editDialog.getText() == null ? 0 : editDialog.getText().length());

        new TableFieldDialog(
                editText.getContext(),
                editTextModel.label(),
                editTextModel.description(),
                msgView,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        if (editTextModel != null && editTextModel.editable() && !editDialog.getText().toString().equals(editTextModel.value()) && validate()) {
                            processor.onNext(RowAction.create(editTextModel.uid(), editDialog.getText().toString(),
                                    editTextModel.dataElement(), editTextModel.categoryOptionCombo(),
                                    editTextModel.catCombo(), editTextModel.row(), editTextModel.column()));
                        }
                    }

                    @Override
                    public void onNegative() {
                    }
                },
                v -> editDialog.setText(null)
        ).show();
    }


    private boolean validate() {
        if (!editText.getText().toString().isEmpty()) {
            switch (editTextModel.valueType()) {
                case PHONE_NUMBER:
                    if (Patterns.PHONE.matcher(editText.getText().toString()).matches())
                        return true;
                    else {
                        editText.setError(editText.getContext().getString(R.string.invalid_phone_number));
                        return false;
                    }
                case EMAIL:
                    if (Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches())
                        return true;
                    else {
                        editText.setError(editText.getContext().getString(R.string.invalid_email));
                        return false;
                    }
                case INTEGER_NEGATIVE:
                    if (Integer.valueOf(editText.getText().toString()) < 0)
                        return true;
                    else {
                        editText.setError(editText.getContext().getString(R.string.invalid_negative_number));
                        return false;
                    }
                case INTEGER_ZERO_OR_POSITIVE:
                    if (Integer.valueOf(editText.getText().toString()) >= 0)
                        return true;
                    else {
                        editText.setError(editText.getContext().getString(R.string.invalid_possitive_zero));
                        return false;
                    }
                case INTEGER_POSITIVE:
                    if (Integer.valueOf(editText.getText().toString()) > 0)
                        return true;
                    else {
                        editText.setError(editText.getContext().getString(R.string.invalid_possitive));
                        return false;
                    }
                case UNIT_INTERVAL:
                    if (Float.valueOf(editText.getText().toString()) >= 0 && Float.valueOf(editText.getText().toString()) <= 1)
                        return true;
                    else {
                        editText.setError(editText.getContext().getString(R.string.invalid_interval));
                        return false;
                    }
                case PERCENTAGE:
                    if (Float.valueOf(editText.getText().toString()) >= 0 && Float.valueOf(editText.getText().toString()) <= 100)
                        return true;
                    else {
                        editText.setError(editText.getContext().getString(R.string.invalid_percentage));
                        return false;
                    }
                default:
                    return true;
            }
        } else
            return true;
    }

    @Override
    public void dispose() {
    }

    public void selectNext() {
        if (tableView.getColumnHeaderRecyclerView().get(tableView.getColumnHeaderRecyclerView().size() - 1).getAdapter().getItemCount() > tableView.getSelectedColumn() + 1) {
            tableView.setSelectedCell(tableView.getSelectedColumn() + 1, tableView.getSelectedRow());
        } else if (tableView.getRowHeaderRecyclerView().getAdapter().getItemCount() > tableView.getSelectedRow() + 1) {
            tableView.setSelectedCell(0, tableView.getSelectedRow() + 1);
        } else {
            setSelected(SelectionState.UNSELECTED);
            tableView.getSelectionHandler().clearSelection();
            editText.clearFocus();
            closeKeyboard(editText);
        }
    }

    @Override
    public void setSelected(SelectionState selectionState) {
        super.setSelected(selectionState);
        if (selectionState == SelectionState.SELECTED && editTextModel.editable()) {
            editText.requestFocus();
            editText.setSelection(editText.getText().length());
            openKeyboard(editText);
        }
    }
}
