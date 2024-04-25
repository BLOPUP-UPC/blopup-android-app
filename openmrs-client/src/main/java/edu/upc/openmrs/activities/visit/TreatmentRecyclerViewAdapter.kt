package edu.upc.openmrs.activities.visit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import edu.upc.R
import edu.upc.blopup.model.Treatment


class TreatmentRecyclerViewAdapter(
    private val context: Context,
    private val isCurrentVisitActive: Boolean = false,
    private val currentVisitUuid: String? = null,
    private val listener: TreatmentListener? = null
) :
    RecyclerView.Adapter<TreatmentRecyclerViewAdapter.ViewHolder>() {

    private var treatmentList: List<Treatment> = emptyList()

    fun updateData(treatmentList: List<Treatment>) {
        this.treatmentList = treatmentList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.row_patient_treatments, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val treatment = treatmentList[position]

        treatment.doctor?.let {
            val doctorInfo = context.getString(R.string.doctor_info, it.name, it.registrationNumber)
            holder.whoRecommended.visibility = View.VISIBLE
            holder.whoRecommended.text = doctorInfo
        }
        holder.medicationNameTextView.text = treatment.medicationName
        holder.medicationTypeTextView.text =
            treatment.medicationType.map { it.getLabel(context) }.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(",", "  • ")
        holder.notesTextView.text = treatment.notes

        if (isCurrentVisitActive) {
            when (treatment.visitUuid) {
                currentVisitUuid -> {
                    holder.treatmentCardLayout.setOnClickListener {
                        showPopupMenu(
                            it.findViewById(R.id.ellipsis),
                            R.menu.treatments_menu_current_visit,
                            position
                        )
                    }
                }

                else -> {
                    if (!treatment.isActive) {
                        //if treatment is marked as finalise then show the background with opacity
                        holder.treatmentCardLayout.alpha = 0.5f
                        holder.ellipsisTextView.visibility = View.GONE
                    } else {
                        holder.treatmentCardLayout.setOnClickListener {
                            showPopupMenu(
                                it.findViewById(R.id.ellipsis),
                                R.menu.treatments_menu_previous_visit,
                                position
                            )
                        }
                    }
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
                    listener?.onEditClicked(treatmentList[position])
                    return@setOnMenuItemClickListener true
                }

                R.id.action_remove -> {
                    listener?.onRemoveClicked(treatmentList[position])
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
    ) : RecyclerView.ViewHolder(itemView) {
        private var cardView: CardView
        var whoRecommended: TextView
        var medicationNameTextView: TextView
        var medicationTypeTextView: TextView
        var notesTextView: TextView
        var ellipsisTextView: TextView
        var treatmentCardLayout: ConstraintLayout

        init {
            cardView = itemView as CardView
            whoRecommended = itemView.findViewById(R.id.who_recommended)
            medicationNameTextView = itemView.findViewById(R.id.medication_name)
            medicationTypeTextView = itemView.findViewById(R.id.medication_type)
            notesTextView = itemView.findViewById(R.id.notes)
            ellipsisTextView = itemView.findViewById(R.id.ellipsis)
            treatmentCardLayout = itemView.findViewById(R.id.treatment_card_layout)
        }
    }
}