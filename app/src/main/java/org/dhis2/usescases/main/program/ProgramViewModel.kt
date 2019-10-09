package org.dhis2.usescases.main.program

import androidx.databinding.BaseObservable

import com.google.auto.value.AutoValue

import org.hisp.dhis.android.core.common.State

import java.io.Serializable

@AutoValue
abstract class ProgramViewModel : BaseObservable(), Serializable {

    val state: State
        get() = State.valueOf(state())

    abstract fun id(): String

    abstract fun title(): String

    abstract fun color(): String?

    abstract fun icon(): String?

    abstract fun count(): Int

    abstract fun type(): String?

    abstract fun typeName(): String

    abstract fun programType(): String

    abstract fun description(): String?

    abstract fun onlyEnrollOnce(): Boolean

    abstract fun accessDataWrite(): Boolean

    abstract fun state(): String

    companion object {

        fun create(uid: String, displayName: String, color: String?,
                   icon: String?, count: Int, type: String?,
                   typeName: String, programType: String, description: String?, onlyEnrollOnce: Boolean, accessDataWrite: Boolean): ProgramViewModel {
            return AutoValue_ProgramViewModel(uid, displayName, color, icon, count, type, typeName, programType, description, onlyEnrollOnce, accessDataWrite, State.SYNCED.name)
        }

        fun create(uid: String, displayName: String, color: String?,
                   icon: String?, count: Int, type: String?,
                   typeName: String, programType: String, description: String?, onlyEnrollOnce: Boolean, accessDataWrite: Boolean, state: String): ProgramViewModel {
            return AutoValue_ProgramViewModel(uid, displayName, color, icon, count, type, typeName, programType, description, onlyEnrollOnce, accessDataWrite, state)
        }
    }

}
