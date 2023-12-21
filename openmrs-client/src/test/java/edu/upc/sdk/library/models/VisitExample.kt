package edu.upc.sdk.library.models

import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.databases.entities.ConceptEntity
import edu.upc.sdk.utilities.DateUtils
import java.util.UUID


object VisitExample {

    fun random(treatment: Treatment): Visit {

        val activeText = if (treatment.isActive) " 1.0" else " 0.0"

        return Visit().apply {
            uuid = UUID.randomUUID().toString()
            encounters = listOf(
                Encounter().apply {
                    uuid = UUID.randomUUID().toString()
                    visitID = treatment.visitId
                    encounterDate = DateUtils.dateFormatterToString(treatment.creationDate)
                    encounterType = EncounterType(EncounterType.TREATMENT)
                    observations = listOf(
                        Observation().apply {
                            concept = ConceptEntity().apply {
                                uuid = TreatmentRepository.RECOMMENDED_BY_CONCEPT_ID
                            }
                            displayValue = treatment.recommendedBy
                        },
                        Observation().apply {
                            concept = ConceptEntity().apply {
                                uuid = TreatmentRepository.MEDICATION_NAME_CONCEPT_ID
                            }
                            displayValue = treatment.medicationName
                            display = "Medication Name: ${treatment.medicationName}"
                            uuid = UUID.randomUUID().toString()
                        },
                        Observation().apply {
                            concept = ConceptEntity().apply {
                                uuid = TreatmentRepository.MEDICATION_TYPE_CONCEPT_ID
                            }
                            groupMembers = treatment.medicationType.map {
                                Observation().apply {
                                    valueCodedName = it.conceptId
                                }
                            }
                            uuid = UUID.randomUUID().toString()
                        },
                        Observation().apply {
                            concept = ConceptEntity().apply {
                                uuid = TreatmentRepository.TREATMENT_NOTES_CONCEPT_ID
                            }
                            displayValue = treatment
                                .notes
                            display = "Treatment Notes: ${treatment.notes}"
                            uuid = UUID.randomUUID().toString()
                        },
                        Observation().apply {
                            concept = ConceptEntity().apply { uuid = TreatmentRepository.ACTIVE_CONCEPT_ID }
                            displayValue = activeText
                            display = "Active:$activeText"
                            uuid = UUID.randomUUID().toString()
                            dateCreated = treatment.creationDate.toString()
                        }
                    )
                }
            )
        }
    }
}