package edu.upc.sdk.library.api.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncounterRepository @Inject constructor() : BaseRepository(null) {
    suspend fun removeEncounter(encounterUuid: String?): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val response = restApi.deleteEncounter(encounterUuid).execute()

                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Remove treatment error: ${response.code()} - ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Remove encounter error: ${e.message}"))
        }
    }
}
