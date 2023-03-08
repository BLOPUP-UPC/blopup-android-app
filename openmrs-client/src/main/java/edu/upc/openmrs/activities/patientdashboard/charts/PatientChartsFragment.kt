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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.collect.Lists
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ToastUtil
import edu.upc.sdk.utilities.ToastUtil.showShortToast
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.FragmentPatientChartsBinding
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import kotlinx.android.synthetic.main.fragment_patient_charts.vitalEmpty
import kotlinx.android.synthetic.main.fragment_patient_charts.vitalList
import org.json.JSONException
import org.json.JSONObject

@AndroidEntryPoint
class PatientChartsFragment : edu.upc.openmrs.activities.BaseFragment(), edu.upc.openmrs.activities.patientdashboard.charts.PatientChartsRecyclerViewAdapter.OnClickListener {
    private var _binding: FragmentPatientChartsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PatientDashboardChartsViewModel by viewModels()

    private var observationList: JSONObject? = null

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
                edu.upc.openmrs.activities.patientdashboard.charts.PatientChartsRecyclerViewAdapter(
                    activity,
                    JSONObject(),
                    this@PatientChartsFragment
                )
        }
    }

    private fun setupObserver() {
        viewModel.result.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> {
                }
                is Result.Success -> {
                    observationList = result.data
                    showChartsList(result.data)
                }
                is Result.Error -> {
                }
                else -> {
                }
            }
        })
    }

    private fun fetchChartsData() {
        viewModel.fetchChartsData()
    }

    private fun showChartsList(observationList: JSONObject) {
        with(binding) {
            vitalEmpty.makeGone()
            vitalList.makeVisible()
            (vitalList.adapter as edu.upc.openmrs.activities.patientdashboard.charts.PatientChartsRecyclerViewAdapter).updateList(observationList)
        }
    }

    private fun showEmptyList(visibility: Boolean) {
        vitalEmpty.makeVisible()
        vitalList.makeGone()
    }

    override fun showChartActivity(vitalName: String) {
        try {
            val chartData = observationList!!.getJSONObject(vitalName)
            val dates = chartData.keys()
            val dateList = Lists.newArrayList(dates)
            if (dateList.size == 0) showShortToast(requireContext(), ToastUtil.ToastType.ERROR, getString(R.string.data_not_available_for_this_field)) else {
                val dataArray = chartData.getJSONArray(dateList[0])
                val entry = dataArray[0] as String
                try {
                    val entryValue = entry.toFloat()
                    Intent(activity, edu.upc.openmrs.activities.patientdashboard.charts.ChartsViewActivity::class.java).apply {
                        val bundle = Bundle().apply { putString("vitalName", chartData.toString()) }
                        putExtra("bundle", bundle)
                        startActivity(this)
                    }
                } catch (e: NumberFormatException) {
                    showShortToast(requireContext(), ToastUtil.ToastType.ERROR, getString(R.string.data_type_not_available_for_this_field))
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            showShortToast(requireContext(), ToastUtil.ToastType.ERROR, e.message!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(patientId: String): PatientChartsFragment {
            val fragment = PatientChartsFragment()
            fragment.arguments = bundleOf(Pair(PATIENT_ID_BUNDLE, patientId))
            return fragment
        }
    }
}
