package edu.upc.blopup

import edu.upc.sdk.library.models.MedicationType

data class CheckTreatment(val title: String, val medicationType: Set<MedicationType>, val selected: Boolean = false, var onCheckedChange: (Boolean) -> Unit) {
    fun check(onToggleEnabled: () -> Unit) {
        if (selected) {
            onToggleEnabled()
        }
    }
}