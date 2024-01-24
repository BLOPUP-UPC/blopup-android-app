package edu.upc.openmrs.activities.visitdashboard

import edu.upc.sdk.library.models.Treatment

interface TreatmentListener {
    fun onFinaliseClicked(treatment: Treatment)
    fun onEditClicked(treatment: Treatment)
    fun onRemoveClicked(treatment: Treatment)
    fun onRefreshTreatments()
}