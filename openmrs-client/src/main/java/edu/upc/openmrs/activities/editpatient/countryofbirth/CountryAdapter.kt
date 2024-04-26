package edu.upc.openmrs.activities.editpatient.countryofbirth

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import edu.upc.R
import java.util.Locale

class CountryAdapter(
    context: Context,
    resource: Int,
    countryList: Array<Country>
) : ArrayAdapter<Country>(context, resource, countryList) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var originalList: Array<Country> = countryList.sortedBy { it.getLabel(context) }.toTypedArray()
    var filteredList: Array<Country> = countryList.sortedBy { it.getLabel(context) }.toTypedArray()


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.item_country, parent, false)
            holder = ViewHolder()
            holder.imageViewFlag = view.findViewById(R.id.flag_image)
            holder.textViewCountry = view.findViewById(R.id.country_name)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val country = getItem(position)
        holder.textViewCountry?.text = country.getLabel(context)
        country.flag.let { holder.imageViewFlag?.setImageResource(it) }

        return view
    }

    private class ViewHolder {
        var imageViewFlag: ImageView? = null
        var textViewCountry: TextView? = null
    }

    override fun getCount(): Int {
        return filteredList.size
    }

    override fun getItem(position: Int): Country {
        return filteredList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(text: CharSequence): FilterResults {
                val filteredResults = FilterResults()
                val searchText = text.toString().lowercase(Locale.getDefault())

                filteredList = if (searchText.isNotEmpty()) {
                    originalList.filter { country ->
                        country.getLabel(context).lowercase(Locale.getDefault()).contains(searchText)
                    }.toTypedArray()
                } else {
                    originalList
                }

                filteredResults.values = filteredList
                filteredResults.count = filteredList.size

                return filteredResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filteredList = results.values as Array<Country>
                notifyDataSetChanged()
            }
        }
    }
}
