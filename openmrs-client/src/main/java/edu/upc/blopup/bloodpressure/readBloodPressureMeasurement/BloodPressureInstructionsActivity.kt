package edu.upc.blopup.bloodpressure.readBloodPressureMeasurement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.upc.databinding.ActivityBloodPressureBinding


class BloodPressureInstructionsActivity : AppCompatActivity(){

    private lateinit var mBinding: ActivityBloodPressureBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityBloodPressureBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val adapter = BloodPressureInstructionsAdapter()
        val myPager = mBinding.viewPagerBloodPressure
        myPager.adapter = adapter
        myPager.currentItem = 2
    }
}