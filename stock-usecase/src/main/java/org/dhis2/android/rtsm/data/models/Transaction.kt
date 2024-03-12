package org.dhis2.android.rtsm.data.models

import android.os.Parcel
import android.os.Parcelable
import org.dhis2.android.rtsm.data.TransactionType

class Transaction(
    val transactionType: TransactionType,
    val facility: IdentifiableModel,
    val transactionDate: String,
    val distributedTo: IdentifiableModel?,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        enumValueOf(parcel.readString()!!),
        // TODO: Find a way to get the OrganisationUnit given the UID,
        //  and include it in the constructor call
        parcel.readParcelable<IdentifiableModel>(
            IdentifiableModel::class.java.classLoader,
        )!!,
        parcel.readString()!!,
        parcel.readParcelable<IdentifiableModel>(
            IdentifiableModel::class.java.classLoader,
        ),
    )

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(transactionType.name)
        out.writeParcelable(facility, flags)
        out.writeString(transactionDate)
        out.writeParcelable(distributedTo, flags)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction = Transaction(parcel)

        override fun newArray(size: Int): Array<Transaction?> = arrayOfNulls(size)
    }

    override fun toString(): String {
        return if (transactionType == TransactionType.DISTRIBUTION && distributedTo != null) {
            "Transaction[Type: %s, Facility: %s, Date: %s, Distributed to: %s]".format(
                transactionType.name,
                facility.displayName,
                transactionDate,
                distributedTo.displayName,
            )
        } else {
            "Transaction[Type: %s, Facility: %s, Date: %s]".format(
                transactionType.name,
                facility.displayName,
                transactionDate,
            )
        }
    }
}
