package edu.upc.blopup.tensiometer.readTensiometerMeasurement

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import edu.upc.databinding.ActivityTensiometerBinding


class TensiometerActivity : AppCompatActivity(){

    private lateinit var mBinding: ActivityTensiometerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityTensiometerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val adapter = MyPagerAdapter()
        val myPager = mBinding.viewPagerTensiometer
        myPager.adapter = adapter
        myPager.currentItem = 2
    }
}