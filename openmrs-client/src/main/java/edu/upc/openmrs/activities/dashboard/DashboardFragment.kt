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
package edu.upc.openmrs.activities.dashboard

import android.graphics.Bitmap
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.FragmentDashboardBinding
import edu.upc.openmrs.activities.BaseFragment
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ImageUtils

@AndroidEntryPoint
class DashboardFragment : BaseFragment(), View.OnClickListener {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private var mBitmapCache: SparseArray<Bitmap>? = null

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val settings2 =
            requireActivity().getSharedPreferences(ApplicationConstants.OPENMRS_PREF_FILE, 0)
        if (settings2.getBoolean("my_first_time", true)) {
            settings2.edit().putBoolean("my_first_time", false).apply()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        bindDrawableResources()
        setListeners()

        return binding.root
    }

    private fun setListeners() {
        with(binding) {
            registryPatientView.setOnClickListener(this@DashboardFragment)
            findPatientView.setOnClickListener(this@DashboardFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindDrawableResources()
    }


    /**
     * Binds drawable resources to all dashboard buttons
     */
    fun bindDrawableResources() {
        with(binding) {
            bindDrawableResource(findPatientButton, R.mipmap.ico_search_patients)
            bindDrawableResource(registryPatientButton, R.mipmap.ico_add_patient)
            findPatientLabel.setText(R.string.dashboard_search_icon_label)
            registryLabel.setText(R.string.action_register_patient)
        }
    }

    /**
     * Binds drawable resource to ImageView
     *
     * @param imageView ImageView to bind resource to
     * @param drawableId id of drawable resource (for example R.id.somePicture);
     */
    private fun bindDrawableResource(imageView: ImageView, drawableId: Int) {
        mBitmapCache = SparseArray()
        if (view != null) {
            createImageBitmap(drawableId, imageView.layoutParams)
            imageView.setImageBitmap(mBitmapCache!![drawableId])
        }
    }

    /**
     * Unbinds drawable resources
     */
    private fun unbindDrawableResources() {
        if (null != mBitmapCache) {
            for (i in 0 until mBitmapCache!!.size()) {
                val bitmap = mBitmapCache!!.valueAt(i)
                bitmap!!.recycle()
            }
        }
    }

    private fun createImageBitmap(key: Int, layoutParams: ViewGroup.LayoutParams) {
        if (mBitmapCache!![key] == null) {
            mBitmapCache!!.put(
                key, ImageUtils.decodeBitmapFromResource(
                    resources, key,
                    layoutParams.width, layoutParams.height
                )
            )
        }
    }

    override fun onClick(v: View) {
        val directionToRegister =
            DashboardFragmentDirections.actionDashboardFragmentToAddEditPatientActivity()
        val directionToFindPatent =
            DashboardFragmentDirections.actionDashboardFragmentToSyncedPatientsActivity()
        when (v.id) {
            R.id.findPatientView -> findNavController().navigate(directionToFindPatent)
            R.id.registryPatientView -> findNavController().navigate(directionToRegister)
            else -> {
            }
        }
    }

    companion object {
        fun newInstance(): DashboardFragment {
            return DashboardFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
