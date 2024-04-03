package dhis2.org.analytics.charts.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import dhis2.org.R
import dhis2.org.databinding.ItemIndicatorBinding
import org.dhis2.commons.dialogs.CustomDialog
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.IndicatorInput
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

class IndicatorViewHolder(
    val binding: ItemIndicatorBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(programIndicatorModel: IndicatorModel) {
        binding.composeView.setContent {
            Column(
                Modifier
                    .fillMaxSize()
                    .then(
                        if (programIndicatorModel.programIndicator?.description() != null) {
                            Modifier.clickable(
                                interactionSource = MutableInteractionSource(),
                                indication = rememberRipple(),
                                onClick = { showDescription(programIndicatorModel.programIndicator) },
                            )
                        } else {
                            Modifier
                        },
                    ),
            ) {
                IndicatorInput(
                    title = programIndicatorModel.label(),
                    indicatorColor = if (!programIndicatorModel.color.isNullOrEmpty()) {
                        Color(programIndicatorModel.color())
                    } else {
                        SurfaceColor.Container
                    },
                    content = programIndicatorModel.value ?: "",
                    modifier = Modifier.then(
                        if (programIndicatorModel.programIndicator?.description() != null) {
                            Modifier.clickable(
                                interactionSource = MutableInteractionSource(),
                                indication = rememberRipple(),
                                onClick = { showDescription(programIndicatorModel.programIndicator) },
                            )
                        } else {
                            Modifier
                        },
                    ),
                )
                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }

    private fun showDescription(programIndicatorModel: ProgramIndicator?) {
        programIndicatorModel?.let {
            CustomDialog(
                itemView.context,
                it.displayName() ?: "",
                it.displayDescription() ?: "",
                itemView.context.getString(R.string.action_accept),
                null,
                CustomDialog.DESCRIPTION_DIALOG,
                null,
            ).show()
        }
    }
}
