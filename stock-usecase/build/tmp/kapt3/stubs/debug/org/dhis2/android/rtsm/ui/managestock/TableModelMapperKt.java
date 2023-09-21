package org.dhis2.android.rtsm.ui.managestock;

import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.models.StockEntry;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.composetable.model.RowHeader;
import org.dhis2.composetable.model.TableCell;
import org.dhis2.composetable.model.TableHeader;
import org.dhis2.composetable.model.TableHeaderCell;
import org.dhis2.composetable.model.TableHeaderRow;
import org.dhis2.composetable.model.TableModel;
import org.dhis2.composetable.model.TableRowModel;
import org.hisp.dhis.android.core.arch.helpers.Result;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerZeroOrPositiveFailure;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\b\n\u0000\n\u0002\u0010\u000e\n\u0000\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0002"}, d2 = {"STOCK_TABLE_ID", "", "psm-v2.9-DEV_debug"})
public final class TableModelMapperKt {
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String STOCK_TABLE_ID = "STOCK";
}