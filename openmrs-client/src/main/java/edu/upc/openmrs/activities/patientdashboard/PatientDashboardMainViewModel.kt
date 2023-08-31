package edu.upc.openmrs.activities.patientdashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.OperationType
import edu.upc.sdk.library.models.OperationType.PatientSynchronizing
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject


@HiltViewModel
class PatientDashboardMainViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val visitDAO: VisitDAO,
    private val patientRepository: PatientRepository,
    private val visitRepository: VisitRepository,
    private val savedStateHandle: SavedStateHandle
) : edu.upc.openmrs.activities.BaseViewModel<Unit>() {

    val patientId: String = savedStateHandle.get<Long>(PATIENT_ID_BUNDLE)?.toString()!!
    private val patient: Patient = patientDAO.findPatientByID(patientId)
    private val patientUuid: String = patient.uuid!!

    private var runningSyncs = 0

    fun hasActiveVisit(): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        addSubscription(visitDAO.getActiveVisitByPatientId(patientId.toLong())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { visit: Visit? -> liveData.value = visit != null })
        return liveData
    }

    fun endActiveVisit(): LiveData<Boolean> {
        val endVisitResult = MutableLiveData<Boolean>()

        addSubscription(visitDAO.getActiveVisitByPatientId(patientId.toLong())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { activeVisit ->
                if (activeVisit != null) {
                    visitRepository.endVisit(activeVisit)
                        .observeOn(AndroidSchedulers.mainThread())
                } else {
                    Observable.empty()
                }
            }
            .subscribe(
                { endVisitResult.value = true },
                { endVisitResult.value = false }
            )
        )
        return endVisitResult
    }

    fun syncPatientData() {
        setLoading(PatientSynchronizing)
        syncDetails()
        syncVisits()
        syncVitals()
    }

    private fun syncDetails() {
        runningSyncs++
        addSubscription(patientRepository.downloadPatientByUuid(patientUuid)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { handleSyncPatientDetails(it) },
                { setError(it, PatientSynchronizing) }
            )
        )
    }

    private fun handleSyncPatientDetails(serverPatient: Patient) {
        if (!serverPatient.equals(patient)) {
            patientDAO.updatePatient(patientId.toLong(), serverPatient)
        }

        setContent(Unit, PatientSynchronizing)
    }

    private fun syncVisits() {
        runningSyncs++
        addSubscription(visitRepository.syncVisitsData(patient)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { setContent(Unit, PatientSynchronizing) },
                { setError(it, PatientSynchronizing) }
            ))
    }

    private fun syncVitals() {
        runningSyncs++
        addSubscription(visitRepository.syncLastVitals(patient.uuid)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { setContent(Unit, PatientSynchronizing) },
                { setError(it, PatientSynchronizing) }
            )
        )
    }

    override fun setContent(data: Unit, operationType: OperationType) {
        if (operationType == PatientSynchronizing) {
            runningSyncs--
            // Check if no syncs are still running
            if (runningSyncs == 0) super.setContent(data, operationType)
        } else {
            super.setContent(data, operationType)
        }
    }

    override fun setError(t: Throwable, operationType: OperationType) {
        Log.d("GeneralLogKey", " setError: ${t.message}")
        if (operationType == PatientSynchronizing) clearSubscriptions()
        super.setError(t, operationType)
    }
}
