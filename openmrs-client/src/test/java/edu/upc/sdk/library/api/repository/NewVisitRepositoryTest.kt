package edu.upc.sdk.library.api.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.api.RestApi
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
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
    fun getVisitByUuid() {
        val visitUuid = UUID.randomUUID()
        val expectedVisit = Visit(
            visitUuid,
            "La casa de Aleh",
            LocalDate.now(),
            BloodPressure(120, 80, 70),
            177,
            70.0f
        )

        val result = visitRepository.getVisitByUuid(visitUuid)

        Assert.assertEquals(expectedVisit, result)
    }
}