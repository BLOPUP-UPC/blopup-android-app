package edu.upc.blopup.vitalsform

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.upc.R
import edu.upc.sdk.library.models.Treatment

class TreatmentAdherenceRecyclerViewAdapter(private val context: Context) :
    RecyclerView.Adapter<TreatmentAdherenceRecyclerViewAdapter.ViewHolder>() {

    private var activeTreatments = emptyList<Treatment>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.row_treatment_adherence, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (_, medicationName, medicationType) = activeTreatments[position]

        holder.medicationNameTextView.text = medicationName
        holder.medicationTypeTextView.text = medicationType.map { it.getLabel(context) }.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(",", "  • ")
    }

    override fun getItemCount() = activeTreatments.size

    fun updateData(newList: List<Treatment>) {
        activeTreatments = newList
        this.notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var medicationNameTextView: TextView
        var medicationTypeTextView: TextView
        init {
            medicationNameTextView = itemView.findViewById(R.id.medication_name)
            medicationTypeTextView = itemView.findViewById(R.id.medication_type)
        }
    }

}