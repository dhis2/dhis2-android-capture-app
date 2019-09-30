package org.dhis2.usescases.searchTrackEntity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.forms.dataentry.fields.age.AgeRow;
import org.dhis2.data.forms.dataentry.fields.age.AgeViewModel;
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateRow;
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import org.dhis2.data.forms.dataentry.fields.datetime.DateTimeRow;
import org.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextRow;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import org.dhis2.data.forms.dataentry.fields.file.FileRow;
import org.dhis2.data.forms.dataentry.fields.file.FileViewModel;
import org.dhis2.data.forms.dataentry.fields.image.ImageRow;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitRow;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonRow;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerRow;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedRow;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 06/11/2017.
 */

public class FormAdapter extends RecyclerView.Adapter {

    private long ENROLLMENT_DATE_ID = 1;
    private long INCIDENT_DATE_ID = 2;

    private final int EDITTEXT = 0;
    private final int BUTTON = 1;
    private final int CHECKBOX = 2;
    private final int SPINNER = 3;
    private final int COORDINATES = 4;
    private final int TIME = 5;
    private final int DATE = 6;
    private final int DATETIME = 7;
    private final int AGEVIEW = 8;
    private final int YES_NO = 9;
    private final int ORG_UNIT = 10;
    private static final int IMAGE = 11;
    private static final int UNSUPPORTED = 12;
    private static final int LONG_TEXT = 13;
    private int programData = 0;
    private List<TrackedEntityAttributeModel> attributeList;
    private ProgramModel programModel;
    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;

    @NonNull
    private final List<Row> rows;

    private Context context;
    private HashMap<String, String> queryData;

