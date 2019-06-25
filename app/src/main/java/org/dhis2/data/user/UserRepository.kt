package org.dhis2.data.user

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel
import org.hisp.dhis.android.core.user.UserCredentialsModel
import org.hisp.dhis.android.core.user.UserModel

import io.reactivex.Flowable
import io.reactivex.Observable

interface UserRepository {

    fun credentials(): Flowable<UserCredentialsModel>

    fun me(): Flowable<UserModel>

}
