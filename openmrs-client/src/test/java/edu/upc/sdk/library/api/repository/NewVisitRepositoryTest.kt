package edu.upc.sdk.library.api.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.models.VisitExample
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Response
import java.time.LocalDateTime
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class NewVisitRepositoryTest {

    @MockK
    private lateinit var restApi: RestApi

    @InjectMockKs
    private lateinit var visitRepository: NewVisitRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should get visit with Vitals by uuid`() {
        val visitUuid = UUID.randomUUID()
        val patientUuid = UUID.randomUUID()
        val expectedVisit = Visit(
            visitUuid,
            patientUuid,
            "La casa de Ale",
            LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS),
            BloodPressure(120, 80, 70),
            177,
            70.0f
        )

        val openMRSVisit = VisitExample.withVitals(
            visitUuid.toString(),
            patientUuid.toString(),
            expectedVisit.startDate,
            expectedVisit.location,
            expectedVisit.bloodPressure.systolic,
            expectedVisit.bloodPressure.diastolic,
            expectedVisit.bloodPressure.pulse,
            expectedVisit.weightKg,
            expectedVisit.heightCm
        )
        val response = Response.success(openMRSVisit)

        val call = mockk<Call<edu.upc.sdk.library.models.Visit>>(relaxed = true)
        coEvery { restApi.getVisitByUuid(visitUuid.toString()) } returns call
        coEvery { call.execute() } returns response

        val result = visitRepository.getVisitByUuid(visitUuid)

        assertEquals(expectedVisit, result)
    }
}