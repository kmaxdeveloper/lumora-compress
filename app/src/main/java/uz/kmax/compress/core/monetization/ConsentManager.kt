package uz.kmax.compress.core.monetization

import android.app.Activity
import android.content.Context
import com.google.android.ump.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

    interface OnConsentCheckListener {
        fun onConsentRequired()
        fun onConsentNotRequired()
        fun onError(error: String)
    }

    fun gatherConsent(activity: Activity, listener: OnConsentCheckListener) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        listener.onError(formError.message)
                    } else if (canRequestAds()) {
                        listener.onConsentNotRequired()
                    }
                }
            },
            { requestConsentError ->
                listener.onError(requestConsentError.message)
            }
        )
    }

    fun canRequestAds(): Boolean {
        return consentInformation.canRequestAds()
    }

    fun isPrivacyOptionsRequired(): Boolean {
        return consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun showPrivacyOptionsForm(activity: Activity, onDismissListener: ConsentForm.OnConsentFormDismissedListener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onDismissListener)
    }
}
