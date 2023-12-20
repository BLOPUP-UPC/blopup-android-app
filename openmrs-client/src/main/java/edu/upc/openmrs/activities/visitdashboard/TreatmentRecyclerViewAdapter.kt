package edu.upc.openmrs.activities.visitdashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import edu.upc.R
import edu.upc.sdk.library.models.Treatment
import java.util.Arrays


class TreatmentRecyclerViewAdapter(private val context: Context) :
    RecyclerView.Adapter<TreatmentRecyclerViewAdapter.ViewHolder>() {

    private var treatmentList: List<Treatment> = emptyList()

    fun updateData(newTreatmentList: List<Treatment>) {
        treatmentList = newTreatmentList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.row_patient_treatments, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (_, medicationName, medicationType, notes) = treatmentList[position]

        // Bind data to the views in your CardView
        holder.medicationNameTextView.text = medicationName
        holder.medicationTypeTextView.text = medicationType.map { it.getLabel(context) }.toString().replace("[", "").replace("]", "")
        holder.notesTextView.text = notes
    }

    override fun getItemCount(): Int {
        return treatmentList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var cardView: CardView
        var medicationNameTextView: TextView
        var medicationTypeTextView: TextView
        var notesTextView: TextView

        init {
            cardView = itemView as CardView
            medicationNameTextView = itemView.findViewById(R.id.medication_name)
            medicationTypeTextView = itemView.findViewById(R.id.medication_type)
            notesTextView = itemView.findViewById(R.id.notes)
        }
    }
}