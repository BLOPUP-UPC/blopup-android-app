package edu.upc.openmrs.activities.addeditpatient

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import edu.upc.R

class NationalityAdapter(
    context: Context,
    private val resourceId: Int,
    private val nationalities: Array<Nationality>
) : ArrayAdapter<Nationality>(context, resourceId, nationalities) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(resourceId, parent, false)
        }

        val flagImageView = itemView!!.findViewById<ImageView>(R.id.flag_image)
        val countryTextView = itemView.findViewById<TextView>(R.id.country_name)

        val nationality = nationalities[position]
        flagImageView.setImageResource(nationality.flagResId)
        countryTextView.text = nationality.name

        return itemView
    }
}
