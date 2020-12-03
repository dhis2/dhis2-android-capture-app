package org.dhis2.usescases.searchTrackEntity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.Bindings.ValueTypeExtensionsKt;
import org.dhis2.Components;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.forms.dataentry.fields.age.AgeViewModel;
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import org.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import org.dhis2.data.forms.dataentry.fields.file.FileViewModel;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.dhis2.utils.reporting.CrashReportController;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

import static org.dhis2.R.layout.custom_form_coordinate;
import static org.dhis2.R.layout.form_age_custom;
import static org.dhis2.R.layout.form_button;
import static org.dhis2.R.layout.form_date_text;
import static org.dhis2.R.layout.form_date_time_text;
import static org.dhis2.R.layout.form_edit_text_custom;
import static org.dhis2.R.layout.form_option_set_spinner;
import static org.dhis2.R.layout.form_org_unit;
import static org.dhis2.R.layout.form_scan;
import static org.dhis2.R.layout.form_time_text;
import static org.dhis2.R.layout.form_yes_no;

/**
 * QUADRAM. Created by ppajuelo on 06/11/2017.
 */

public class FormAdapter extends RecyclerView.Adapter<FormViewHolder> implements FormViewHolder.FieldItemCallback {

    private List<TrackedEntityAttribute> attributeList;
    private Program program;
    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;
    private SearchTEContractsModule.Presenter presenter;
    public CrashReportController crashReportController;

    private Context context;
    private HashMap<String, String> queryData;
    private List<ValueTypeDeviceRendering> renderingTypes;

    public FormAdapter(Context context, SearchTEContractsModule.Presenter presenter) {
        setHasStableIds(true);
        this.processor = PublishProcessor.create();
        this.processorOptionSet = PublishProcessor.create();
        this.context = context;
        this.presenter = presenter;

        attributeList = new ArrayList<>();
        crashReportController = ((Components) context.getApplicationContext()).appComponent().injectCrashReportController();
    }

