package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.api.RestApi
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewVisitRepository @Inject constructor(val restApi: RestApi){
    fun getVisitByUuid(visitUuid: UUID): Visit {
        return Visit(
            visitUuid,
            "La casa de Aleh",
            LocalDate.now(),
            BloodPressure(120, 80, 70),
            177,
            70.0f
        )
    }

}
