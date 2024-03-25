/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package edu.upc.sdk.library.databases

import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.dao.EncounterDAO
import edu.upc.sdk.library.dao.ObservationDAO
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.databases.entities.ConceptEntity
import edu.upc.sdk.library.databases.entities.DiagnosisEntity
import edu.upc.sdk.library.databases.entities.EncounterEntity
import edu.upc.sdk.library.databases.entities.LocationEntity
import edu.upc.sdk.library.databases.entities.ObservationEntity
import edu.upc.sdk.library.databases.entities.PatientEntity
import edu.upc.sdk.library.databases.entities.VisitEntity
import edu.upc.sdk.library.models.Diagnosis
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.PatientIdentifier
import edu.upc.sdk.library.models.PersonAddress
import edu.upc.sdk.library.models.PersonName
import edu.upc.sdk.library.models.Resource
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.library.models.VisitType
import edu.upc.sdk.utilities.DateUtils
import edu.upc.sdk.utilities.DateUtils.convertTime
import edu.upc.sdk.utilities.FormService.getFormByUuid
import rx.Observable
import rx.schedulers.Schedulers
import java.util.concurrent.Callable

object AppDatabaseHelper {
    @JvmStatic
    fun convert(obs: Observation, encounterID: Long): ObservationEntity {
        val observationEntity =
            ObservationEntity()
        observationEntity.id = obs.id
        observationEntity.uuid = obs.uuid
        observationEntity.display = obs.display
        observationEntity.encounterKeyID = encounterID
        observationEntity.displayValue = obs.displayValue
        observationEntity.diagnosisOrder = obs.diagnosisOrder
        observationEntity.diagnosisList = obs.diagnosisList
        observationEntity.diagnosisCertainty = obs.diagnosisCertainty
        observationEntity.diagnosisNote = obs.diagnosisNote
        if (obs.concept != null) {
            observationEntity.conceptuuid = obs.concept!!.uuid
        } else {
            observationEntity.conceptuuid = null
        }
        return observationEntity
    }

    @JvmStatic
    fun convert(observationEntityList: List<ObservationEntity>): List<Observation> {
        val observationList: MutableList<Observation> = ArrayList()
        for (entity in observationEntityList) {
            val obs = Observation()
            obs.id = entity.id
            obs.encounterID = entity.encounterKeyID
            obs.uuid = entity.uuid
            obs.display = entity.display
            obs.displayValue = entity.displayValue
            obs.diagnosisOrder = entity.diagnosisOrder
            obs.diagnosisList = entity.diagnosisList
            obs.setDiagnosisCertanity(entity.diagnosisCertainty)
            obs.diagnosisNote = entity.diagnosisNote
            val concept = ConceptEntity()
            concept.uuid = entity.conceptuuid
            obs.concept = concept
            observationList.add(obs)
        }
        return observationList
    }

    @JvmStatic
    fun convert(encounter: Encounter, visitID: Long?): EncounterEntity {
        val encounterEntity =
            EncounterEntity()
        encounterEntity.id = encounter.id
        encounterEntity.display = encounter.display
        encounterEntity.uuid = encounter.uuid
        if (visitID != null) {
            encounterEntity.visitKeyId = visitID.toString()
        }
        encounterEntity.encounterDateTime = encounter.encounterDatetime.toString()
        encounterEntity.encounterType = encounter.encounterType!!.display
        encounterEntity.patientUuid = encounter.patient?.uuid
        encounterEntity.formUuid = encounter.formUuid
        if (null == encounter.location) {
            encounterEntity.locationUuid = null
        } else {
            encounterEntity.locationUuid = encounter.location!!.uuid
        }
        if (encounter.encounterProviders.isEmpty()) {
            encounterEntity.encounterProviderUuid = null
        } else {
            encounterEntity.encounterProviderUuid = encounter.encounterProviders[0].uuid
        }
        return encounterEntity
    }

    @JvmStatic
    fun convert(entity: EncounterEntity): Encounter {
        val encounter = Encounter()
        if (null != entity.encounterType) {
            encounter.encounterType = EncounterType(entity.encounterType)
        }
        encounter.id = entity.id
        if (null != entity.visitKeyId) {
            encounter.visitID = entity.visitKeyId.toLong()
        }
        encounter.uuid = entity.uuid
        encounter.display = entity.display
        val dateTime = entity.encounterDateTime.toLong()
        encounter.setEncounterDatetime(convertTime(dateTime, DateUtils.OPEN_MRS_REQUEST_FORMAT))
        encounter.observations = ObservationDAO().findObservationByEncounterID(entity.id)
        val location: LocationEntity? = try {
            AppDatabase
                .getDatabase(OpenmrsAndroid.getInstance()?.applicationContext)
                .locationRoomDAO()
                .findLocationByUUID(entity.locationUuid)
                .blockingGet()
        } catch (e: Exception) {
            null
        }
        encounter.location = location
        encounter.form = getFormByUuid(entity.formUuid)
        return encounter
    }

