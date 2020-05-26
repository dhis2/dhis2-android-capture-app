package org.dhis2.usescases.settingsprogram

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.dhis2.databinding.ItemSettingProgramBinding
import org.dhis2.utils.resources.ResourceManager

class SettingsProgramAdapter(private val resourceManager: ResourceManager) :
    ListAdapter<ProgramSettingsViewModel, ProgramSettingsHolder>(
        object : DiffUtil.ItemCallback<ProgramSettingsViewModel>() {
            override fun areItemsTheSame(
                oldItem: ProgramSettingsViewModel,
                newItem: ProgramSettingsViewModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ProgramSettingsViewModel,
                newItem: ProgramSettingsViewModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramSettingsHolder {
        return ProgramSettingsHolder(
            ItemSettingProgramBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            resourceManager
        )
    }

    override fun onBindViewHolder(holder: ProgramSettingsHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
