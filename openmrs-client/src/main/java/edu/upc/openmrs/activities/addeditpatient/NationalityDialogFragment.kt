package edu.upc.openmrs.activities.addeditpatient

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.DialogFragment
import edu.upc.R
import edu.upc.databinding.DialogSearchNationalityBinding


class NationalityDialogFragment : DialogFragment() {
    private lateinit var nationalityDialogBinding: DialogSearchNationalityBinding

    private var nationalityString: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nationalityDialogBinding = DialogSearchNationalityBinding.inflate(inflater, container, false)

        val searchText = nationalityDialogBinding.editTextSearch
        val listNationalities = nationalityDialogBinding.listView

        val nationalities = NationalityData.getNationalities(requireContext())

        val adapter = NationalityAdapter(requireContext(), R.layout.item_nationality, nationalities)
        listNationalities.adapter = adapter

        searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                adapter.filter.filter(text)
            }

            override fun afterTextChanged(s: Editable) {}
        })

        listNationalities.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedNationality = adapter.getItem(position)
                nationalityString = selectedNationality?.name

                val parentFragment = parentFragment
                if (parentFragment is AddEditPatientFragment) {
                    parentFragment.onNationalitySelected(nationalityString)
                }
                dialog?.dismiss()
            }
        return nationalityDialogBinding.root
    }
}