    @NonNull
    @Override
    public FormViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, viewType, parent, false);
        return new FormViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FormViewHolder holder, int position) {
        FieldViewModel viewModel;
        TrackedEntityAttribute attr = attributeList.get(holder.getAdapterPosition());
        String label = attr.displayName();
        String hint = ValueTypeExtensionsKt.toHint(attr.valueType(), context);
        switch (holder.getItemViewType()) {
            case form_edit_text_custom:
                viewModel = EditTextViewModel.create(attr.uid(), label, false,
                        queryData.get(attr.uid()), hint, 1, attr.valueType(), null, true,
                        attr.displayDescription(), null, ObjectStyle.builder().build(), attr.fieldMask(), ValueTypeRenderingType.DEFAULT.toString(), false, true, processor);
                break;
            case form_button:
                viewModel = FileViewModel.create(attr.uid(), label, false, queryData.get(attr.uid()), null, attr.displayDescription(), ObjectStyle.builder().build(), processor);
                break;
            case form_yes_no:
                viewModel = RadioButtonViewModel.fromRawValue(attr.uid(), label, attr.valueType(), false, queryData.get(attr.uid()), null, true, attr.displayDescription(), ObjectStyle.builder().build(), ValueTypeRenderingType.DEFAULT, false, processor, true);
                break;
            case form_option_set_spinner:
                viewModel = SpinnerViewModel.create(attr.uid(), label, "", false, attr.optionSet().uid(), queryData.get(attr.uid()), null, true, attr.displayDescription(), 20, ObjectStyle.builder().build(), false, ValueTypeRenderingType.DEFAULT.toString(), processor);
                break;
            case custom_form_coordinate:
                viewModel = CoordinateViewModel.create(attr.uid(), label, false, queryData.get(attr.uid()), null, true, attr.displayDescription(), ObjectStyle.builder().build(), FeatureType.POINT, false, true, processor);
                break;
            case form_time_text:
            case form_date_text:
            case form_date_time_text:
                viewModel = DateTimeViewModel.create(attr.uid(), label, false, attr.valueType(), queryData.get(attr.uid()), null, true, true, attr.displayDescription(), ObjectStyle.builder().build(), false, true, processor);
                break;
            case form_age_custom:
                viewModel = AgeViewModel.create(attr.uid(), label, false, queryData.get(attr.uid()), null, true, attr.displayDescription(), ObjectStyle.builder().build(), false, true, processor);
                break;
            case form_org_unit:
                String value = presenter.nameOUByUid(queryData.get(attr.uid()));
                if (value != null)
                    value = queryData.get(attr.uid()) + "_ou_" + value;
                else
                    value = queryData.get(attr.uid());

                viewModel = OrgUnitViewModel.create(attr.uid(), label, false, value, null, true, attr.displayDescription(), ObjectStyle.builder().build(), false, ProgramStageSectionRenderingType.LISTING.name(), processor);
                break;
            case form_scan:
                viewModel = ScanTextViewModel.create(attr.uid(), label, false, queryData.get(attr.uid()), null, true, attr.optionSet() != null ? attr.optionSet().uid() : null, attr.description(), ObjectStyle.builder().build(), renderingTypes.get(position), hint, false, true, processor);
                break;
            default:
                D2Error error = D2Error.builder()
                        .errorDescription("Unsupported viewType source type: " + holder.getItemViewType())
                        .build();
                crashReportController.logException(error);
                viewModel = EditTextViewModel.create(attr.uid(), "UNSUPPORTED", false, null, "UNSUPPORTED", 1, attr.valueType(), null, false, attr.displayDescription(), null, ObjectStyle.builder().build(), attr.fieldMask(), null, false, true, processor);
                break;
        }
        holder.bind(viewModel, position, this);

    }

    @Override
    public int getItemCount() {
        return attributeList != null ? attributeList.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return attributeList.get(position).uid().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        if (attributeList.get(position).optionSet() != null)
            if (renderingTypes != null && !renderingTypes.isEmpty() && renderingTypes.get(position) != null &&
                    (renderingTypes.get(position).type() == ValueTypeRenderingType.BAR_CODE ||
                            (renderingTypes.get(position).type() == ValueTypeRenderingType.QR_CODE))) {
                return form_scan;
            } else {
                return form_option_set_spinner;
            }
        else {
            ValueType valueType = attributeList.get(position).valueType();
            switch (valueType) {
                case AGE:
                    return form_age_custom;
                case TEXT:
                case EMAIL:
                case LETTER:
                case NUMBER:
                case INTEGER:
                case USERNAME:
                case PERCENTAGE:
                case PHONE_NUMBER:
                case INTEGER_NEGATIVE:
                case INTEGER_POSITIVE:
                case INTEGER_ZERO_OR_POSITIVE:
                case UNIT_INTERVAL:
                case URL:
                case LONG_TEXT:
                    if (renderingTypes != null && !renderingTypes.isEmpty() && renderingTypes.get(position) != null &&
                            (renderingTypes.get(position).type() == ValueTypeRenderingType.BAR_CODE ||
                                    (renderingTypes.get(position).type() == ValueTypeRenderingType.QR_CODE))) {
                        return form_scan;
                    } else {
                        return form_edit_text_custom;
                    }
                case TIME:
                    return form_time_text;
                case DATE:
                    return form_date_text;
                case DATETIME:
                    return form_date_time_text;
                case FILE_RESOURCE:
                    return form_button;
                case COORDINATE:
                    return custom_form_coordinate;
                case BOOLEAN:
                case TRUE_ONLY:
                    return form_yes_no;
                case ORGANISATION_UNIT:
                    return form_org_unit;
                case TRACKER_ASSOCIATE:
                default:
                    return form_edit_text_custom;
            }
        }

    }

    public void setList(List<TrackedEntityAttribute> trackedEntityAttributes, Program program, HashMap<String, String> queryData, List<ValueTypeDeviceRendering> renderingTypes) {
        this.queryData = queryData;
        this.program = program;
        this.attributeList = trackedEntityAttributes;
        this.renderingTypes = renderingTypes;

        notifyDataSetChanged();

    }

    @NonNull
    public FlowableProcessor<RowAction> asFlowableRA() {
        return processor;
    }

    @Override
    public void onNext(int position) {
        if (position < getItemCount()) {
            //TODO needs to get the view model instead of the attribute list
            //getItem(position+1).onActive();
        }
    }

    @Override
    public void onShowDialog(String title, String message) {

    }
}
