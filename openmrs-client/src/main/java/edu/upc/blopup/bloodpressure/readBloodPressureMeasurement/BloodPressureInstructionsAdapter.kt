package edu.upc.blopup.bloodpressure.readBloodPressureMeasurement

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class BloodPressureInstructionsAdapter : PagerAdapter() {
    override fun getCount(): Int {
        return 2
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val inflater = collection.context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var viewId = 0
        when (position) {
            0 -> viewId = edu.upc.R.layout.activity_read_blood_pressure
            1 -> viewId = edu.upc.R.layout.activity_blood_pressure_instructions
        }
        val view: View = inflater.inflate(viewId, null)
        (collection as ViewPager).addView(view, 0)
        return view
    }

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1 as View
    }

    override fun saveState(): Parcelable? {
        return null
    }
}
