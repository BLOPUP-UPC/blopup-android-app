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
package edu.upc.openmrs.activities.patientdashboard.charts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.FragmentPatientChartsBinding
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ToastUtil
import edu.upc.sdk.utilities.ToastUtil.showShortToast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@AndroidEntryPoint
class PatientChartsFragment : edu.upc.openmrs.activities.BaseFragment(), PatientChartsRecyclerViewAdapter.OnClickListener {
    private var _binding: FragmentPatientChartsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PatientDashboardChartsViewModel by viewModels()

    private var observationList: JSONObject? = null

    private val patientId: Int by lazy {
        requireArguments().getString(PATIENT_ID_BUNDLE)!!.toInt()
    }
    private val patientUuud: String by lazy {
        requireArguments().getString(ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPatientChartsBinding.inflate(inflater, container, false)

        setupAdapter()
        setupObserver()
        fetchChartsData()

        return binding.root
    }

    private fun setupAdapter() {
        with(binding.vitalList) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter =
                PatientChartsRecyclerViewAdapter(
                    activity,
                    JSONObject(),
                    this@PatientChartsFragment
                )
        }
    }

    private fun setupObserver() {
        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                }

                is Result.Success -> {
                    observationList = result.data
                    showChartsList()
                }

                is Result.Error -> {
                }

                else -> {
                }
            }
        }
    }

    private fun fetchChartsData() {
        viewModel.fetchChartsData()
    }

    private fun showChartsList() {
        with(binding) {
            vitalEmpty.makeGone()
            vitalList.makeVisible()
            (vitalList.adapter as PatientChartsRecyclerViewAdapter).updateList()
        }
    }

    override fun showChartActivity(vitalName: String) {
        try {
            val systolicData = observationList!!.getJSONObject("Systolic blood pressure")
            val diastolicData = observationList!!.getJSONObject("Diastolic blood pressure")

            val map = HashMap<String, Pair<Float, Float>>()
            val dates = systolicData.keys()
            for (key in dates) {
                map.put(key, Pair(((systolicData.get(key) as JSONArray).get(0) as String).toFloat(), ((diastolicData.get(key) as JSONArray).get(0) as String).toFloat()))
            }

            try{
                Intent(
                    activity,
                    ChartsViewActivity::class.java
                ).apply {
                    val bundle = Bundle().apply {
                        putSerializable(ChartsViewActivity.BLOOD_PRESSURE, map)
                        putInt(ChartsViewActivity.PATIENT_ID, patientId)
                        putString(ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE, patientUuud)
                    }
                    putExtra(ApplicationConstants.BUNDLE, bundle)
                    startActivity(this)
                }
            } catch (e: NumberFormatException) {
                showShortToast(requireContext(), ToastUtil.ToastType.ERROR, getString(R.string.data_type_not_available_for_this_field))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            showShortToast(requireContext(), ToastUtil.ToastType.ERROR, getString(R.string.patient_with_no_values_to_show))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        fetchChartsData()
    }

    companion object {
        fun newInstance(patientId: String, patientUuid: String): PatientChartsFragment {
            val fragment = PatientChartsFragment()
            fragment.arguments = bundleOf(
                Pair(PATIENT_ID_BUNDLE, patientId),
                Pair(ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE, patientUuid)
            )
            return fragment
        }
    }
}
