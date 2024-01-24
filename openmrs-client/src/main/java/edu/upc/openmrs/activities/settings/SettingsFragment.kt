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
package edu.upc.openmrs.activities.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.FragmentSettingsBinding
import edu.upc.openmrs.activities.BaseFragment
import edu.upc.openmrs.activities.community.contact.ContactUsActivity
import edu.upc.sdk.utilities.ApplicationConstants.OpenMRSlanguage.LANGUAGE_LIST

@AndroidEntryPoint
class SettingsFragment :  BaseFragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        addBuildVersionInfo()
        addPrivacyPolicyInfo()
        rateUs()
        setupContactUsButton()
        setupLanguageSpinner()

        updateLanguageView()
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireActivity())
    }

    private fun updateLanguageView() = with(binding) {
        languageApplyButton.setOnClickListener { requireActivity().recreate() }
    }

    private fun addBuildVersionInfo() {
        with(binding) {
            appNameTextView.text = getString(R.string.app_name)
            versionTextView.text = viewModel.getBuildVersionInfo(requireContext())
        }
    }

    private fun addPrivacyPolicyInfo() {
        binding.privacyPolicyLayout.setOnClickListener {
            Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(getString(R.string.url_privacy_policy)))
                .let { startActivity(it) }
        }
    }

    private fun rateUs() {
        binding.rateUsLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, viewModel.appMarketUri)
            // Ignore Play Store back stack, on back press will take us back to our app
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                )
            }
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, viewModel.appLinkUri))
            }
        }
    }

    private fun setupContactUsButton() {
        binding.contactUsLayout.setOnClickListener {
            startActivity(
                Intent(
                    context,
                    ContactUsActivity::class.java
                )
            )
        }
    }

    private fun setupLanguageSpinner() {
        with(binding.languageSpinner) {
            adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, LANGUAGE_LIST)
            setSelection(viewModel.languageListPosition)
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.languageListPosition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
