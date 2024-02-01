/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package edu.upc.openmrs.activities.introduction

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import edu.upc.R
import edu.upc.databinding.ActivitySplashBinding
import edu.upc.openmrs.utilities.LanguageUtils
import edu.upc.sdk.utilities.ApplicationConstants

class SplashActivity : edu.upc.openmrs.activities.ACBaseActivity() {

    private val mHandler = Handler()
    private lateinit var mRunnable: Runnable
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LanguageUtils.setAppToDeviceLanguage(this)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val move = AnimationUtils.loadAnimation(applicationContext, R.anim.splash_screen_logo_anim)
        val set = AnimationSet(true)
        val fadeIn: Animation = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = AccelerateInterpolator()
        fadeIn.duration = 1000
        set.addAnimation(fadeIn)
        set.addAnimation(move)
        binding.logo.startAnimation(set)
        mRunnable = Runnable {
            val intent = Intent(this@SplashActivity, IntroActivity::class.java)
            startActivity(intent)
            finish()
        }
        mHandler.postDelayed(mRunnable, ApplicationConstants.SPLASH_TIMER.toLong())
    }

    override fun onDestroy() {
        mHandler.removeCallbacks(mRunnable)
        super.onDestroy()
    }
}