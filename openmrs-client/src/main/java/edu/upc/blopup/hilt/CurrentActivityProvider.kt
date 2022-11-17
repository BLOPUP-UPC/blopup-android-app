package edu.upc.blopup.hilt

import android.app.Activity
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.internal.GeneratedComponentManagerHolder
import edu.upc.blopup.scale.showScaleMeasurement.ShowScaleMeasurementActivity
import javax.inject.Inject

@ActivityRetainedScoped
class CurrentActivityProvider @Inject constructor() {

    private var currentActivity: Activity? = null

    companion object {
        private fun Activity.withProvider(
            block: CurrentActivityProvider.() -> Unit
        ) {
            if (this is GeneratedComponentManagerHolder) {
                val entryPoint: ActivityProviderEntryPoint = EntryPointAccessors.fromActivity(this, ActivityProviderEntryPoint::class.java)
                val provider = entryPoint.activityProvider
                provider.block()
            }
        }

        fun onActivityCreated(activity: Activity) {
            activity.withProvider {
                currentActivity = activity
            }
        }

        fun onActivityDestroyed(activity: Activity) {
            activity.withProvider {
                if (currentActivity === activity) {
                    currentActivity = null
                }
            }
        }
    }

    fun <T> withActivity(block: Activity.() -> T): T {
//        checkMainThread()
        val activity = currentActivity
        check(activity != null) {
            "Don't call this after the activity is finished!"
        }
        return activity.block()
    }
}