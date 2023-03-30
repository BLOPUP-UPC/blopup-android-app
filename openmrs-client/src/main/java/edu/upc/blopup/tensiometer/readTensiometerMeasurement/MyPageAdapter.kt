package edu.upc.blopup.tensiometer.readTensiometerMeasurement

import android.R
import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager


class MyPagerAdapter : PagerAdapter() {
    override fun getCount(): Int {
        return 2
    }

    override fun instantiateItem(collection: View, position: Int): Any {
        val inflater = collection.getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var resId = 0
        when (position) {
            0 -> resId = edu.upc.R.layout.activity_read_tensiometer
            1 -> resId = edu.upc.R.layout.activity_tensiometer_instructions
        }
        val view: View = inflater.inflate(resId, null)
        (collection as ViewPager).addView(view, 0)
        return view
    }

//    fun destroyItem(arg0: View, arg1: Int, arg2: Any?) {
//        (arg0 as ViewPager).removeView(arg2 as View?)
//    }

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1 as View
    }

    override fun saveState(): Parcelable? {
        return null
    }
}