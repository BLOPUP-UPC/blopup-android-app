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

package edu.upc.sdk.library.api.repository;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.upc.BuildConfig;
import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.api.RestServiceBuilder;
import edu.upc.sdk.library.dao.EncounterCreateRoomDAO;
import edu.upc.sdk.library.dao.PatientDAO;
import edu.upc.sdk.library.databases.AppDatabaseHelper;
import edu.upc.sdk.library.models.Encountercreate;
import edu.upc.sdk.library.models.IdGenPatientIdentifiers;
import edu.upc.sdk.library.models.IdentifierType;
import edu.upc.sdk.library.models.Patient;
import edu.upc.sdk.library.models.PatientDto;
import edu.upc.sdk.library.models.PatientIdentifier;
import edu.upc.sdk.library.models.PersonAttribute;
import edu.upc.sdk.library.models.PersonAttributeType;
import edu.upc.sdk.library.models.PersonName;
import edu.upc.sdk.library.models.ResultType;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.utilities.PatientComparator;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;

/**
 * The type Patient repository.
 */
@Singleton
public class PatientRepository extends BaseRepository {
    private final PatientDAO patientDAO;
    private final LocationRepository locationRepository;

    /**
     * Instantiates a new Patient repository.
     */
    @Inject
    public PatientRepository() {
        super(null);
        this.patientDAO = new PatientDAO();
        this.locationRepository = new LocationRepository();
    }

    /**
     * Uploads a patient to the server.
     *
     * @param patient the patient to be registered in the server
     */
    public Observable<Patient> syncPatient(final Patient patient) {
        return AppDatabaseHelper.createObservableIO(() -> {
            final List<PatientIdentifier> identifiers = new ArrayList<>();
            final PatientIdentifier identifier = new PatientIdentifier();
            identifier.setLocation(locationRepository.getLocation());
            identifier.setIdentifier(getIdGenPatientIdentifier());
            identifier.setIdentifierType(getPatientIdentifierType());
            identifiers.add(identifier);

            patient.setIdentifiers(identifiers);

            PatientDto patientDto = patient.getPatientDto();

            Response<PatientDto> response = restApi.createPatient(patientDto).execute();
            if (response.isSuccessful()) {
                PatientDto returnedPatientDto = response.body();

                patient.setUuid(returnedPatientDto.getUuid());

                if (!patient.getEncounters().isEmpty()) {
                    addEncounters(patient);
                }

                return patient;
            } else {
                throw new Exception("syncPatient error: " + response.message());
            }
        });
    }

    /**
     * Registers a patient locally or to the server, according to network state.
     *
     * @param patient the patient to be registered
     * @return Observable result type of registration process
     */
    public Observable<Patient> registerPatient(final Patient patient) {
        return AppDatabaseHelper.createObservableIO(() -> {
                syncPatient(patient).single().toBlocking().first();
                Long id = patientDAO.savePatient(patient).single().toBlocking().first();
                patient.setId(id);
                return patient;
        });
    }

    public Observable<Patient> registerPatient(String name, String familyName, String dateOfBirth, Boolean isBirthdateEstimated, String gender, String countryOfBirth) {

        PersonName personName = new PersonName();
        personName.setGivenName(name);
        personName.setFamilyName(familyName);

        ArrayList<PersonName> names = new ArrayList<>();
        names.add(personName);

        Patient patient = new Patient();
        patient.setDeceased(false);
        patient.setNames(names);

        patient.setBirthdate(dateOfBirth);
        patient.setBirthdateEstimated(isBirthdateEstimated);

        patient.setGender(gender);

        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setUuid(BuildConfig.COUNTRY_OF_BIRTH_ATTRIBUTE_TYPE_UUID);

        PersonAttribute personAttribute = new PersonAttribute();
        personAttribute.setAttributeType(personAttributeType);
        personAttribute.setValue(countryOfBirth);

        ArrayList<PersonAttribute> attributes = new ArrayList<>();
        attributes.add(personAttribute);

        patient.setAttributes(attributes);

        return AppDatabaseHelper.createObservableIO(() -> {
            syncPatient(patient).single().toBlocking().first();
            Long id = patientDAO.savePatient(patient).single().toBlocking().first();
            patient.setId(id);
            return patient;
        });
    }

