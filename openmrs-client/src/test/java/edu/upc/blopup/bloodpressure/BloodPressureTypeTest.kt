package edu.upc.blopup.bloodpressure

import edu.upc.sdk.library.databases.entities.ConceptEntity
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.utilities.ApplicationConstants.vitalsConceptType.DIASTOLIC_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.vitalsConceptType.SYSTOLIC_FIELD_CONCEPT
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
                concept = ConceptEntity().apply {
                    uuid = SYSTOLIC_FIELD_CONCEPT
                }
            },
            Observation().apply {
                display = "Diastolic"
                displayValue = "70"
                concept = ConceptEntity().apply {
                    uuid = DIASTOLIC_FIELD_CONCEPT
                }
            }
        )
        assertEquals(
            BloodPressureType.NORMAL,
            hypertensionTypeFromEncounter(encounter)
        )
    }

    @Test
    fun hypertensionTypeFromEncounterStageI() {
        val encounter = Encounter()
        encounter.observations = listOf(
            Observation().apply {
                display = "Systolic"
                displayValue = "130"
                concept = ConceptEntity().apply {
                    uuid = SYSTOLIC_FIELD_CONCEPT
                }
            },
            Observation().apply {
                display = "Diastolic"
                displayValue = "80"
                concept = ConceptEntity().apply {
                    uuid = DIASTOLIC_FIELD_CONCEPT
                }
            }
        )
        assertEquals(
            BloodPressureType.STAGE_I,
            hypertensionTypeFromEncounter(encounter)
        )
    }

    @Test
    fun hypertensionTypeFromEncounterStageIIA() {
        val encounter = Encounter()
        encounter.observations = listOf(
            Observation().apply {
                display = "Systolic"
                displayValue = "140"
                concept = ConceptEntity().apply {
                    uuid = SYSTOLIC_FIELD_CONCEPT
                }
            },
            Observation().apply {
                display = "Diastolic"
                displayValue = "90"
                concept = ConceptEntity().apply {
                    uuid = DIASTOLIC_FIELD_CONCEPT
                }
            }
        )
        assertEquals(
            BloodPressureType.STAGE_II_A,
            hypertensionTypeFromEncounter(encounter)
        )
    }

    @Test
    fun hypertensionTypeFromEncounterStageIIB() {
        val encounter = Encounter()
        encounter.observations = listOf(
            Observation().apply {
                display = "Systolic"
                displayValue = "160"
                concept = ConceptEntity().apply {
                    uuid = SYSTOLIC_FIELD_CONCEPT
                }
            },
            Observation().apply {
                display = "Diastolic"
                displayValue = "100"
                concept = ConceptEntity().apply {
                    uuid = DIASTOLIC_FIELD_CONCEPT
                }
            }
        )
        assertEquals(
            BloodPressureType.STAGE_II_B,
            hypertensionTypeFromEncounter(encounter)
        )
    }

    @Test
    fun hypertensionTypeFromEncounterStageIIC() {
        val encounter = Encounter()
        encounter.observations = listOf(
            Observation().apply {
                display = "Systolic"
                displayValue = "180"
                concept = ConceptEntity().apply {
                    uuid = SYSTOLIC_FIELD_CONCEPT
                }
            },
            Observation().apply {
                display = "Diastolic"
                displayValue = "110"
                concept = ConceptEntity().apply {
                    uuid = DIASTOLIC_FIELD_CONCEPT
                }
            }
        )
        assertEquals(
            BloodPressureType.STAGE_II_C,
            hypertensionTypeFromEncounter(encounter)
        )
    }
}