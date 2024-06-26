package edu.upc.sdk.library.models

import edu.upc.blopup.model.Treatment
import edu.upc.sdk.library.api.ObservationConcept
import edu.upc.sdk.library.api.repository.DoctorRepository.Companion.REGISTRATION_NUMBER_UUID
import edu.upc.sdk.library.api.repository.TreatmentRepository.Companion.ENCOUNTER_DOCTOR_ROLE_UUID
import edu.upc.sdk.library.api.repository.TreatmentRepository.Companion.TREATMENT_ENCOUNTER_TYPE
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.api.repository.VisitRepository.Companion.VITALS_ENCOUNTER_TYPE
import edu.upc.sdk.library.databases.entities.ConceptEntity
import edu.upc.sdk.library.databases.entities.LocationEntity
import edu.upc.sdk.utilities.ApplicationConstants.FACILITY_VISIT_TYPE_UUID
import edu.upc.sdk.utilities.DateUtils.formatToApiRequest
import java.time.Instant
import java.util.UUID


object OpenMrsVisitExample {

    fun withTreatment(
        treatment: Treatment = TreatmentExample.activeTreatment(),
        startDate: Instant = Instant.now()
    ): OpenMRSVisit {

        val activeText = if (treatment.isActive) " 1.0" else " 0.0"

        return OpenMRSVisit().apply {
            patient = Patient().apply {
                uuid = UUID.randomUUID().toString()
            }
            uuid = treatment.visitUuid
            startDatetime = startDate.toString()
            encounters = listOf(
                Encounter().apply {
                    uuid = treatment.treatmentUuid
                    visit = OpenMRSVisit().apply {
                        uuid = treatment.visitUuid
                    }
                    encounterDate = treatment.creationDate.toString()
                    encounterType = EncounterType(TREATMENT_ENCOUNTER_TYPE)
                    treatment.doctor?.let {
                        encounterProviders =
                            listOf(
                                EncounterProvider().apply {
                                    encounterRole = Resource().apply {
                                        uuid = ENCOUNTER_DOCTOR_ROLE_UUID
                                    }
                                    provider = Provider().apply {
                                        uuid = it.uuid
                                        person = Person().apply {
                                            display = it.name
                                        }
                                        attributes = listOf(
                                            ProviderAttribute().apply {
                                                attributeType = ProviderAttributeType().apply {
                                                    uuid = REGISTRATION_NUMBER_UUID
                                                }
                                                value = it.registrationNumber
                                            }
                                        )
                                    }
                                }
                            )
                    }
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
                                dateCreated = date.formatToApiRequest()
                                obsDatetime = date.formatToApiRequest()
                            }
                        }
                    )
                }
            )
        }
    }

    fun withVitals(
        visitUuid: String = UUID.randomUUID().toString(),
        patientUuid: String = UUID.randomUUID().toString(),
        visitStartDate: java.time.Instant = java.time.Instant.now(),
        visitLocation: String = "Location",
        systolic: Int = 120,
        diastolic: Int = 80,
        pulse: Int = 70,
        weight: Float? = null,
        height: Int? = null
    ): OpenMRSVisit {
        return OpenMRSVisit().apply {
            patient = Patient().apply {
                uuid = patientUuid
            }
            uuid = visitUuid
            location = LocationEntity(visitLocation)
            startDatetime = visitStartDate.toString()
            visitType = VisitType("FACILITY", FACILITY_VISIT_TYPE_UUID)
            encounters = listOf(
                Encounter().apply {
                    encounterType = EncounterType(VITALS_ENCOUNTER_TYPE)
                    observations = listOf(
                        Observation().apply {
                            concept = ConceptEntity().apply {
                                uuid = VisitRepository.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT
                            }
                            display = "Systolic blood pressure: $systolic"
                            displayValue = systolic.toString()
                            value = systolic.toString()
                        },
                        Observation().apply {
                            concept = ConceptEntity().apply {
                                uuid = VisitRepository.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT
                            }
                            display = "Diastolic blood pressure: $diastolic"
                            displayValue = diastolic.toString()
                            value = diastolic.toString()
                        },
                        Observation().apply {
                            concept = ConceptEntity().apply {
                                uuid = VisitRepository.VitalsConceptType.HEART_RATE_FIELD_CONCEPT
                            }
                            display = "Pulse: $pulse"
                            displayValue = pulse.toString()
                            value = pulse.toString()
                        }
                    )
                    weight?.let {
                        observations = observations.plus(
                            Observation().apply {
                                concept = ConceptEntity().apply {
                                    uuid = VisitRepository.VitalsConceptType.WEIGHT_FIELD_CONCEPT
                                }
                                display = "Weight (kg): $weight"
                                displayValue = weight.toString()
                                value = weight.toString()
                            }
                        )
                    }
                    height?.let {
                        observations = observations.plus(
                            Observation().apply {
                                concept = ConceptEntity().apply {
                                    uuid = VisitRepository.VitalsConceptType.HEIGHT_FIELD_CONCEPT
                                }
                                display = "Height (cm): $height"
                                displayValue = height.toString()
                                value = height.toString()
                            }
                        )
                    }
                }
            )
        }
    }
}