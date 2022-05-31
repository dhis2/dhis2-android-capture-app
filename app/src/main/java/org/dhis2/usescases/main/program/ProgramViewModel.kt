package org.dhis2.usescases.main.program

import org.hisp.dhis.android.core.common.State

data class ProgramViewModel(
    val uid:String,
    val title:String,
    val color:String?,
    val icon:String?,
    val count:Int,
    val type:String?,
    val typeName: String,
    val programType: String,
    val description: String?,
    val onlyEnrollOnce: Boolean,
    val accessDataWrite: Boolean,
    val state: State,
    val hasOverdueEvent: Boolean,
    val filtersAreActive: Boolean
){
    fun translucent():Boolean {
        return filtersAreActive && count == 0
    }
}