    /**
     * Updates patient locally and remotely.
     *
     * @param patient the patient
     * @return Observable result type
     */
    public Observable<ResultType> updatePatient(final Patient patient) {
        return AppDatabaseHelper.createObservableIO(() -> {
                Call<PatientDto> call = restApi.updatePatient(
                        patient.getUpdatedPatientDto(), patient.getUuid(), "full");
                Response<PatientDto> response = call.execute();

                if (response.isSuccessful()) {
                    PatientDto patientDto = response.body();
                    patient.setBirthdate(patientDto.getPerson().getBirthdate());
                    patient.setUuid(patientDto.getUuid());

                    patientDAO.updatePatient(patient.getId(), patient);

                    return ResultType.PatientUpdateSuccess;
                } else {
                    throw new Exception("updatePatient error: " + response.message());
                }
        });
    }

    /**
     * Download patient by uuid.
     *
     * @param uuid patient uuid
     * @return Patient observable
     */
    public Observable<Patient> downloadPatientByUuid(@NonNull final String uuid) {
        return AppDatabaseHelper.createObservableIO(() -> {
            Call<PatientDto> call = restApi.getPatientByUUID(uuid, "full");
            Response<PatientDto> response = call.execute();
            if (response.isSuccessful()) {
                final PatientDto newPatientDto = response.body();

                return newPatientDto.getPatient();
            } else {
                throw new IOException("Error with downloading patient: " + response.message());
            }
        });
    }

    /**
     * Add encounters.
     *
     * @param patient the patient
     */
    public void addEncounters(Patient patient) {
        EncounterCreateRoomDAO dao = db.encounterCreateRoomDAO();
        String enc = patient.getEncounters();
        List<Long> list = new ArrayList<>();
        for (String s : enc.split(","))
            list.add(Long.parseLong(s));

        for (long id : list) {
            Encountercreate encountercreate = dao.getCreatedEncountersByID(id);
            encountercreate.setPatient(patient.getUuid());
            dao.updateExistingEncounter(encountercreate);
        }
    }

    /**
     * Gets id gen patient identifier.
     *
     * @return the id gen patient identifier
     */
    public String getIdGenPatientIdentifier() throws IOException {
        IdGenPatientIdentifiers idList = null;

        RestApi patientIdentifierService = RestServiceBuilder.createServiceForPatientIdentifier(RestApi.class);
        Call<IdGenPatientIdentifiers> call = patientIdentifierService.getPatientIdentifiers(OpenmrsAndroid.getUsername(), OpenmrsAndroid.getPassword());

        Response<IdGenPatientIdentifiers> response = call.execute();
        if (response.isSuccessful()) {
            idList = response.body();
        }

        assert idList != null;
        return idList.getIdentifiers().get(0);
    }

    /**
     * Gets patient identifier type (only has uuid).
     *
     * @return the patient identifier type
     */
    public IdentifierType getPatientIdentifierType() throws IOException {
        Call<Results<IdentifierType>> call = restApi.getIdentifierTypes();
        Response<Results<IdentifierType>> response = call.execute();
        if (response.isSuccessful()) {
            Results<IdentifierType> idResList = response.body();
            for (IdentifierType result : idResList.getResults()) {
                if (result.getDisplay().equals("OpenMRS ID")) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Fetches similar patients by different strategies:
     * <br> 1. Fetch similar patients from server directly using an API.
     *
     * @param patient to find similar patients to
     * @return Observable list of similar patients
     */
    public Observable<List<Patient>> fetchSimilarPatients(final Patient patient) {
        return AppDatabaseHelper.createObservableIO(() -> {
                List<Patient> localPatients = patientDAO.getAllPatients().toBlocking().first();
                return new PatientComparator().findSimilarPatient(localPatients, patient);
        });
    }
}
