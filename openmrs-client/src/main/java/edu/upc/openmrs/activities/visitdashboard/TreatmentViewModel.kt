package edu.upc.openmrs.activities.visitdashboard

import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.models.Treatment
import javax.inject.Inject

@HiltViewModel
class TreatmentViewModel @Inject constructor() :
    BaseViewModel<Treatment>() {

    fun registerMedication() {}

}