    @JvmStatic
    fun convert(visitEntity: VisitEntity): Visit {
        val visit = Visit()
        visit.id = visitEntity.id
        visit.uuid = visitEntity.uuid
        visit.display = visitEntity.display
        visit.visitType = VisitType(visitEntity.visitType)
        try {
            val locationEntity = AppDatabase
                .getDatabase(OpenmrsAndroid.getInstance()?.applicationContext)
                .locationRoomDAO()
                .findLocationByName(visitEntity.visitPlace)
                .blockingGet()
            visit.location = locationEntity
        } catch (e: Exception) {
            visit.location = LocationEntity(visitEntity.visitPlace)
        }
        visit.startDatetime = visitEntity.startDate
        visit.stopDatetime = visitEntity.stopDate
        visit.encounters = EncounterDAO().findEncountersByVisitID(visitEntity.id)
        visit.patient = PatientDAO().findPatientByID(visitEntity.patientKeyID.toString())
        return visit
    }

    @JvmStatic
    fun convert(visit: Visit): VisitEntity {
        val visitEntity = VisitEntity()
        visitEntity.id = visit.id
        visitEntity.uuid = visit.uuid
        visitEntity.patientKeyID = visit.patient.id!!
        visitEntity.visitType = visit.visitType.display
        if (visit.location != null) {
            visitEntity.visitPlace = visit.location.display
        }
        visitEntity.isStartDate = visit.startDatetime
        visitEntity.stopDate = visit.stopDatetime
        return visitEntity
    }

    @JvmStatic
    fun convert(patientEntity: PatientEntity): Patient {
        val patient = Patient(
            patientEntity.id,
            patientEntity.encounters,
            null
        )
        patient.display = patientEntity.display
        patient.uuid = patientEntity.uuid

        //#region -- Contact Details --
        val contactNames = PersonName()
        contactNames.givenName = patientEntity.givenName
        contactNames.familyName = patientEntity.familyName
        patient.contactNames.add(contactNames)
        patient.contactPhoneNumber = patientEntity.contactPhoneNumber
        //#endregion

        val patientIdentifier = PatientIdentifier()
        patientIdentifier.identifier = patientEntity.identifier
        if (patient.identifiers == null) {
            patient.identifiers = ArrayList()
        }
        patient.identifiers.add(patientIdentifier)
        val personName = PersonName()
        personName.givenName = patientEntity.givenName
        personName.familyName = patientEntity.familyName
        patient.names.add(personName)
        patient.gender = patientEntity.gender
        patient.birthdate = patientEntity.birthDate
        val personAddress = PersonAddress()
        personAddress.address1 = patientEntity.address_1
        personAddress.address2 = patientEntity.address_2
        personAddress.postalCode = patientEntity.postalCode
        personAddress.country = patientEntity.country
        personAddress.stateProvince = patientEntity.state
        personAddress.cityVillage = patientEntity.city
        patient.addresses.add(personAddress)
        if (patientEntity.causeOfDeath != null) {
            patient.causeOfDeath = Resource(
                String(),
                patientEntity.causeOfDeath,
                ArrayList(),
                0
            )
        }
        patient.isDeceased = patientEntity.deceased == "true"
        patient.attributes = patientEntity.attributes
        return patient
    }

    @JvmStatic
    fun convert(patient: Patient): PatientEntity {
        val patientEntity =
            PatientEntity()
        patientEntity.display = patient.name?.nameString
        patientEntity.uuid = patient.uuid

        //#region -- Contact Details --
        patientEntity.contactPhoneNumber = patient.contactPhoneNumber
        if (patient.contact != null) {
            patientEntity.contactFirstName = patient.contact.givenName
            patientEntity.contactLastName = patient.contact.familyName
        }
        //#endregion

        patientEntity.isSynced = patient.isSynced
        if (patient.identifier != null) {
            patientEntity.identifier = patient.identifier.identifier
            patientEntity.identifierUuid = patient.identifier.uuid
        } else {
            patientEntity.identifier = null
        }
        patientEntity.givenName = patient.name?.givenName
        patientEntity.familyName = patient.name?.familyName
        patientEntity.gender = patient.gender
        patientEntity.birthDate = patient.birthdate
        patientEntity.deathDate = null
        if (null != patient.causeOfDeath) {
            if (patient.causeOfDeath.display == null) {
                patientEntity.causeOfDeath = null
            } else {
                patientEntity.causeOfDeath = patient.causeOfDeath.display
            }
        } else {
            patientEntity.causeOfDeath = null
        }
        patientEntity.age = null
        if (null != patient.address) {
            patientEntity.address_1 = patient.address.address1
            patientEntity.address_2 = patient.address.address2
            patientEntity.postalCode = patient.address.postalCode
            patientEntity.country = patient.address.country
            patientEntity.state = patient.address.stateProvince
            patientEntity.city = patient.address.cityVillage
        }
        patientEntity.encounters = patient.encounters
        patientEntity.deceased = patient.isDeceased.toString()
        patientEntity.attributes = patient.attributes
        return patientEntity
    }

    @JvmStatic
    fun <T> createObservableIO(func: Callable<T>?): Observable<T> {
        return Observable.fromCallable(func)
            .subscribeOn(Schedulers.io())
    }

    @JvmStatic
    fun convert(diagnosis: Diagnosis, encounterID: Long): DiagnosisEntity {
        val diagnosisEntity = DiagnosisEntity()
        diagnosisEntity.uuid = diagnosis.uuid
        diagnosisEntity.display = diagnosis.display
        diagnosisEntity.links = diagnosis.links
        diagnosisEntity.encounterId = encounterID

        return diagnosisEntity
    }
}
