package com.tods.project_olx.di

// Ensure these imports are correct and unambiguous
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tods.project_olx.analytics.AnalyticsManager // Check this path carefully
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAnalyticsManager(
        firebaseAnalytics: FirebaseAnalytics, // Provided by AnalyticsModule
        firebaseCrashlytics: FirebaseCrashlytics // Provided by AnalyticsModule
    ): AnalyticsManager {
        // Pass dependencies in the correct order/name as expected by AnalyticsManager's constructor
        return AnalyticsManager(
            analytics = firebaseAnalytics,
            crashlytics = firebaseCrashlytics
        )
    }

}

