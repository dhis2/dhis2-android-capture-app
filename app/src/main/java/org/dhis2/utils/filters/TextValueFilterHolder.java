package org.dhis2.utils.filters;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemFilterValueBinding;
import org.dhis2.utils.DataElementsAdapter;
import org.hisp.dhis.android.core.dataelement.DataElement;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

class TextValueFilterHolder extends FilterHolder {

    private final List<DataElement> textDataElements;

    Pair<String,String> textValueFilter = Pair.create("","");


    TextValueFilterHolder(@NonNull ItemFilterValueBinding binding,
            ObservableField<Filters> openedFilter,
            List<DataElement> textDataElements) {
        super(binding, openedFilter);
        filterType = Filters.TEXT_VALUE;
        this.textDataElements = textDataElements;
    }

    @Override
    public void bind() {
        super.bind();
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_form_text));
        filterTitle.setText(R.string.filters_title_value);

        ItemFilterValueBinding localBinding = (ItemFilterValueBinding) binding;

        DataElementsAdapter dataElementsAdapter = new DataElementsAdapter(itemView.getContext(),
                R.layout.spinner_layout,
                R.id.spinner_text,
                textDataElements,
                R.color.white_faf);

        localBinding.filterValue.dataElementsSpinner.setAdapter(dataElementsAdapter);
        localBinding.filterValue.dataElementsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                DataElement dataelement = textDataElements.get(position);
                textValueFilter = Pair.create(dataelement.uid(),textValueFilter.val1());
                FilterManager.getInstance().setTexValueFilter(textValueFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("","");
            }
        });

        localBinding.filterValue.valueEditText.setText(FilterManager.getInstance().getTexValueFilter().val1());
        localBinding.filterValue.valueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textValueFilter = Pair.create(textValueFilter.val0(),s.toString());

                if (!textValueFilter.val0().isEmpty()){
                    FilterManager.getInstance().setTexValueFilter(textValueFilter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
