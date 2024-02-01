package edu.upc.openmrs.activities.patientdashboard.charts

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.OperationType.PatientVisitsFetching
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class PatientDashboardChartsViewModel @Inject constructor(
    private val visitDAO: VisitDAO,
    private val savedStateHandle: SavedStateHandle
) : edu.upc.openmrs.activities.BaseViewModel<JSONObject>() {

    private val patientId: String = savedStateHandle[PATIENT_ID_BUNDLE]!!

    fun fetchChartsData() {
        setLoading()
        addSubscription(visitDAO.getVisitsByPatientID(patientId.toLong())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { visits: List<Visit> -> setContent(getObservationListFromVisits(visits), PatientVisitsFetching) },
                        { setError(it) }
                ))
    }

    private fun getObservationListFromVisits(visits: List<Visit>): JSONObject {
        val displayableEncounterTypesArray = HashSet(ApplicationConstants.EncounterTypes.ENCOUNTER_TYPES_DISPLAYS.toList())
        val observationList = JSONObject()
        for (visit in visits) {
            val encounters = visit.encounters
            if (encounters.isNotEmpty()) {
                for (encounter in encounters) {
                    val datetime = encounter.encounterDate
                    val encounterTypeDisplay = encounter.encounterType!!.display
                    if (displayableEncounterTypesArray.contains(encounterTypeDisplay)) {
                        for (obs in encounter.observations) {
                            var observationLabel = obs.display
                            if (observationLabel!!.contains(":")) {
                                observationLabel = observationLabel.substring(0, observationLabel.indexOf(':'))
                            }
                            try {
                                if (observationList.has(observationLabel)) {
                                    val chartData: JSONObject? = observationList.getJSONObject(observationLabel)
                                    if (chartData!!.has(datetime)) {
                                        val obsValue: JSONArray? = chartData.getJSONArray(datetime)
                                        obsValue?.put(obs.displayValue)
                                        chartData.put(datetime, obsValue)
                                    } else {
                                        val obsValue = JSONArray()
                                        obsValue.put(obs.displayValue)
                                        chartData.put(datetime, obsValue)
                                    }
                                } else {
                                    val chartData = JSONObject()
                                    val obsValue = JSONArray()
                                    obsValue.put(obs.displayValue)
                                    chartData.put(datetime, obsValue)
                                    observationList.put(observationLabel, chartData)
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
        return observationList
    }

}
