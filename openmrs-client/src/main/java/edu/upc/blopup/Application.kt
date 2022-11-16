package edu.upc.blopup

import android.app.Activity
import android.app.Application
import android.os.Bundle
import blopup.upc.edu.microlife.hilt.CurrentActivityProvider
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MicrolifeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) {
                CurrentActivityProvider.onActivityCreated(activity)
            }

            override fun onActivityStarted(p0: Activity) {
            }

            override fun onActivityResumed(p0: Activity) {
            }

            override fun onActivityPaused(p0: Activity) {
            }

            override fun onActivityStopped(p0: Activity) {
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                CurrentActivityProvider.onActivityDestroyed(activity)
            }
        })
    }
}