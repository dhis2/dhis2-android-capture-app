package org.dhis2.data.forms.dataentry.fields.coordinate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.App;
import org.dhis2.BR;
import org.dhis2.Bindings.DoubleExtensionsKt;
import org.dhis2.R;
import org.dhis2.uicomponents.map.views.MapSelectorActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.ActivityResultObservable;
import org.dhis2.utils.ActivityResultObserver;
import org.dhis2.utils.Constants;
import org.dhis2.utils.customviews.FieldLayout;
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

import kotlin.Unit;

import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter.ACCESS_LOCATION_PERMISSION_REQUEST;
import static org.dhis2.utils.Constants.RQ_MAP_LOCATION_VIEW;

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

    @SuppressLint("MissingPermission")
    public void updateLocation(Geometry geometry) {
        viewModel.onCurrentLocationClick(geometry);
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

