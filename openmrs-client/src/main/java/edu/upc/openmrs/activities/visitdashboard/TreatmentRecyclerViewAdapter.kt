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


class TreatmentRecyclerViewAdapter(
    private val context: Context,
    private val visit: Pair<Boolean, String>,
    private val listener: TreatmentListener?
) :
    RecyclerView.Adapter<TreatmentRecyclerViewAdapter.ViewHolder>() {

    private var treatmentList: List<Treatment> = emptyList()

    fun updateData(newTreatmentList: List<Treatment>) {
        treatmentList = newTreatmentList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.row_patient_treatments, parent, false)
        return ViewHolder(view, visit)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (_, medicationName, medicationType, notes, _, _, visitUuid) = treatmentList[position]

        // Bind data to the views in your CardView
        holder.medicationNameTextView.text = medicationName
        holder.medicationTypeTextView.text =
            medicationType.map { it.getLabel(context) }.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(",", "  • ")
        holder.notesTextView.text = notes

        if(holder.visit.first) {
            when {
                visitUuid == visit.second -> {
                    holder.ellipsisTextView.setOnClickListener { showPopupMenu(it, R.menu.treatments_menu_current_visit, position) }
                }
                visit.second.isEmpty() -> {
                    holder.ellipsisTextView.visibility = View.GONE
                }
                else -> {
                    holder.ellipsisTextView.setOnClickListener { showPopupMenu(
                        it,
                        R.menu.treatments_menu_previous_visit,
                        position
                    ) }
                }
            }
        } else {
            holder.ellipsisTextView.visibility = View.GONE
        }
    }

    private fun showPopupMenu(
        view: View,
        menuResId: Int,
        position: Int
    ) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(menuResId, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_finalise -> {
                    listener?.onFinaliseClicked(treatmentList[position])
                    return@setOnMenuItemClickListener true
                }
                R.id.action_edit -> {
                    return@setOnMenuItemClickListener true
                }
                R.id.action_remove -> {
                    return@setOnMenuItemClickListener true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun getItemCount(): Int {
        return treatmentList.size
    }

    class ViewHolder(
        itemView: View,
        val visit: Pair<Boolean, String>
    ) : RecyclerView.ViewHolder(itemView) {
        private var cardView: CardView
        var medicationNameTextView: TextView
        var medicationTypeTextView: TextView
        var notesTextView: TextView
        var ellipsisTextView: TextView

        init {
            cardView = itemView as CardView
            medicationNameTextView = itemView.findViewById(R.id.medication_name)
            medicationTypeTextView = itemView.findViewById(R.id.medication_type)
            notesTextView = itemView.findViewById(R.id.notes)
            ellipsisTextView = itemView.findViewById(R.id.ellipsis)
        }
    }
}