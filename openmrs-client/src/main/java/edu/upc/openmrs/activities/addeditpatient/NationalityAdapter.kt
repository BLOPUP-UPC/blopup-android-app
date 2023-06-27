package edu.upc.openmrs.activities.addeditpatient

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import edu.upc.R
import java.util.*

class NationalityAdapter(
    context: Context,
    resource: Int,
    nationalityList: List<Nationality>
) : ArrayAdapter<Nationality>(context, resource, nationalityList) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var originalList: List<Nationality> = nationalityList
    var filteredList: List<Nationality> = nationalityList

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.item_nationality, parent, false)
            holder = ViewHolder()
            holder.imageViewFlag = view.findViewById(R.id.flag_image)
            holder.textViewNationality = view.findViewById(R.id.country_name)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val nationality = getItem(position)
        holder.textViewNationality?.text = nationality.getLabel(context)
        nationality.flag.let { holder.imageViewFlag?.setImageResource(it) }

        return view
    }

    private class ViewHolder {
        var imageViewFlag: ImageView? = null
        var textViewNationality: TextView? = null
    }

    override fun getCount(): Int {
        return filteredList.size
    }

    override fun getItem(position: Int): Nationality {
        return filteredList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(text: CharSequence): FilterResults {
                val filteredResults = FilterResults()
                val searchText = text.toString().lowercase(Locale.getDefault())

                filteredList = if (searchText.isNotEmpty()) {
                    originalList.filter { nationality ->
                        nationality.getLabel(context).lowercase(Locale.getDefault()).contains(searchText)
                    }
                } else {
                    originalList
                }

                filteredResults.values = filteredList
                filteredResults.count = filteredList.size

                return filteredResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filteredList = results.values as List<Nationality>
                notifyDataSetChanged()
            }
        }
    }
}
