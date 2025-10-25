package com.tods.project_olx.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) {

    // Screen tracking
    fun logScreenView(screenName: String, screenClass: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // Ad events
    fun logAdViewed(adId: String, adTitle: String, category: String) {
        val bundle = Bundle().apply {
            putString("ad_id", adId)
            putString("ad_title", adTitle)
            putString("category", category)
        }
        analytics.logEvent("ad_viewed", bundle)
    }

    fun logAdCreated(adId: String, category: String) {
        val bundle = Bundle().apply {
            putString("ad_id", adId)
            putString("category", category)
        }
        analytics.logEvent("ad_created", bundle)
    }

    fun logAdDeleted(adId: String) {
        val bundle = Bundle().apply {
            putString("ad_id", adId)
        }
        analytics.logEvent("ad_deleted", bundle)
    }

    fun logCallButtonClicked(adId: String) {
        val bundle = Bundle().apply {
            putString("ad_id", adId)
        }
        analytics.logEvent("call_button_clicked", bundle)
    }

    // User events
    fun logUserLogin(userId: String, method: String = "email") {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
        crashlytics.setUserId(userId)
    }

    fun logUserSignUp(method: String = "email") {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
    }

    // Search events
    fun logSearch(query: String, resultCount: Int) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
            putInt("result_count", resultCount)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
    }

    fun logFilterApplied(filterType: String, filterValue: String) {
        val bundle = Bundle().apply {
            putString("filter_type", filterType)
            putString("filter_value", filterValue)
        }
        analytics.logEvent("filter_applied", bundle)
    }

    // Error tracking
    fun logError(error: Throwable, context: String) {
        crashlytics.recordException(error)
        crashlytics.log("Error in $context: ${error.message}")
    }

    fun logNonFatal(message: String) {
        crashlytics.log(message)
    }

    // Custom events
    fun logCustomEvent(eventName: String, params: Map<String, Any>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        }
        analytics.logEvent(eventName, bundle)
    }

    // User properties
    fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }
}
