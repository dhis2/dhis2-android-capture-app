package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.forms.dataentry.tablefields.age.AgeRow;
import org.dhis2.data.forms.dataentry.tablefields.age.AgeViewModel;
import org.dhis2.data.forms.dataentry.tablefields.coordinate.CoordinateRow;
import org.dhis2.data.forms.dataentry.tablefields.coordinate.CoordinateViewModel;
import org.dhis2.data.forms.dataentry.tablefields.datetime.DateTimeRow;
import org.dhis2.data.forms.dataentry.tablefields.datetime.DateTimeViewModel;
import org.dhis2.data.forms.dataentry.tablefields.edittext.EditTextModel;
import org.dhis2.data.forms.dataentry.tablefields.edittext.EditTextRow;
import org.dhis2.data.forms.dataentry.tablefields.file.FileCellRow;
import org.dhis2.data.forms.dataentry.tablefields.file.FileViewModel;
import org.dhis2.data.forms.dataentry.tablefields.image.ImageRow;
import org.dhis2.data.forms.dataentry.tablefields.image.ImageViewModel;
import org.dhis2.data.forms.dataentry.tablefields.orgUnit.OrgUnitRow;
import org.dhis2.data.forms.dataentry.tablefields.orgUnit.OrgUnitViewModel;
import org.dhis2.data.forms.dataentry.tablefields.radiobutton.RadioButtonRow;
import org.dhis2.data.forms.dataentry.tablefields.radiobutton.RadioButtonViewModel;
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerCellRow;
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel;
import org.dhis2.data.forms.dataentry.tablefields.unsupported.UnsupportedRow;
import org.dhis2.data.forms.dataentry.tablefields.unsupported.UnsupportedViewModel;
import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetTableAdapter extends AbstractTableAdapter<CategoryOption, DataElement, String> {
    private static final int EDITTEXT = 0;
    private static final int BUTTON = 1;
    private static final int CHECKBOX = 2;
    private static final int SPINNER = 3;
    private static final int COORDINATES = 4;
    private static final int TIME = 5;
    private static final int DATE = 6;
    private static final int DATETIME = 7;
    private static final int AGEVIEW = 8;
    private static final int YES_NO = 9;
    private static final int ORG_UNIT = 10;
    private static final int IMAGE = 11;
    private static final int UNSUPPORTED = 12;
    private final Context context;

    @NonNull
    private List<List<FieldViewModel>> viewModels;

    @NonNull
    private final FlowableProcessor<RowAction> processor;
    @NonNull
    private final List<Row> rows;

    private LayoutInflater layoutInflater;

    private Boolean showRowTotal = false;
    private Boolean showColumnTotal = false;
    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;

    private int currentWidth = 200;
    private int currentHeight;

    private String catCombo;
    private Boolean dataElementDecoration;

    public enum TableScale {
        SMALL, DEFAULT, LARGE
    }

    private ObservableField<TableScale> currentTableScale = new ObservableField<>();

    public void scale() {
        if (currentWidth == 200) {
            currentWidth = 300;
            currentHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 36, context.getResources().getDisplayMetrics());
            currentTableScale.set(TableScale.DEFAULT);
        } else if (currentWidth == 300) {
            currentWidth = 400;
            currentHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 48, context.getResources().getDisplayMetrics());
            currentTableScale.set(TableScale.LARGE);
        } else if (currentWidth == 400) {
            currentWidth = 200;
            currentHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, context.getResources().getDisplayMetrics());
            currentTableScale.set(TableScale.SMALL);

        }

        onScaleListener.scaleTo(currentWidth, currentHeight);

        notifyDataSetChanged();
    }

    public ObservableField<TableScale> getCurrentTableScale() {
        return currentTableScale;
    }


    public DataSetTableAdapter(Context context, @NotNull FlowableProcessor<RowAction> processor, FlowableProcessor<Trio<String, String, Integer>> processorOptionSet) {
        super(context);
        this.currentHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 36, context.getResources().getDisplayMetrics());
        this.currentTableScale.set(TableScale.DEFAULT);
        this.context = context;
        rows = new ArrayList<>();
        this.processor = processor;
        this.processorOptionSet = processorOptionSet;
        layoutInflater = LayoutInflater.from(context);
        viewModels = new ArrayList<>();
    }

    public void initializeRows(Boolean accessDataWrite) {
        rows.add(EDITTEXT, new EditTextRow(layoutInflater, processor, new ObservableBoolean(accessDataWrite), (TableView) getTableView(), currentTableScale));
        rows.add(BUTTON, new FileCellRow(layoutInflater, processor, currentTableScale));
        rows.add(CHECKBOX, new RadioButtonRow(layoutInflater, processor, accessDataWrite, currentTableScale));
        rows.add(SPINNER, new SpinnerCellRow(layoutInflater, processor, accessDataWrite, processorOptionSet, currentTableScale));
        rows.add(COORDINATES, new CoordinateRow(layoutInflater, processor, accessDataWrite, currentTableScale));
        rows.add(TIME, new DateTimeRow(layoutInflater, processor, TIME, true, accessDataWrite, currentTableScale));
        rows.add(DATE, new DateTimeRow(layoutInflater, processor, DATE, true, accessDataWrite, currentTableScale));
        rows.add(DATETIME, new DateTimeRow(layoutInflater, processor, DATETIME, true, accessDataWrite, currentTableScale));
        rows.add(AGEVIEW, new AgeRow(layoutInflater, processor, accessDataWrite, currentTableScale));
        rows.add(YES_NO, new RadioButtonRow(layoutInflater, processor, accessDataWrite, currentTableScale));
        rows.add(ORG_UNIT, new OrgUnitRow(null, layoutInflater, processor, true, ProgramStageSectionRenderingType.LISTING.name())); //TODO: TABLE SCALE
        rows.add(IMAGE, new ImageRow(layoutInflater, processor, true, ProgramStageSectionRenderingType.LISTING.name()));
        rows.add(UNSUPPORTED, new UnsupportedRow(layoutInflater, processor));
    }

    /**
     * This is where you create your custom Cell ViewHolder. This method is called when Cell
     * RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given type to
     * represent an item.
     *
     * @param viewType : This value comes from #getCellItemViewType method to support different type
     *                 of viewHolder as a Cell item.
     * @see #getCellItemViewType(int, int);
     */
    @Override
    public AbstractViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        return rows.get(viewType).onCreate(parent);
    }

    /**
     * That is where you set Cell View Model data to your custom Cell ViewHolder. This method is
     * Called by Cell RecyclerView of the TableView to display the data at the specified position.
     * This method gives you everything you need about a cell item.
     *
     * @param holder         : This is one of your cell ViewHolders that was created on
     *                       ```onCreateCellViewHolder``` method. In this example we have created
     *                       "MyCellViewHolder" holder.
     * @param cellItemModel  : This is the cell view model located on this X and Y position. In this
     *                       example, the model class is "Cell".
     * @param columnPosition : This is the X (Column) position of the cell item.
     * @param rowPosition    : This is the Y (Row) position of the cell item.
     * @see #onCreateCellViewHolder(ViewGroup, int);
     */
    @Override
    public void onBindCellViewHolder(AbstractViewHolder holder, Object cellItemModel, int columnPosition, int rowPosition) {

        String value = cellItemModel != null && !cellItemModel.equals("") ? cellItemModel.toString() : viewModels.get(rowPosition).get(columnPosition).value();

        rows.get(holder.getItemViewType()).onBind(holder, viewModels.get(rowPosition).get(columnPosition), value);
        holder.itemView.getLayoutParams().width = currentWidth;
        holder.itemView.getLayoutParams().height = currentHeight;
    }

    public void swap(List<List<FieldViewModel>> viewModels) {
        this.viewModels = viewModels;
    }


    /**
     * This is where you create your custom Column Header ViewHolder. This method is called when
     * Column Header RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given
     * type to represent an item.
     *
     * @param viewType : This value comes from "getColumnHeaderItemViewType" method to support
     *                 different type of viewHolder as a Column Header item.
     * @see #getColumnHeaderItemViewType(int);
     */
    @Override
    public AbstractViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {
        return new DataSetRHeaderHeader(
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_dataset_header, parent, false)
        );
    }

    /**
     * That is where you set Column Header View Model data to your custom Column Header ViewHolder.
     * This method is Called by ColumnHeader RecyclerView of the TableView to display the data at
     * the specified position. This method gives you everything you need about a column header
     * item.
     *
     * @param holder                : This is one of your column header ViewHolders that was created on
     *                              ```onCreateColumnHeaderViewHolder``` method. In this example we have created
     *                              "MyColumnHeaderViewHolder" holder.
     * @param columnHeaderItemModel : This is the column header view model located on this X position. In this
     *                              example, the model class is "ColumnHeader".
     * @param position              : This is the X (Column) position of the column header item.
     * @see #onCreateColumnHeaderViewHolder(ViewGroup, int) ;
     */
    @Override
    public void onBindColumnHeaderViewHolder(AbstractViewHolder holder, Object columnHeaderItemModel, int position) {
        ((DataSetRHeaderHeader) holder).bind(((CategoryOption) columnHeaderItemModel).displayName(), currentTableScale);
        if (((CategoryOption) columnHeaderItemModel).displayName().isEmpty()) {
            ((DataSetRHeaderHeader) holder).binding.container.getLayoutParams().width = currentWidth;
        } else {
            int i = getHeaderRecyclerPositionFor(columnHeaderItemModel);
            ((DataSetRHeaderHeader) holder).binding.container.getLayoutParams().width =
                    (currentWidth * i + (int) (context.getResources().getDisplayMetrics().density * (i - 1)));
        }
        ((DataSetRHeaderHeader) holder).binding.title.requestLayout();
    }

    /**
     * This is where you create your custom Row Header ViewHolder. This method is called when
     * Row Header RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given
     * type to represent an item.
     *
     * @param viewType : This value comes from "getRowHeaderItemViewType" method to support
     *                 different type of viewHolder as a row Header item.
     * @see #getRowHeaderItemViewType(int);
     */
    @Override
    public AbstractViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {
        return new DataSetRowHeader(
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_dataset_row, parent, false)
        );
    }


    /**
     * That is where you set Row Header View Model data to your custom Row Header ViewHolder. This
     * method is Called by RowHeader RecyclerView of the TableView to display the data at the
     * specified position. This method gives you everything you need about a row header item.
     *
     * @param holder             : This is one of your row header ViewHolders that was created on
     *                           ```onCreateRowHeaderViewHolder``` method. In this example we have created
     *                           "MyRowHeaderViewHolder" holder.
     * @param rowHeaderItemModel : This is the row header view model located on this Y position. In this
     *                           example, the model class is "RowHeader".
     * @param position           : This is the Y (row) position of the row header item.
     * @see #onCreateRowHeaderViewHolder(ViewGroup, int) ;
     */
    @Override
    public void onBindRowHeaderViewHolder(AbstractViewHolder holder, Object rowHeaderItemModel, int
            position) {
        ((DataSetRowHeader) holder).bind(mRowHeaderItems.get(position), currentTableScale, dataElementDecoration);
        holder.itemView.getLayoutParams().height = currentHeight;
    }


    @Override
    public View onCreateCornerView() {
        // Get Corner xml layout
        return null;
        /*View corner = LayoutInflater.from(mContext).inflate(R.layout.table_view_corner_layout, null);
        corner.findViewById(R.id.buttonScale).setOnClickListener(view -> scale());
        return corner;*/
    }

    @Override
    public int getColumnHeaderItemViewType(int columnPosition) {
        return 0;
    }

    @Override
    public int getRowHeaderItemViewType(int rowPosition) {
        return 0;
    }

    @Override
    public int getCellItemViewType(int columnPosition, int rowPosition) {

        FieldViewModel viewModel = viewModels.get(rowPosition).get(0);

        if (viewModel instanceof EditTextModel) {
            return EDITTEXT;
        } else if (viewModel instanceof RadioButtonViewModel) {
            return CHECKBOX;
        } else if (viewModel instanceof SpinnerViewModel) {
            return SPINNER;
        } else if (viewModel instanceof CoordinateViewModel) {
            return COORDINATES;

        } else if (viewModel instanceof DateTimeViewModel) {
            if (((DateTimeViewModel) viewModel).valueType() == ValueType.DATE)
                return DATE;
            if (((DateTimeViewModel) viewModel).valueType() == ValueType.TIME)
                return TIME;
            else
                return DATETIME;
        } else if (viewModel instanceof AgeViewModel) {
            return AGEVIEW;
        } else if (viewModel instanceof FileViewModel) {
            return BUTTON;
        } else if (viewModel instanceof OrgUnitViewModel) {
            return ORG_UNIT;
        } else if (viewModel instanceof ImageViewModel) {
            return IMAGE;
        } else if (viewModel instanceof UnsupportedViewModel) {
            return UNSUPPORTED;
        } else {
            throw new IllegalStateException("Unsupported view model type: "
                    + viewModel.getClass());
        }
    }

    @NonNull
    public FlowableProcessor<RowAction> asFlowable() {
        return processor;
    }

    public FlowableProcessor<Trio<String, String, Integer>> asFlowableOptionSet() {
        return processorOptionSet;
    }

    public void updateValue(RowAction rowAction) {
        if (showRowTotal || showColumnTotal) {
            int oldValue = 0;
            if (getCellItem(rowAction.columnPos(), rowAction.rowPos()) != null && !getCellItem(rowAction.columnPos(), rowAction.rowPos()).isEmpty())
                oldValue = Integer.parseInt(getCellItem(rowAction.columnPos(), rowAction.rowPos()));

            if (showRowTotal) {
                int totalRow = Integer.parseInt(getCellItem(viewModels.get(0).size() - 1, rowAction.rowPos()))
                        + (Integer.parseInt(rowAction.value() != null ? rowAction.value() : "0") - oldValue);
                changeCellItem(viewModels.get(0).size() - 1, rowAction.rowPos(), totalRow + "", showRowTotal);
            }
            if (showColumnTotal) {
                int totalColumn = Integer.parseInt(getCellItem(rowAction.columnPos(), viewModels.size() - 1))
                        + (Integer.parseInt(rowAction.value() != null ? rowAction.value() : "0") - oldValue);
                changeCellItem(rowAction.columnPos(), viewModels.size() - 1, totalColumn + "", showColumnTotal);
            }
            if (showRowTotal && showColumnTotal) {
                int total = Integer.parseInt(getCellItem(viewModels.get(0).size() - 1, viewModels.size() - 1))
                        + (Integer.parseInt(rowAction.value() != null ? rowAction.value() : "0") - oldValue);
                changeCellItem(viewModels.get(0).size() - 1, viewModels.size() - 1, total + "", true);
            }
        }
        changeCellItem(rowAction.columnPos(), rowAction.rowPos(), rowAction.value() != null ? rowAction.value() : "", false);
    }

    public void setShowRowTotal(Boolean showRowTotal) {
        this.showRowTotal = showRowTotal;
    }

    public void setShowColumnTotal(Boolean showColumnTotal) {
        this.showColumnTotal = showColumnTotal;
    }

    public Boolean getShowRowTotal() {
        return showRowTotal;
    }

    public Boolean getShowColumnTotal() {
        return showColumnTotal;
    }

    public void setCatCombo(String catCombo) {
        this.catCombo = catCombo;
    }

    public String getCatCombo() {
        return catCombo;
    }

    public void setDataElementDecoration(Boolean dataElementDecoration) {
        this.dataElementDecoration = dataElementDecoration;
    }
}
