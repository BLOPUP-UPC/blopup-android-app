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

package edu.upc.openmrs.test;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import edu.upc.BuildConfig;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.library.models.Encounter;
import edu.upc.sdk.library.models.EncounterType;
import edu.upc.sdk.library.models.Observation;
import edu.upc.sdk.library.models.Patient;
import edu.upc.sdk.library.models.PatientIdentifier;
import edu.upc.sdk.library.models.PersonAddress;
import edu.upc.sdk.library.models.PersonAttribute;
import edu.upc.sdk.library.models.PersonAttributeType;
import edu.upc.sdk.library.models.PersonName;
import edu.upc.sdk.library.models.Resource;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.library.models.Visit;
import edu.upc.sdk.library.models.VisitType;
import okhttp3.Headers;
import retrofit2.Call;

@RunWith(AndroidJUnit4.class)
public abstract class ACUnitTestBase {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().silent();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    protected Patient createPatient(Long id) {
        Patient patient = new Patient(id, "",
                Collections.singletonList(createIdentifier(id)));
        patient.setUuid(UUID.randomUUID().toString());
        updatePatientData(id, patient);
        return patient;
    }

    protected PatientIdentifier createIdentifier(Long id) {
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier("some_identifier_" + id);
        return identifier;
    }

    protected Patient updatePatientData(Long id, Patient patient) {
        patient.setNames(Collections.singletonList(createPersonName(id)));
        patient.setAddresses(Collections.singletonList(createPersonAddress(id)));
        patient.setGender("M");
        patient.setBirthdate("25-02-2016");
        patient.setDeceased(false);
        patient.setCauseOfDeath(new Resource());
        patient.setAttributes(createCountryOfBirthAttribute());
        return patient;
    }

    private List<PersonAttribute> createCountryOfBirthAttribute() {
        PersonAttributeType attributeType = new PersonAttributeType();
        attributeType.setUuid(BuildConfig.COUNTRY_OF_BIRTH_ATTRIBUTE_TYPE_UUID);
        PersonAttribute personAttribute = new PersonAttribute();
        personAttribute.setAttributeType(attributeType);
        return Collections.singletonList(personAttribute);
    }

    protected PersonAddress createPersonAddress(Long id) {
        PersonAddress personAddress = new PersonAddress();
        personAddress.setAddress1("address_1_" + id);
        personAddress.setAddress2("address_2_" + id);
        personAddress.setCityVillage("city_" + id);
        personAddress.setStateProvince("state_" + id);
        personAddress.setCountry("country_" + id);
        personAddress.setPostalCode("postal_code_" + id);
        return personAddress;
    }

    protected PersonName createPersonName(Long id) {
        PersonName personName = new PersonName();
        char alphabetic_id = (char) (id.intValue() + 'a' - 1);
        personName.setGivenName("given_name_" + alphabetic_id);
        personName.setFamilyName("family_name_" + alphabetic_id);
        return personName;
    }

    protected Patient createPatient(Long id, String identifier) {
        Patient patient = createPatient(id);
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier(identifier);
        patient.setIdentifiers(Collections.singletonList(patientIdentifier));
        return patient;
    }

    protected List<Visit> createVisitList() {
        ArrayList<Visit> visits = new ArrayList();
        visits.add(createVisit("visit1", 1L));
        visits.add(createVisit("visit2", 2L));
        return visits;
    }

    protected Visit createVisit(String display, long patientId) {
        Visit visit = new Visit();
        visit.setStartDatetime("2019-01-01T00:00:00.000+0000");
        visit.setLocation(new LocationEntity(display));
        visit.visitType = new VisitType(display);
        visit.patient = createPatient(patientId);
        visit.setUuid(UUID.randomUUID().toString());
        ArrayList<Observation> observations = getObservations();
        visit.encounters = new ArrayList<>();
        Encounter encounter = new Encounter();
        encounter.setEncounterType(new EncounterType(EncounterType.VITALS));
        encounter.setObservations(observations);
        visit.encounters.add(encounter);
        return visit;
    }

    @NonNull
    private static ArrayList<Observation> getObservations() {
        Observation systolic = new Observation();
        systolic.setDisplayValue("120");
        systolic.setDisplay("Systolic");
        Observation diastolic = new Observation();
        diastolic.setDisplayValue("80");
        diastolic.setDisplay("Diastolic");
        Observation pulse = new Observation();
        pulse.setDisplayValue("90");
        pulse.setDisplay("Pulse");
        return new ArrayList<Observation>() {
            {
                add(systolic);
                add(diastolic);
                add(pulse);
            }
        };
    }

    protected <T> Call<Results<T>> mockSuccessCall(List<T> list) {
        return new MockSuccessResponse<>(list);
    }


    protected <T> Call<T> mockSuccessCall(T object) {
        return new MockSuccessResponse<>(object);
    }

    protected <T> Call<T> mockSuccessCall(T object, Headers headers) {
        return new MockSuccessResponse<>(object, headers);
    }

    protected <T> Call<T> mockErrorCall(int code) {
        return new MockErrorResponse<>(code);
    }

    protected <T> Call<T> mockFailureCall() {
        Throwable throwable = Mockito.mock(Throwable.class);
        return new MockFailure<>(throwable);
    }
}
