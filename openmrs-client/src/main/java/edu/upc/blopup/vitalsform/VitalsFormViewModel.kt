package edu.upc.blopup.vitalsform

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.EncounterRepository
import edu.upc.sdk.library.api.repository.FormRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.*
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.execute
import org.joda.time.LocalDateTime
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class VitalsFormViewModel @Inject constructor(
    patientDAO: PatientDAO,
    private val formRepository: FormRepository,
    private val visitRepository: VisitRepository,
    private val encounterRepository: EncounterRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<Unit>() {

    private val patientId: Long = savedStateHandle[PATIENT_ID_BUNDLE]!!
    private val encounterType: String = EncounterType.VITALS
    private val formName: String = "Vitals"

    val patient: Patient = patientDAO.findPatientByID(patientId.toString())

    fun getLastHeightFromVisits(): LiveData<Result<String>> {
        val resultLiveData = MutableLiveData<Result<String>>()
        val latestVisit = visitRepository.getLatestVisitWithHeight(patientId)
        latestVisit.ifPresent { visit ->
            resultLiveData.value = Result.Success(visit.getLatestHeight())
        }
        if (!latestVisit.isPresent) {
            resultLiveData.value = Result.Success("")
        }
        return resultLiveData
    }

    fun submitForm(vitals: List<Vital>): LiveData<ResultType> {
        val resultLiveData = MutableLiveData<ResultType>()
        if (vitals.isEmpty()) {
            resultLiveData.value = ResultType.EncounterSubmissionError
            return resultLiveData
        }
        val encounterCreate = Encountercreate()
        encounterCreate.patientId = patientId
        encounterCreate.observations = createObservationsFromVitals(vitals)

        return if (visitRepository.getActiveVisitByPatientId(patientId) == null) {
            val visit = visitRepository.startVisit(patient).execute()
            createRecords(encounterCreate, visit.id)
        } else {
            createRecords(encounterCreate, null)
        }
    }

    private fun createObservationsFromVitals(vitals: List<Vital>): List<Obscreate> {
        val observations = mutableListOf<Obscreate>()

        for (vital in vitals) {
            if (vital.validate()) {
                observations += Obscreate().apply {
                    concept = vital.concept
                    value = vital.value
                    obsDatetime = LocalDateTime().toString()
                    person = patient.uuid
                }
            }
        }
        return observations
    }

    private fun createRecords(
        encounterCreate: Encountercreate,
        visitId: Long?
    ): MutableLiveData<ResultType> {
        val resultLiveData = MutableLiveData<ResultType>()

        encounterCreate.patient = patient.uuid
        encounterCreate.encounterType = encounterType
        encounterCreate.formname = formName
        encounterCreate.formUuid = formRepository.fetchFormResourceByName(formName).execute().uuid

        addSubscription(
            encounterRepository.saveEncounter(encounterCreate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { resultLiveData.value = it },
                    { resultLiveData.value = ResultType.EncounterSubmissionError; if (visitId != null) {
                        visitRepository.deleteVisitById(visitId)
                    }
                    },
                    {
                        if (resultLiveData.value == ResultType.EncounterSubmissionError && visitId != null)
                            visitRepository.deleteVisitById(visitId)
                    }
                )
        )
        return resultLiveData
    }
}
