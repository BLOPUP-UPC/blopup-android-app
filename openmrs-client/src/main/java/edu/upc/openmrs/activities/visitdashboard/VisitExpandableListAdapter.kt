/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package edu.upc.openmrs.activities.visitdashboard

import android.content.Context
import android.graphics.Bitmap
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import edu.upc.R
import edu.upc.blopup.bloodpressure.bloodPressureTypeFromEncounter
import edu.upc.openmrs.application.OpenMRSInflater
import edu.upc.openmrs.utilities.EncounterTranslationUtils.getTranslatedResourceId
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ImageUtils.decodeBitmapFromResource

class VisitExpandableListAdapter(
    private val mContext: Context,
    private var mEncounters: List<Encounter>,
    private val fragmentManager: FragmentManager
) :
    BaseExpandableListAdapter() {
    private var mChildLayouts: List<ViewGroup>
    private val mBitmapCache: SparseArray<Bitmap?>
    private val bmiCalculator: BMICalculator

    init {
        mBitmapCache = SparseArray()
        mChildLayouts = generateChildLayouts()
        bmiCalculator = BMICalculator()
    }

    fun updateList(encounters: List<Encounter>) {
        mEncounters = encounters.sortedBy { it.encounterDatetime }.reversed()
        notifyDataSetChanged()
    }

    private fun generateChildLayouts(): List<ViewGroup> {
        val layouts: MutableList<ViewGroup> = ArrayList()
        val inflater = LayoutInflater.from(mContext)
        val openMRSInflater = OpenMRSInflater(inflater)
        for (encounter in mEncounters) {
            val convertView = inflater.inflate(R.layout.list_visit_item, null) as ViewGroup
            val contentLayout =
                convertView.findViewById<LinearLayout>(R.id.listVisitItemLayoutContent)

            when (encounter.encounterType!!.display) {

                EncounterType.VITALS -> {
                    val bmiData = bmiCalculator.execute(encounter.observations)

                    openMRSInflater.addVitalsData(
                        contentLayout,
                        encounter.observations,
                        bmiData,
                        bloodPressureTypeFromEncounter(encounter),
                        fragmentManager
                    )

                    layouts.add(convertView)
                }

                EncounterType.VISIT_NOTE -> {
                    for (obs in encounter.observations) {
                        //checking the type of observation, to extract the relevant data from it to add to the layout
                        if (obs.diagnosisNote != null && obs.diagnosisNote != ApplicationConstants.EMPTY_STRING) {
                            //if the observation is a Diagnosis Note, i.e. it contains a value for diagnosisNote
                            openMRSInflater.addKeyValueStringView(
                                contentLayout,
                                mContext.getString(R.string.treatment_label),
                                obs.diagnosisNote
                            )
                        } else if (obs.diagnosisOrder != null && obs.shortDiagnosisCertainty != null && obs.diagnosisList != null) {
                            //if the observation is a Diagnosis Order
                            openMRSInflater.addKeyValueStringView(
                                contentLayout, obs.diagnosisOrder,
                                "(" + obs.shortDiagnosisCertainty + ") " + obs.diagnosisList
                            )
                        } else if (obs.display != null && obs.displayValue != null) {
                            if (obs.display!!.contains(mContext.getString(R.string.hiv_yes))) {
                                openMRSInflater.addKeyValueStringView(
                                    contentLayout, obs.display,
                                    mContext.getString(R.string.hiv_yes)
                                )
                            } else if (obs.display!!.contains(mContext.getString(R.string.hiv_no))) {
                                openMRSInflater.addKeyValueStringView(
                                    contentLayout, obs.display,
                                    mContext.getString(R.string.hiv_no)
                                )
                            } else if (obs.display!!.contains(mContext.getString(R.string.hiv_unknown))) {
                                openMRSInflater.addKeyValueStringView(
                                    contentLayout, obs.display,
                                    mContext.getString(R.string.hiv_unknown)
                                )
                            } else {
                                //miscellaneous, for all other cases that have a Display - Value pair
                                openMRSInflater.addKeyValueStringView(
                                    contentLayout,
                                    mContext.getString(R.string.treatment_label),
                                    obs.displayValue
                                )
                            }
                        }
                    }
                    for (diagnosis in encounter.diagnoses) {
                        openMRSInflater.addKeyValueStringView(
                            contentLayout,
                            mContext.getString(R.string.diagnosis_label),
                            diagnosis.display
                        )
                    }
                    layouts.add(convertView)
                }

                EncounterType.DISCHARGE -> {
                    openMRSInflater.addSingleStringView(
                        contentLayout,
                        mContext.getString(
                            R.string.discharge_location_in_list,
                            encounter.location!!.display
                        )
                    )
                    layouts.add(convertView)
                }

                EncounterType.ADMISSION -> {
                    openMRSInflater.addSingleStringView(
                        contentLayout,
                        mContext.getString(
                            R.string.admission_location_in_list,
                            encounter.location!!.display
                        )
                    )
                    layouts.add(convertView)
                }

                else -> {}
            }
        }
        return layouts
    }

    override fun getGroupCount(): Int {
        return mEncounters.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return 1
    }

    override fun getGroup(groupPosition: Int): Any {
        return mEncounters[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return mChildLayouts[groupPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val rowView: View = if (null == convertView) {
            val inflater =
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.list_visit_group, null)
        } else {
            convertView
        }
        val encounterName = rowView.findViewById<TextView>(R.id.listVisitGroupEncounterName)
        val detailsSelector = rowView.findViewById<TextView>(R.id.listVisitGroupDetailsSelector)
        val encounter = mEncounters[groupPosition]
        val encounterNameString = encounter.encounterType!!.display
        val translatedEncounterDisplay = getTranslatedResourceId(
            encounterNameString!!
        )
        val translatedEncounter = mContext.getString(translatedEncounterDisplay)
        encounterName.text = translatedEncounter
        if (isExpanded) {
            detailsSelector.text = mContext.getString(R.string.list_visit_selector_hide)
            bindDrawableResources(R.drawable.exp_list_hide_details, detailsSelector, RIGHT)
        } else {
            detailsSelector.text = mContext.getString(R.string.list_visit_selector_show)
            bindDrawableResources(R.drawable.exp_list_show_details, detailsSelector, RIGHT)
        }
        when (encounter.encounterType!!.display) {
            EncounterType.VITALS -> bindDrawableResources(
                R.drawable.ico_vitals_small,
                encounterName,
                LEFT
            )

            EncounterType.VISIT_NOTE -> bindDrawableResources(
                R.drawable.visit_note,
                encounterName,
                LEFT
            )

            else -> {}
        }
        return rowView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        return getChild(groupPosition, childPosition) as ViewGroup
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun notifyDataSetChanged() {
        mChildLayouts = generateChildLayouts()
        super.notifyDataSetChanged()
    }

    private fun bindDrawableResources(drawableID: Int, textView: TextView, direction: Int) {
        val scale = mContext.resources.displayMetrics.density
        val image = mContext.resources.getDrawable(drawableID)
        if (direction == LEFT) {
            image.setBounds(0, 0, (40 * scale + 0.5f).toInt(), (40 * scale + 0.5f).toInt())
            textView.compoundDrawablePadding = (13 * scale + 0.5f).toInt()
            textView.setCompoundDrawables(image, null, null, null)
        } else {
            image.setBounds(0, 0, image.intrinsicWidth, image.intrinsicHeight)
            textView.compoundDrawablePadding = (10 * scale + 0.5f).toInt()
            textView.setCompoundDrawables(null, null, image, null)
        }
    }

    private fun createImageBitmap(key: Int, layoutParams: ViewGroup.LayoutParams) {
        if (mBitmapCache[key] == null) {
            mBitmapCache.put(
                key, decodeBitmapFromResource(
                    mContext.resources, key,
                    layoutParams.width, layoutParams.height
                )
            )
        }
    }

    companion object {
        private const val LEFT = 0
        private const val RIGHT = 1
    }
}