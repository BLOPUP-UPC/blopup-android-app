package edu.upc.sdk.library.models

import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.databases.entities.ConceptEntity
import edu.upc.sdk.utilities.DateUtils.formatToOpenmrsDate
import org.joda.time.Instant
import java.util.UUID


object VisitExample {

    fun random(treatment: Treatment, startDate: Instant = Instant.now()): Visit {

        val activeText = if (treatment.isActive) " 1.0" else " 0.0"

        return Visit().apply {
            patient = Patient().apply {
                uuid = UUID.randomUUID().toString()
            }
            id = treatment.visitId
            uuid = treatment.visitUuid
            startDatetime = startDate.formatToOpenmrsDate()
            encounters = listOf(
                Encounter().apply {
                    uuid = UUID.randomUUID().toString()
                    visit = Visit().apply {
                        id = treatment.visitId
                        uuid = treatment.visitUuid
                    }
                    encounterDate = treatment.creationDate.formatToOpenmrsDate()
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
                            obsDatetime = treatment.inactiveDate.toString()
                        }
                    )
                }
            )
        }
    }
}