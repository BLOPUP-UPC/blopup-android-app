package edu.upc.blopup.bloodpressure

import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Observation
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class BloodPressureTypeTest {

    @Test
    fun hypertensionTypeFromEncounter() {
        val encounter = Encounter()
        encounter.observations = listOf(
            Observation().apply {
                display = "Systolic"
                displayValue = "120"
            },
            Observation().apply {
                display = "Diastolic"
                displayValue = "70"
            }
        )
        assertEquals(
            BloodPressureType.NORMAL,
            bloodPressureTypeFromEncounter(encounter)?.bloodPressureType
        )
    }

    @Test
    fun hypertensionTypeFromEncounterStageI() {
        val encounter = Encounter()
        encounter.observations = listOf(
            Observation().apply {
                display = "Systolic"
                displayValue = "130"
            },
            Observation().apply {
                display = "Diastolic"
                displayValue = "80"
            }
        )
        assertEquals(
            BloodPressureType.STAGE_I,
            bloodPressureTypeFromEncounter(encounter)?.bloodPressureType
        )
    }

    @Test
    fun hypertensionTypeFromEncounterStageIIA() {
        val encounter = Encounter()
        encounter.observations = listOf(
            Observation().apply {
                display = "Systolic"
                displayValue = "140"
            },
            Observation().apply {
                display = "Diastolic"
                displayValue = "90"
            }
        )
        assertEquals(
            BloodPressureType.STAGE_II_A,
            bloodPressureTypeFromEncounter(encounter)?.bloodPressureType
        )
    }

    @Test
    fun hypertensionTypeFromEncounterStageIIB() {
        val encounter = Encounter()
        encounter.observations = listOf(
            Observation().apply {
                display = "Systolic"
                displayValue = "160"
            },
            Observation().apply {
                display = "Diastolic"
                displayValue = "100"
            }
        )
        assertEquals(
            BloodPressureType.STAGE_II_B,
            bloodPressureTypeFromEncounter(encounter)?.bloodPressureType
        )
    }

    @Test
    fun hypertensionTypeFromEncounterStageIIC() {
        val encounter = Encounter()
        encounter.observations = listOf(
            Observation().apply {
                display = "Systolic"
                displayValue = "180"
            },
            Observation().apply {
                display = "Diastolic"
                displayValue = "110"
            }
        )
        assertEquals(
            BloodPressureType.STAGE_II_C,
            bloodPressureTypeFromEncounter(encounter)?.bloodPressureType
        )
    }
}