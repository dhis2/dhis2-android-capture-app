/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.usescases.programStageSelection

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.BR
import org.dhis2.Bindings.Bindings
import org.dhis2.databinding.ItemProgramStageBinding
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.program.ProgramStage

class ProgramStageSelectionViewHolder(
    private val binding: ItemProgramStageBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(presenter: ProgramStageSelectionPresenter, programStage: ProgramStage) {
        binding.setVariable(BR.presenter, presenter)
        binding.setVariable(BR.programStage, programStage)
        binding.executePendingBindings()

        val style: ObjectStyle = programStage.style() ?: ObjectStyle.builder().build()

        style.icon()?.let {
            val resources = binding.programStageIcon.context.resources
            val iconName = if (it.startsWith("ic_")) it else "ic_$it"
            val icon = resources.getIdentifier(
                iconName,
                "drawable",
                binding.programStageIcon.context.packageName
            )
            binding.programStageIcon.setImageResource(icon)
        }

        style.color()?.let {
            val color = if (it.startsWith("#")) it else "#$it"
            val colorRes = Color.parseColor(color)
            val colorStateList = ColorStateList.valueOf(colorRes)
            ViewCompat.setBackgroundTintList(binding.programStageIcon, colorStateList)
            Bindings.setFromResBgColor(binding.programStageIcon, colorRes)
        }

        itemView.setOnClickListener {
            presenter.onProgramStageClick(programStage)
        }
    }
}
