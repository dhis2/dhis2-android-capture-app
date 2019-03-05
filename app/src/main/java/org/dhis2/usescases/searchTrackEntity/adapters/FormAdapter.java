package org.dhis2.usescases.searchTrackEntity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

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
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitRow;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonRow;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerRow;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 06/11/2017.
 */

public class FormAdapter extends RecyclerView.Adapter {

    private static final long ENROLLMENT_DATE_ID = 1;
    private static final long INCIDENT_DATE_ID = 2;

    private int programData = 0;
    private List<TrackedEntityAttribute> attributeList;
    private ProgramModel programModel;
    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;

    @NonNull
    private final List<Row> rows;

    private Context context;
    private Map<String, String> queryData;

    public FormAdapter(FragmentManager fm, LayoutInflater layoutInflater, Observable<List<OrganisationUnitModel>> orgUnits, Context context) {
        setHasStableIds(true);
        this.processor = PublishProcessor.create();
        this.processorOptionSet = PublishProcessor.create();
        this.context = context;
        attributeList = new ArrayList<>();
        rows = new ArrayList<>();

        rows.add(Constants.EDITTEXT, new EditTextRow(layoutInflater, processor, false));
        rows.add(Constants.BUTTON, new FileRow(layoutInflater, false));
        rows.add(Constants.CHECKBOX, new RadioButtonRow(layoutInflater, processor, false));
        rows.add(Constants.SPINNER, new SpinnerRow(layoutInflater, processor, processorOptionSet, false));
        rows.add(Constants.COORDINATES, new CoordinateRow(layoutInflater, processor, false));
        rows.add(Constants.TIME, new DateTimeRow(layoutInflater, processor, Constants.TIME, false));
        rows.add(Constants.DATE, new DateTimeRow(layoutInflater, processor, Constants.DATE, false));
        rows.add(Constants.DATETIME, new DateTimeRow(layoutInflater, processor, Constants.DATETIME, false));
        rows.add(Constants.AGEVIEW, new AgeRow(layoutInflater, processor, false));
        rows.add(Constants.YES_NO, new RadioButtonRow(layoutInflater, processor, false));
        rows.add(Constants.ORG_UNIT, new OrgUnitRow(fm, layoutInflater, processor, false, orgUnits));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return rows.get(viewType).onCreate(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FieldViewModel viewModel;
        if (position < programData) {

            String enrollmentDate = !isEmpty(programModel.enrollmentDateLabel()) ? programModel.enrollmentDateLabel() : context.getString(R.string.enrollmment_date);
            String incidentDate = !isEmpty(programModel.incidentDateLabel()) ? programModel.incidentDateLabel() : context.getString(R.string.incident_date);

            viewModel = DateTimeViewModel.create(
                    position == 0 ? Constants.ENROLLMENT_DATE_UID : Constants.INCIDENT_DATE_UID,
                    holder.getAdapterPosition() == 0 ? enrollmentDate : incidentDate,
                    false,
                    ValueType.DATE,
                    null,
                    null,
                    holder.getAdapterPosition() == 0 ? programModel.selectEnrollmentDatesInFuture() : programModel.selectIncidentDatesInFuture(), true, null,
                    ObjectStyleModel.builder().build());

        } else {
            TrackedEntityAttribute attr = attributeList.get(holder.getAdapterPosition() - programData);
            String label = attr.displayName();
            switch (holder.getItemViewType()) {
                case Constants.EDITTEXT:
                    viewModel = EditTextViewModel.create(attr.uid(), label, false,
                            queryData.get(attr.uid()), label, 1, attr.valueType(), null, !attr.generated(),
                            attr.displayDescription(), null, ObjectStyleModel.builder().build());
                    break;
                case Constants.BUTTON:
                    viewModel = FileViewModel.create(attr.uid(), label, false, queryData.get(attr.uid()), null, attr.displayDescription(), ObjectStyleModel.builder().build());
                    break;
                case Constants.CHECKBOX:
                case Constants.YES_NO:
                    viewModel = RadioButtonViewModel.fromRawValue(attr.uid(), label, attr.valueType(), false, queryData.get(attr.uid()), null, true, attr.displayDescription(), ObjectStyleModel.builder().build());
                    break;
                case Constants.SPINNER:
                    viewModel = SpinnerViewModel.create(attr.uid(), label, "", false, attr.optionSet().uid(), queryData.get(attr.uid()), null, true, attr.displayDescription(), 20, ObjectStyleModel.builder().build());
                    break;
                case Constants.COORDINATES:
                    viewModel = CoordinateViewModel.create(attr.uid(), label, false, queryData.get(attr.uid()), null, true, attr.displayDescription(), ObjectStyleModel.builder().build());
                    break;
                case Constants.TIME:
                case Constants.DATE:
                case Constants.DATETIME:
                    viewModel = DateTimeViewModel.create(attr.uid(), label, false, attr.valueType(), queryData.get(attr.uid()), null, true, true, attr.displayDescription(), ObjectStyleModel.builder().build());
                    break;
                case Constants.AGEVIEW:
                    viewModel = AgeViewModel.create(attr.uid(), label, false, queryData.get(attr.uid()), null, true, attr.displayDescription(), ObjectStyleModel.builder().build());
                    break;
                case Constants.ORG_UNIT:
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
            return Constants.DATE;

        if (attributeList.get(position - programData).optionSet() != null)
            return Constants.SPINNER;
        else {
            switch (attributeList.get(position - programData).valueType()) {
                case AGE:
                    return Constants.AGEVIEW;
                case TEXT:
                case EMAIL:
                case LETTER:
                case NUMBER:
                case INTEGER:
                case USERNAME:
                case LONG_TEXT:
                case PERCENTAGE:
                case PHONE_NUMBER:
                case INTEGER_NEGATIVE:
                case INTEGER_POSITIVE:
                case INTEGER_ZERO_OR_POSITIVE:
                case UNIT_INTERVAL:
                case URL:
                    return Constants.EDITTEXT;
                case TIME:
                    return Constants.TIME;
                case DATE:
                    return Constants.DATE;
                case DATETIME:
                    return Constants.DATETIME;
                case FILE_RESOURCE:
                    return Constants.BUTTON;
                case COORDINATE:
                    return Constants.COORDINATES;
                case BOOLEAN:
                case TRUE_ONLY:
                    return Constants.YES_NO;
                case ORGANISATION_UNIT:
                    return Constants.ORG_UNIT;
                case TRACKER_ASSOCIATE:
                default:
                    return Constants.EDITTEXT;
            }
        }

    }

    public void setList(List<TrackedEntityAttribute> modelList, ProgramModel programModel, Map<String, String> queryData) {
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