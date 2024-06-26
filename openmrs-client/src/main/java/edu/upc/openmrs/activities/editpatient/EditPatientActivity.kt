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
package edu.upc.openmrs.activities.editpatient

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.openmrs.activities.editpatient.EditPatientFragment.Companion.newInstance
import edu.upc.sdk.utilities.ApplicationConstants

@AndroidEntryPoint
class EditPatientActivity : ACBaseActivity() {
    private var editPatientFragment: EditPatientFragment? = null
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_info)

        // Get and send patient id to the fragment (in case of updating a patient)
        val patientBundle = savedInstanceState ?: intent.extras
        val patientID = patientBundle?.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE)

        // Create fragment
        editPatientFragment =
            supportFragmentManager.findFragmentById(R.id.patientInfoContentFrame) as EditPatientFragment?
        editPatientFragment = editPatientFragment ?: newInstance(patientID)

        if (!editPatientFragment!!.isActive) {
            addFragmentToActivity(
                supportFragmentManager,
                editPatientFragment!!,
                R.id.patientInfoContentFrame
            )
        }
    }

    override fun onBackPressed() {
        if (editPatientFragment!!.isAnyFieldNotEmpty()) showInfoLostDialog()
        else if (!editPatientFragment!!.isLoading()) super.onBackPressed()
    }

    /**
     * The method creates a warning dialog when the user presses back button while registering a patient
     */
    private fun showInfoLostDialog() {
        alertDialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle(R.string.dialog_title_reset_patient)
            .setMessage(R.string.dialog_message_data_lost)
            .setCancelable(false)
            .setPositiveButton(R.string.dialog_button_stay) { dialog: DialogInterface, id: Int -> dialog.cancel() }
            .setNegativeButton(R.string.dialog_button_leave) { _: DialogInterface?, id: Int ->
                // Finish the activity
                super.onBackPressed()
                finish()
            }
            .create()
        alertDialog?.show()
    }

    override fun onPause() {
        if (alertDialog != null) {
            // Dismiss and clear the dialog to prevent Window leaks
            alertDialog!!.dismiss()
            alertDialog = null
        }
        super.onPause()
    }
}
