package edu.upc.openmrs.activities.patientdashboard.charts

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import edu.upc.R
import edu.upc.sdk.utilities.DateUtils.formatAsDate
import java.time.LocalDate


class TreatmentsListExpandableListAdapter(
    private val layoutInflater: LayoutInflater,
    private val expandableListTitle: List<LocalDate>,
    private val expandableListDetail: Map<LocalDate, List<TreatmentAdherence>>
) : BaseExpandableListAdapter() {
    override fun getChild(listPosition: Int, expandedListPosition: Int): TreatmentAdherence {
        return this.expandableListDetail[this.expandableListTitle[listPosition]]?.get(expandedListPosition) ?: TreatmentAdherence("", emptySet(), false, LocalDate.now())
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(
        listPosition: Int, expandedListPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup?
    ): View {
        val view = convertView ?: layoutInflater.inflate(R.layout.chart_layout_treatment_item, null)
        val expandedListNameView = view.findViewById<TextView>(R.id.treatmentItem)
        val expandedListTypesView = view.findViewById<TextView>(R.id.treatmentItemTypes)
        val adherenceIcon = view.findViewById<ImageView>(R.id.adherence_icon)

        val treatment = getChild(listPosition, expandedListPosition)
        expandedListNameView.text = treatment.medicationName
        expandedListTypesView.text = treatment.medicationTypeToString(layoutInflater.context)

        adherenceIcon.setImageResource(treatment.icon())

        return view
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return expandableListDetail[expandableListTitle[listPosition]]?.size ?: 0
    }

    override fun getGroup(listPosition: Int): LocalDate {
        return expandableListTitle[listPosition]
    }

    override fun getGroupCount(): Int {
        return expandableListTitle.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup?
    ): View? {
        val view =
            convertView ?: layoutInflater.inflate(R.layout.chart_layout_treatments_group, null)
        val listTitleTextView = view.findViewById<View>(R.id.treatmentListTitle) as TextView
        listTitleTextView.setTypeface(null, Typeface.BOLD)
        listTitleTextView.text = getGroup(listPosition).formatAsDate()

        val iconView = view.findViewById<ImageView>(R.id.group_indicator)

        if (isExpanded)
            iconView.setImageResource(R.drawable.icon_arrow_up)
        else
            iconView.setImageResource(R.drawable.icon_arrow_down)

        return view
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return false
    }

    fun getTreatmentIdToExpand(date: LocalDate) : Int {
        return expandableListTitle.indexOfFirst { it == date }
    }
}