package edu.upc.openmrs.activities.addeditpatient

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import edu.upc.R

class NationalityAdapter(
    context: Context,
    private val resourceId: Int,
    private val nationalities: List<Nationality>
) : ArrayAdapter<Nationality>(context, resourceId, nationalities), Filterable {

    private var filteredList: MutableList<Nationality> = nationalities.toMutableList()

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                if (constraint.isBlank()) {
                    results.values = nationalities
                    results.count = nationalities.size
                } else {
                    val filteredNames = nationalities.filter { nationality ->
                        nationality.name.contains(constraint, true)
                    }
                    results.values = filteredNames
                    results.count = filteredNames.size
                }
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filteredList.clear()
                filteredList.addAll(results.values as List<Nationality>)
                notifyDataSetChanged()
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent, filteredList)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup, data: List<Nationality>): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(resourceId, parent, false)
        }

        val flagImageView = itemView!!.findViewById<ImageView>(R.id.flag_image)
        val countryTextView = itemView.findViewById<TextView>(R.id.country_name)

        val nationality = data[position]
        flagImageView.setImageResource(nationality.flagResId)
        countryTextView.text = nationality.name

        return itemView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent, filteredList)
    }
}