    public FormAdapter(FragmentManager fm, Context context) {
        setHasStableIds(true);
        this.processor = PublishProcessor.create();
        this.processorOptionSet = PublishProcessor.create();
        this.context = context;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        attributeList = new ArrayList<>();
        rows = new ArrayList<>();

        rows.add(EDITTEXT, new EditTextRow(layoutInflater, processor, false, false));
        rows.add(BUTTON, new FileRow(layoutInflater, processor, false));
        rows.add(CHECKBOX, new RadioButtonRow(layoutInflater, processor, false));
        rows.add(SPINNER, new SpinnerRow(layoutInflater, processor, processorOptionSet, false));
        rows.add(COORDINATES, new CoordinateRow(layoutInflater, processor, false));
        rows.add(TIME, new DateTimeRow(layoutInflater, processor, TIME, false));
        rows.add(DATE, new DateTimeRow(layoutInflater, processor, DATE, false));
        rows.add(DATETIME, new DateTimeRow(layoutInflater, processor, DATETIME, false));
        rows.add(AGEVIEW, new AgeRow(layoutInflater, processor, false));
        rows.add(YES_NO, new RadioButtonRow(layoutInflater, processor, false));
        rows.add(ORG_UNIT, new OrgUnitRow(fm, layoutInflater, processor, false));
        rows.add(IMAGE, new ImageRow(layoutInflater, processor, null));
        rows.add(UNSUPPORTED, new UnsupportedRow(layoutInflater));
        rows.add(LONG_TEXT, new EditTextRow(layoutInflater, processor, false, true));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return rows.get(viewType).onCreate(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FieldViewModel viewModel;
        if (position < programData) {
            viewModel = DateTimeViewModel.create(
                    position == 0 ? Constants.ENROLLMENT_DATE_UID : Constants.INCIDENT_DATE_UID,
                    holder.getAdapterPosition() == 0 ?
                            !isEmpty(programModel.enrollmentDateLabel()) ? programModel.enrollmentDateLabel() : context.getString(R.string.enrollmment_date) :
                            !isEmpty(programModel.incidentDateLabel()) ? programModel.incidentDateLabel() : context.getString(R.string.incident_date),
                    false,
                    ValueType.DATE,
                    null,
                    null,
                    holder.getAdapterPosition() == 0 ? programModel.selectEnrollmentDatesInFuture() : programModel.selectIncidentDatesInFuture(), true, null,
                    ObjectStyleModel.builder().build());

        } else {
            TrackedEntityAttributeModel attr = attributeList.get(holder.getAdapterPosition() - programData);
            //String label = attr.displayShortName() != null ? attr.displayShortName() : attr.displayName();
            String label = attr.displayName();
            switch (holder.getItemViewType()) {
                case EDITTEXT:
                    viewModel = EditTextViewModel.create(attr.uid(), label, false,
                            queryData.get(attr.uid()), label, 1, attr.valueType(), null, true,
                            attr.displayDescription(), null, ObjectStyleModel.builder().build());
                    break;
                case BUTTON:
                    viewModel = FileViewModel.create(attr.uid(), label, false, queryData.get(attr.uid()), null, attr.displayDescription(), ObjectStyleModel.builder().build());
                    break;
                case CHECKBOX:
                case YES_NO:
                    viewModel = RadioButtonViewModel.fromRawValue(attr.uid(), label, attr.valueType(), false, queryData.get(attr.uid()), null, true, attr.displayDescription(), ObjectStyleModel.builder().build());
                    break;
                case SPINNER:
                    viewModel = SpinnerViewModel.create(attr.uid(), label, "", false, attr.optionSet(), queryData.get(attr.uid()), null, true, attr.displayDescription(), 20, ObjectStyleModel.builder().build());
                    break;
                case COORDINATES:
                    viewModel = CoordinateViewModel.create(attr.uid(), label, false, queryData.get(attr.uid()), null, true, attr.displayDescription(), ObjectStyleModel.builder().build());
                    break;
                case TIME:
                case DATE:
                case DATETIME:
                    viewModel = DateTimeViewModel.create(attr.uid(), label, false, attr.valueType(), queryData.get(attr.uid()), null, true, true, attr.displayDescription(), ObjectStyleModel.builder().build());
                    break;
                case AGEVIEW:
                    viewModel = AgeViewModel.create(attr.uid(), label, false, queryData.get(attr.uid()), null, true, attr.displayDescription(), ObjectStyleModel.builder().build());
                    break;
                case ORG_UNIT:
                    viewModel = OrgUnitViewModel.create(attr.uid(), label, false, queryData.get(attr.uid()), null, true, attr.displayDescription(), ObjectStyleModel.builder().build());
                    break;
                default:
                    Crashlytics.log("Unsupported viewType " +
                            "source type: " + holder.getItemViewType());
                    viewModel = EditTextViewModel.create(attr.uid(), "UNSUPORTED", false, null, "UNSUPPORTED", 1, attr.valueType(), null, false, attr.displayDescription(), null, ObjectStyleModel.builder().build());
                    break;
            }
        }
        rows.get(holder.getItemViewType()).onBind(holder, viewModel);

    }

    @Override
    public int getItemCount() {
        return attributeList != null ? attributeList.size() + programData : programData;
    }

    @Override
    public long getItemId(int position) {
        if (position < programData) {
            return position == 0 ? ENROLLMENT_DATE_ID : INCIDENT_DATE_ID;
        } else {
            return attributeList.get(position - programData).uid().hashCode();
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (position < programData)
            return DATE;

        if (attributeList.get(position - programData).optionSet() != null)
            return SPINNER;
        else {
            switch (attributeList.get(position - programData).valueType()) {
                case AGE:
                    return AGEVIEW;
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
                    return EDITTEXT;
                case LONG_TEXT:
                    return EDITTEXT;
                case TIME:
                    return TIME;
                case DATE:
                    return DATE;
                case DATETIME:
                    return DATETIME;
                case FILE_RESOURCE:
                    return BUTTON;
                case COORDINATE:
                    return COORDINATES;
                case BOOLEAN:
                case TRUE_ONLY:
                    return YES_NO;
                case ORGANISATION_UNIT:
                    return ORG_UNIT;
                case TRACKER_ASSOCIATE:
                default:
                    return EDITTEXT;
            }
        }

    }

    public void setList(List<TrackedEntityAttributeModel> modelList, ProgramModel programModel, HashMap<String, String> queryData) {
        this.queryData = queryData;
        if (programModel != null) {
            this.programModel = programModel;
            programData = programModel.displayIncidentDate() ? 1 : 0;
        } else {
            programData = 0;
        }

        this.attributeList = modelList;

        notifyDataSetChanged();

    }

    @NonNull
    public FlowableProcessor<RowAction> asFlowableRA() {
        return processor;
    }

    public FlowableProcessor<Trio<String, String, Integer>> asFlowableOption() {
        return processorOptionSet;
    }
}
