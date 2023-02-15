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
package edu.upc.openmrs.activities.community.contact

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.openmrs.android_sdk.library.models.EmailRequest
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.ActvityContactUsBinding

@AndroidEntryPoint
class ContactUsActivity : edu.upc.openmrs.activities.ACBaseActivity() {

    private lateinit var binding: ActvityContactUsBinding
    private val viewModel: ContactUsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActvityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.elevation = 0f
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle(R.string.contact_us)
        }

        binding.sendEmail.setOnClickListener {
            sendEmail()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        //Disable Contact Option in Menu
        val contactItem = menu.findItem(R.id.actionContact)
        contactItem.isVisible = false
        val logOutItem = menu.findItem(R.id.actionLogout)
        logOutItem.isVisible = false
        val locationItem = menu.findItem(R.id.actionLocation)
        locationItem.isVisible = false
        val settingItem = menu.findItem(R.id.actionSettings)
        settingItem.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendEmail() {
        val message = binding.message.text.toString()

        val emailRequest = EmailRequest("Blopup", message)

        viewModel.sendEmail(emailRequest)
    }
}
