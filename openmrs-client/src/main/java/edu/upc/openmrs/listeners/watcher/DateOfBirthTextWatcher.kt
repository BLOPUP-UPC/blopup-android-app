package edu.upc.openmrs.listeners.watcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class DateOfBirthTextWatcher(private val dobEditText: EditText, private val estimatedMonth: EditText, private val estimatedYear: EditText) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // No need for this method
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        // Auto-add slash before entering month (e.g. "17/*") and before entering year (e.g. "17/10/*")
        dobEditText.text.toString().let {
            if ((it.length == 3 && !it.contains("/")) ||
                (it.length == 6 && !it.substring(3).contains("/"))
            ) {
                dobEditText.setText(StringBuilder(it).insert(it.length - 1, "/").toString())
                dobEditText.setSelection(dobEditText.text.length)
            }
        }
    }

    override fun afterTextChanged(s: Editable) {
        // If a considerable amount of text is filled in dobEditText, then remove 'Estimated age' fields.
        if (s.length >= 10) {
            estimatedMonth.text.clear()
            estimatedYear.text.clear()
        }
    }
}