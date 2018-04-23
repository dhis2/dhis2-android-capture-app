package com.dhis2.usescases.searchTrackEntity.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.data.forms.dataentry.fields.age.AgeRow;
import com.dhis2.data.forms.dataentry.fields.age.AgeViewModel;
import com.dhis2.data.forms.dataentry.fields.coordinate.CoordinateRow;
import com.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeRow;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;
import com.dhis2.data.forms.dataentry.fields.edittext.EditTextRow;
import com.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import com.dhis2.data.forms.dataentry.fields.file.FileRow;
import com.dhis2.data.forms.dataentry.fields.file.FileViewModel;
import com.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitRow;
import com.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonRow;
import com.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import com.dhis2.data.forms.dataentry.fields.spinner.SpinnerRow;
import com.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * Created by ppajuelo on 06/11/2017.
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
    private int programData = 0;
    private List<TrackedEntityAttributeModel> attributeList;
    private ProgramModel programModel;
    @NonNull
    private final FlowableProcessor<RowAction> processor;

    @NonNull
    private final List<Row> rows;

    public FormAdapter(LayoutInflater layoutInflater) {
        setHasStableIds(true);
        //        this.processor = PublishProcessor.create();
        this.processor = PublishProcessor.create();
        rows = new ArrayList<>();

        rows.add(EDITTEXT, new EditTextRow(layoutInflater, processor, false));
        rows.add(BUTTON, new FileRow(layoutInflater, processor, false));
        rows.add(CHECKBOX, new RadioButtonRow(layoutInflater, processor, false));
        rows.add(SPINNER, new SpinnerRow(layoutInflater, processor, false));
        rows.add(COORDINATES, new CoordinateRow(layoutInflater, processor, false));
        rows.add(TIME, new DateTimeRow(layoutInflater, processor, TIME, false));
        rows.add(DATE, new DateTimeRow(layoutInflater, processor, DATE, false));
        rows.add(DATETIME, new DateTimeRow(layoutInflater, processor, DATETIME, false));
        rows.add(AGEVIEW, new AgeRow(layoutInflater, processor, false));
        rows.add(YES_NO, new RadioButtonRow(layoutInflater, processor, false));
        rows.add(ORG_UNIT, new OrgUnitRow(layoutInflater, processor, false));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return rows.get(viewType).onCreate(parent);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FieldViewModel viewModel = null;

        if (position < programData) {
//            ((DateTimeFormHolder) holder).bindProgramData(presenter, holder.getAdapterPosition() == 0 ? programModel.enrollmentDateLabel() : programModel.incidentDateLabel(), holder.getAdapterPosition());
            viewModel = DateTimeViewModel.create(String.valueOf(programModel.id() + position), holder.getAdapterPosition() == 0 ? programModel.enrollmentDateLabel() : programModel.incidentDateLabel(), false, ValueType.DATE, null, null);

        } else {
            TrackedEntityAttributeModel attr = attributeList.get(holder.getAdapterPosition() - programData);
            switch (holder.getItemViewType()) {
                case EDITTEXT:
                    viewModel = EditTextViewModel.create(attr.uid(), attr.displayShortName(), false, null, attr.displayShortName(), 1, attr.valueType(), null);
                    break;
                case BUTTON:
                    viewModel = FileViewModel.create(attr.uid(), attr.displayShortName(), false, null, null);
                    break;
                case CHECKBOX:
                case YES_NO:
                    viewModel = RadioButtonViewModel.fromRawValue(attr.uid(), attr.displayShortName(), attr.valueType(), false, null, null);
                    break;
                case SPINNER:
                    viewModel = SpinnerViewModel.create(attr.uid(), attr.displayShortName(), "Hola", false, attr.optionSet(), null, null);
                    break;
                case COORDINATES:
                    viewModel = CoordinateViewModel.create(attr.uid(), attr.displayShortName(), false, null, null);
                    break;
                case TIME:
                case DATE:
                case DATETIME:
                    viewModel = DateTimeViewModel.create(attr.uid(), attr.displayShortName(), false, attr.valueType(), null, null);
                    break;
                case AGEVIEW:
                    viewModel = AgeViewModel.create(attr.uid(), attr.displayShortName(), false, null, null);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported viewType " +
                            "source type: " + holder.getItemViewType());
            }
         /*   if (holder.getItemViewType() == EDITTEXT)
                rows.get(holder.getItemViewType()).onBind(holder, EditTextViewModel.create(attr.uid(), attr.displayShortName(), false, null,
                        attr.displayShortName(), 1, attr.valueType()));
            else if (holder.getItemViewType() == SPINNER) {
                rows.get(holder.getItemViewType()).onBind(holder, SpinnerViewModel.create(attr.uid(), attr.displayShortName(), "Hola", false, attr.optionSet(), null));
            } else
                ((FormViewHolder) holder).bind(presenter, attributeList.get(holder.getAdapterPosition() - programData));*/
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
                case LONG_TEXT:
                case PERCENTAGE:
                case PHONE_NUMBER:
                case INTEGER_NEGATIVE:
                case INTEGER_POSITIVE:
                case INTEGER_ZERO_OR_POSITIVE:
                case UNIT_INTERVAL:
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
                case URL:
                default:
                    return EDITTEXT;
            }
        }

    }

    public void setList(List<TrackedEntityAttributeModel> modelList, ProgramModel programModel) {

        if (programModel != null) {
            this.programModel = programModel;
            programData = programModel.displayIncidentDate() ? 2 : 1;
        } else {
            programData = 0;
            List<TrackedEntityAttributeModel> modelListnew = new ArrayList<>();
            for (TrackedEntityAttributeModel attributeModel : modelList) {
                if (attributeModel.displayInListNoProgram())
                    modelListnew.add(attributeModel);
            }
            modelList = new ArrayList<>(modelListnew);
        }


        this.attributeList = modelList;
        notifyDataSetChanged();
    }

    @NonNull
    public FlowableProcessor<RowAction> asFlowableRA() {
        return processor;
    }
}
