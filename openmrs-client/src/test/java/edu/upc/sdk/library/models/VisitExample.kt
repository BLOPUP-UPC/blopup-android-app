package edu.upc.sdk.library.models

import edu.upc.sdk.library.api.ObservationConcept
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
            uuid = treatment.visitUuid
            startDatetime = startDate.formatToOpenmrsDate()
            encounters = listOf(
                Encounter().apply {
                    uuid = treatment.treatmentUuid
                    visit = Visit().apply {
                        uuid = treatment.visitUuid
                    }
                    encounterDate = treatment.creationDate.formatToOpenmrsDate()
                    encounterType = EncounterType(EncounterType.TREATMENT)
                    observations = listOf(
                        Observation().apply {
                            concept = ConceptEntity().apply {
                                uuid = ObservationConcept.RECOMMENDED_BY.uuid
                            }
                            displayValue = treatment.recommendedBy
                        },
                        Observation().apply {
                            concept = ConceptEntity().apply {
                                uuid = ObservationConcept.MEDICATION_NAME.uuid
                            }
                            displayValue = treatment.medicationName
                            display = "Medication Name: ${treatment.medicationName}"
                            uuid = UUID.randomUUID().toString()
                        },
                        Observation().apply {
                            concept = ConceptEntity().apply {
                                uuid = ObservationConcept.MEDICATION_TYPE.uuid
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
                                uuid = ObservationConcept.TREATMENT_NOTES.uuid
                            }
                            displayValue = treatment
                                .notes
                            display = "Treatment Notes: ${treatment.notes}"
                            uuid = UUID.randomUUID().toString()
                        },
                        Observation().apply {
                            concept =
                                ConceptEntity().apply { uuid = ObservationConcept.ACTIVE.uuid }
                            displayValue = activeText
                            display = "Active:$activeText"
                            uuid = UUID.randomUUID().toString()
                            treatment.observationStatusUuid = uuid
                            dateCreated = treatment.creationDate.toString()
                            obsDatetime = treatment.inactiveDate.toString()
                        },
                    ).plus(
                        treatment.adherence.map { (date, value) ->
                            Observation().apply {
                                concept =
                                    ConceptEntity().apply {
                                        uuid = ObservationConcept.TREATMENT_ADHERENCE.uuid
                                    }
                                displayValue = if (value) "1.0" else "0.0"
                                display = "Adherence: $displayValue"
                                dateCreated = date.toString()
                                obsDatetime = date.toString()
                            }
                        }
                    )
                }
            )
        }
    }
}