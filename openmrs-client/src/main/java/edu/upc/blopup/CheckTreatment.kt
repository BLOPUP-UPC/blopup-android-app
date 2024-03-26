package edu.upc.blopup

import edu.upc.blopup.model.MedicationType

data class CheckTreatment(val medicationName: String, val medicationType: Set<MedicationType>, val selected: Boolean = false, var onCheckedChange: (Boolean) -> Unit, val treatmentId: String) {
    fun check(onToggleEnabled: () -> Unit) {
        if (selected) {
            onToggleEnabled()
        }
    }
}