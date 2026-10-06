package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.login.main.data.LoginRepository

internal const val RECOVERY_PATH = "/dhis-web-commons/security/recovery.action"
internal const val RECOVERY_PATH_V43 = "/login/#/reset-password"

class GetRecoveryUrl(
    private val repository: LoginRepository,
) : UseCase<String, String> {
    override suspend fun invoke(input: String): Result<String> {
        val apiVersion =
            try {
                repository.getServerApiVersion(input)
            } catch (_: Exception) {
                null
            }
        val path = if (isVersion43OrHigher(apiVersion)) RECOVERY_PATH_V43 else RECOVERY_PATH
        return Result.success("${input.trimEnd('/')}$path")
    }

    private fun isVersion43OrHigher(apiVersion: String?): Boolean {
        val parts =
            apiVersion
                ?.substringBefore("-")
                ?.split(".")
                ?.map { it.toIntOrNull() ?: return false }
                ?.takeIf { it.size >= 2 }
                ?: return false
        val (major, minor) = parts
        return major > 2 || (major == 2 && minor >= 43)
    }
}
