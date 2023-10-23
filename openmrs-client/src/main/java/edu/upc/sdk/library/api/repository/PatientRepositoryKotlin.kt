package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Results
import edu.upc.sdk.utilities.ApplicationConstants
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepositoryKotlin @Inject constructor() : BaseRepository() {

    /**
     * Find patients.
     *
     * @param query patient query string
     * @return observable list of patients with matching query
     */
    fun findPatients(query: String?): List<Patient> {
        val call: Call<Results<Patient>> =
            restApi.getPatients(query, ApplicationConstants.API.FULL)
        val response = call.execute()
        if (response.isSuccessful) {
            return response.body()!!.results
        } else {
            throw Exception("Error with finding patients: " + response.message())
        }
    }
}