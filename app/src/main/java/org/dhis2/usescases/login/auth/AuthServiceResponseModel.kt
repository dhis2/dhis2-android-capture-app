package org.dhis2.usescases.login.auth

data class AuthServiceResponseModel(
    val token: String?,
    val exception:String?
)