package edu.upc.openmrs.activities.patientdashboard.charts

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import edu.upc.R
import java.time.LocalDate


class TreatmentsListExpandableListAdapter(
    val layoutInflater: LayoutInflater,
    private val expandableListTitle: List<LocalDate>,
    private val expandableListDetail: HashMap<LocalDate, List<Treatment>>
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Treatment {
        return this.expandableListDetail[this.expandableListTitle[listPosition]]!![expandedListPosition]
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
        expandedListNameView.text = treatment.name
        expandedListTypesView.text = treatment.medicationTypeToString()

        adherenceIcon.setImageResource(treatment.adherenceIcon())

        return view
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return expandableListDetail[expandableListTitle[listPosition]]?.size ?: 0
    }

    override fun getGroup(listPosition: Int): String {
        return expandableListTitle[listPosition].toString()
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
        listTitleTextView.text = getGroup(listPosition)

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
}