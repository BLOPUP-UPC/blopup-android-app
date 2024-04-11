package edu.upc.blopup.model

import edu.upc.sdk.library.models.BloodPressureType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.params.ParameterizedTest


class VisitTest {
    @Test
    fun `normal blood pressure type`() {
        val bloodPressure = BloodPressure(
            systolic = 120,
            diastolic = 70,
            pulse = 80
        )
        val visit = VisitExample.random(bloodPressure = bloodPressure)

        assertEquals(
            BloodPressureType.NORMAL,
            visit.bloodPressureType()
        )
    }

    @Test
    fun `stage I blood pressure type`() {
        val bloodPressure = BloodPressure(
            systolic = 130,
            diastolic = 80,
            pulse = 80
        )
        val visit = VisitExample.random(bloodPressure = bloodPressure)

        assertEquals(
            BloodPressureType.STAGE_I,
            visit.bloodPressureType()
        )
    }

    @Test
    fun `stage II A blood pressure type`() {
        val bloodPressure = BloodPressure(
            systolic = 140,
            diastolic = 90,
            pulse = 80
        )
        val visit = VisitExample.random(bloodPressure = bloodPressure)

        assertEquals(
            BloodPressureType.STAGE_II_A,
            visit.bloodPressureType()
        )
    }

    @Test
    fun `stage II B blood pressure type`() {
        val bloodPressure = BloodPressure(
            systolic = 160,
            diastolic = 100,
            pulse = 80
        )
        val visit = VisitExample.random(bloodPressure = bloodPressure)

        assertEquals(
            BloodPressureType.STAGE_II_B,
            visit.bloodPressureType()
        )
    }

    @Test
    fun `stage II C blood pressure type`() {
        val bloodPressure = BloodPressure(
            systolic = 180,
            diastolic = 110,
            pulse = 80
        )
        val visit = VisitExample.random(bloodPressure = bloodPressure)

        assertEquals(
            BloodPressureType.STAGE_II_C,
            visit.bloodPressureType()
        )
    }
}