package edu.upc.openmrs.activities.visitdashboard

import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Treatment
import javax.inject.Inject

@HiltViewModel
class TreatmentViewModel @Inject constructor(private val treatmentRepository: TreatmentRepository) : BaseViewModel<Result<Treatment>>() {

    var treatment = Treatment()

    suspend fun registerTreatment() {
        treatmentRepository.saveTreatment(treatment)
    }
}
