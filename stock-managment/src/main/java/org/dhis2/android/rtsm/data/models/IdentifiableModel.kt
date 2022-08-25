package org.dhis2.android.rtsm.data.models

import android.os.Parcel
import android.os.Parcelable

// TODO: Ensure that you flag errors if any of the entities being
//  converted to a parcel do not have a name or display name at
//  creation time in their various instantiations
class IdentifiableModel(
    val uid: String,
    val name: String,
    val displayName: String
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(uid)
        dest.writeString(name)
        dest.writeString(displayName)
    }

    companion object CREATOR : Parcelable.Creator<IdentifiableModel> {
        override fun createFromParcel(parcel: Parcel): IdentifiableModel = IdentifiableModel(parcel)

        override fun newArray(size: Int): Array<IdentifiableModel?> = arrayOfNulls(size)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null ||
            other !is IdentifiableModel ||
            uid != other.uid ||
            name != other.name ||
            displayName != displayName) return false

        return true
    }
}