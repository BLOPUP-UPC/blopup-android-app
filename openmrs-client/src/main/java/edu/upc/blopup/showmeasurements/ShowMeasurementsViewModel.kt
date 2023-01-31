package edu.upc.blopup.showmeasurements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.openmrs.android_sdk.library.api.repository.EncounterRepository
import com.openmrs.android_sdk.library.api.repository.FormRepository
import com.openmrs.android_sdk.library.dao.PatientDAO
import com.openmrs.android_sdk.library.models.*
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import com.openmrs.android_sdk.utilities.execute
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.activities.BaseViewModel
import org.joda.time.LocalDateTime
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class ShowMeasurementsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val formRepository: FormRepository,
    private val encounterRepository: EncounterRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<Unit>() {

    val DEFAULT_VALUE = -1.0F
    private val patientId: Long = savedStateHandle.get(PATIENT_ID_BUNDLE)!!
    private val encounterType: String = EncounterType.VITALS
    private val formName: String = "Vitals"

    val patient: Patient = patientDAO.findPatientByID(patientId.toString())

    fun submitForm(vitals: List<Vital>): LiveData<ResultType> {
        val encounterCreate = Encountercreate()
        encounterCreate.patientId = patientId
        encounterCreate.observations = createObservationsFromVitals(vitals)

        return createRecords(encounterCreate)
    }

    private fun createObservationsFromVitals(vitals: List<Vital>): List<Obscreate> {
        val observations = mutableListOf<Obscreate>()

        for (vital in vitals) {
            if (vital.value.toFloat() != DEFAULT_VALUE) {
                observations += Obscreate().apply {
                    concept = vital.concept
                    value = vital.value.toString()
                    obsDatetime = LocalDateTime().toString()
                    person = patient.uuid
                }
            }
        }
        return observations
    }

    private fun createRecords(enc: Encountercreate): LiveData<ResultType> {
        val resultLiveData = MutableLiveData<ResultType>()

        addSubscription(
            Observable.fromCallable {
                enc.patient = patient.uuid
                enc.encounterType = encounterType
                enc.formname = formName
                enc.formUuid = formRepository.fetchFormResourceByName(formName).execute().uuid
                return@fromCallable enc
            }
                .flatMap { encounterCreate -> encounterRepository.saveEncounter(encounterCreate) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { resultLiveData.value = it },
                    { resultLiveData.value = ResultType.EncounterSubmissionError }
                )
        )
        return resultLiveData
    }
}
