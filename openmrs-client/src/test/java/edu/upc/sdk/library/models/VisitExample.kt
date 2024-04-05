package edu.upc.sdk.library.models

import edu.upc.blopup.model.Treatment
import edu.upc.sdk.library.api.ObservationConcept
import edu.upc.sdk.library.databases.entities.ConceptEntity
import edu.upc.sdk.library.databases.entities.LocationEntity
import edu.upc.sdk.utilities.ApplicationConstants.FACILITY_VISIT_TYPE_UUID
import edu.upc.sdk.utilities.DateUtils.formatAsOpenMrsDate
import edu.upc.sdk.utilities.DateUtils.formatAsOpenMrsDateWithoutTime
import edu.upc.sdk.utilities.DateUtils.formatToOpenmrsDate
import org.joda.time.Instant
import java.time.LocalDateTime
import java.util.UUID


object VisitExample {

    fun random(treatment: Treatment = TreatmentExample.activeTreatment(), startDate: Instant = Instant.now()): Visit {

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
                                dateCreated = date.formatAsOpenMrsDateWithoutTime()
                                obsDatetime = date.formatAsOpenMrsDateWithoutTime()
                            }
                        }
                    )
                }
            )
        }
    }

    fun withVitals(visitUuid: String, patientUuid: String, visitStartDate: LocalDateTime, visitLocation:String, systolic: Int, diastolic: Int, pulse: Int, weight: Float?, height: Int?): Visit {
        return Visit().apply {
            patient = Patient().apply {
                uuid = patientUuid
            }
            uuid = visitUuid
            location = LocationEntity(visitLocation)
            startDatetime = visitStartDate.formatAsOpenMrsDate()
            visitType = VisitType("FACILITY", FACILITY_VISIT_TYPE_UUID)
            encounters = listOf(
                Encounter().apply {
                    encounterType = EncounterType(EncounterType.VITALS)
                    observations = listOf(
                        Observation().apply {
                            display = "Systolic blood pressure: $systolic"
                            displayValue = systolic.toString()
                        },
                        Observation().apply {
                            display = "Diastolic blood pressure: $diastolic"
                            displayValue = diastolic.toString()
                        },
                        Observation().apply {
                            display = "Pulse: $pulse"
                            displayValue = pulse.toString()
                        },
                        Observation().apply {
                            display = "Weight (kg): $weight"
                            displayValue = weight?.toString()
                        },
                        Observation().apply {
                            display = "Height (cm): $height"
                            displayValue = height?.toString()
                        }
                    )
                }
            )
        }
    }
}