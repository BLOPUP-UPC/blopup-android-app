package edu.upc.openmrs.activities.visit

import edu.upc.blopup.model.Treatment

interface TreatmentListener {
    fun onFinaliseClicked(treatment: Treatment)
    fun onEditClicked(treatment: Treatment)
    fun onRemoveClicked(treatment: Treatment)
    fun onRefreshTreatments()
}