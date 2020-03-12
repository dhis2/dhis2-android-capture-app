package org.dhis2.data.forms.dataentry.fields.scan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.Row
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.databinding.FormScanBinding

class ScanTextRow(
    val inflater: LayoutInflater,
    val processor: FlowableProcessor<RowAction>,
    val isBgTransparent: Boolean
) : Row<ScanTextHolder, ScanTextViewModel> {

    override fun onCreate(parent: ViewGroup): ScanTextHolder {
        val binding = DataBindingUtil.inflate<FormScanBinding>(
            inflater,
            R.layout.form_scan,
            parent,
            false
        )
        binding.scanTextView.setLayoutData(isBgTransparent)
        return ScanTextHolder(binding, processor)
    }

    override fun onBind(viewHolder: ScanTextHolder, viewModel: ScanTextViewModel) {
        viewHolder.update(viewModel)
    }
}
