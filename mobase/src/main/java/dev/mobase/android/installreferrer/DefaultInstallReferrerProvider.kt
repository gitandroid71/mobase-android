package dev.mobase.android.installreferrer

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class DefaultInstallReferrerProvider(
    private val applicationContext: Context,
) : InstallReferrerProvider {
    override suspend fun getInstallReferrer(): Result<InstallReferrerDetails> {
        val client = InstallReferrerClient.newBuilder(applicationContext)
            .build()

        return try {
            val installReferrerDetails = withTimeoutOrNull(3_000) {
                suspendCancellableCoroutine<InstallReferrerDetails> { continuation ->
                    client.startConnection(object : InstallReferrerStateListener {
                        override fun onInstallReferrerSetupFinished(responseCode: Int) {
                            when (responseCode) {
                                InstallReferrerResponse.OK -> {
                                    val installReferrer = client.installReferrer.toInternalModel()
                                    continuation.resume(installReferrer)
                                }

                                InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                                    continuation.resumeWithException(
                                        InstallReferrerException(InstallReferrerError.FEATURE_NOT_SUPPORTED)
                                    )
                                }

                                InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                                    continuation.resumeWithException(
                                        InstallReferrerException(InstallReferrerError.SERVICE_UNAVAILABLE)
                                    )
                                }

                                else -> {
                                    continuation.resumeWithException(
                                        InstallReferrerException(InstallReferrerError.UNKNOWN)
                                    )
                                }
                            }
                        }

                        override fun onInstallReferrerServiceDisconnected() {
                            val e =
                                InstallReferrerException(InstallReferrerError.SERVICE_DISCONNECTED)
                            Timber.e(e, "InstallReferrer service disconnected")
                            continuation.resumeWithException(e)
                        }
                    })
                }
            } ?: throw InstallReferrerException(InstallReferrerError.TIMEOUT)

            Timber.d("Install referrer: $installReferrerDetails")
            Result.success(installReferrerDetails)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to get install referrer")
            Result.failure(e)
        } finally {
            try {
                client.endConnection()
            } catch (e: Throwable) {
                Timber.e(e, "Failed to end connection")
            }
        }
    }

    private fun ReferrerDetails.toInternalModel(): InstallReferrerDetails {
        return InstallReferrerDetails(installReferrer)
    }
}