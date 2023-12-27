package edu.upc.openmrs.activities.visitdashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import edu.upc.R
import edu.upc.sdk.library.models.Treatment


class TreatmentRecyclerViewAdapter(private val context: Context, private val showEllipsis: Boolean) :
    RecyclerView.Adapter<TreatmentRecyclerViewAdapter.ViewHolder>() {

    private var treatmentList: List<Treatment> = emptyList()

    fun updateData(newTreatmentList: List<Treatment>) {
        treatmentList = newTreatmentList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.row_patient_treatments, parent, false)
        return ViewHolder(view, showEllipsis)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (_, medicationName, medicationType, notes) = treatmentList[position]

        // Bind data to the views in your CardView
        holder.medicationNameTextView.text = medicationName
        holder.medicationTypeTextView.text =
            medicationType.map { it.getLabel(context) }.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(",", "  • ")
        holder.notesTextView.text = notes
    }

    override fun getItemCount(): Int {
        return treatmentList.size
    }

    class ViewHolder(itemView: View, private val showEllipsis: Boolean) : RecyclerView.ViewHolder(itemView) {
        private var cardView: CardView
        var medicationNameTextView: TextView
        var medicationTypeTextView: TextView
        var notesTextView: TextView
        private var ellipsisTextView: TextView

        init {
            cardView = itemView as CardView
            medicationNameTextView = itemView.findViewById(R.id.medication_name)
            medicationTypeTextView = itemView.findViewById(R.id.medication_type)
            notesTextView = itemView.findViewById(R.id.notes)
            ellipsisTextView = itemView.findViewById(R.id.ellipsis)

            if (showEllipsis) {
                ellipsisTextView.visibility = View.VISIBLE
                ellipsisTextView.setOnClickListener {
                    showPopupMenu(it)
                }
            } else {
                ellipsisTextView.visibility = View.GONE
            }
        }

        private fun showPopupMenu(view: View) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.treatments_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_finalise -> {
                        return@setOnMenuItemClickListener true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }
}