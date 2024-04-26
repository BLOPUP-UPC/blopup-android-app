package edu.upc.openmrs.activities.editpatient.countryofbirth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import edu.upc.R
import edu.upc.databinding.DialogSearchCountryOfBirthBinding
import edu.upc.openmrs.activities.editpatient.EditPatientFragment


class CountryOfBirthDialogFragment : DialogFragment() {
    private lateinit var countryOfBirthBinding: DialogSearchCountryOfBirthBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        countryOfBirthBinding = DialogSearchCountryOfBirthBinding.inflate(inflater, container, false)

        val searchText = countryOfBirthBinding.editTextSearch
        val listCountries = countryOfBirthBinding.listView

        val countries = Country.values()

        val adapter = CountryAdapter(requireContext(), R.layout.item_country, countries)
        listCountries.adapter = adapter

        searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                adapter.filter.filter(text)
            }

            override fun afterTextChanged(s: Editable) {}
        })

        onCountrySelection(listCountries, adapter)

        closeDialog()

        return countryOfBirthBinding.root
    }

    private fun closeDialog() {
        val closeDialogImage = countryOfBirthBinding.imgDismiss
        closeDialogImage.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun onCountrySelection(
        listCountries: ListView,
        adapter: CountryAdapter
    ) {
        listCountries.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedCountry = adapter.getItem(position)

                val parentFragment = parentFragment
                if (parentFragment is EditPatientFragment) {
                    parentFragment.onCountrySelected(selectedCountry)
                }
                dialog?.dismiss()
            }
    }
}
