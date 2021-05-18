package org.dhis2.data.forms.dataentry.fields.coordinate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.utils.customviews.FieldLayout;

public class CoordinatesView extends FieldLayout {

    private ViewDataBinding binding;
    private CoordinateViewModel viewModel;

    public CoordinatesView(Context context) {
        super(context);
        if (!isInEditMode())
            init(context);
        else
            initEditor();
    }

    public CoordinatesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            init(context);
        else
            initEditor();
    }

    public CoordinatesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode())
            init(context);
        else
            initEditor();
    }

    public void initEditor() {
        LayoutInflater.from(getContext()).inflate(R.layout.form_coordinates, this, true);
    }

    public void init(Context context) {
        super.init(context);
    }

    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.form_coordinates, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.form_coordinates_accent, this, true);
    }

    public String currentCoordinates() {
        return viewModel.currentGeometry() != null ? viewModel.currentGeometry().coordinates() : null;
    }

    public void setViewModel(CoordinateViewModel viewModel) {
        this.viewModel = viewModel;

        if (binding == null) {
            this.isBgTransparent = viewModel.isBackgroundTransparent();
            setLayout();
        }
        binding.setVariable(BR.item, viewModel);
    }
}

