/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package edu.upc.sdk.library.models;

import android.graphics.Bitmap;

import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.upc.sdk.library.models.typeConverters.PersonNameConverter;
import edu.upc.sdk.utilities.StringUtils;

/**
 * The type Patient.
 *
 * <p>More on Type Patient <a href="https://rest.openmrs.org/#patients">...</a> </p>
 */
public class Patient extends Person implements Serializable {
    private Long id;
    private String encounters = "";
    @SerializedName("identifiers")
    @Expose
    private List<PatientIdentifier> identifiers = new ArrayList<>();

    @TypeConverters(PersonNameConverter.class)
    @SerializedName("contactNames")
    @Expose
    private List<PersonName> contactNames = new ArrayList<>();
    //Phone Number
    @SerializedName("contactPhoneNumber")
    @Expose
    private String contactPhoneNumber;

    /**
     * Instantiates a new Patient.
     */
    public Patient() {
    }

    /**
     * Instantiates a new Patient.
     *
     * @param id          the id
     * @param encounters  the encounters
     * @param identifiers the identifiers
     */
    public Patient(Long id, String encounters, List<PatientIdentifier> identifiers) {
        this.id = id;
        this.encounters = encounters;
        this.identifiers = identifiers;
    }

    /**
     * Instantiates a new Patient.
     *
     * @param id                 the id
     * @param encounters         the encounters
     * @param identifiers        the identifiers
     * @param names              the names
     * @param gender             the gender
     * @param birthdate          the birthdate
     * @param birthdateEstimated the birthdate estimated
     * @param addresses          the addresses
     * @param attributes         the attributes
     * @param photo              the photo
     * @param causeOfDeath       the cause of death
     * @param dead               the dead
     */
    /*Constructor to initialize values of current class as well as parent class*/
    public Patient(Long id, String encounters, List<PatientIdentifier> identifiers,
                   List<PersonName> names, String gender, String birthdate,
                   boolean birthdateEstimated, List<PersonAddress> addresses,
                   List<PersonAttribute> attributes,
                   Bitmap photo, Resource causeOfDeath, boolean dead, boolean voided) {
        super(names, gender, birthdate, birthdateEstimated, addresses, attributes, photo, causeOfDeath, dead, voided);
        this.id = id;
        this.encounters = encounters;
        this.identifiers = identifiers;
    }

    /**
     * Instantiates a new Patient.
     *
     * @param id                 the id
     * @param encounters         the encounters
     * @param identifiers        the identifiers
     * @param names              the names
     * @param gender             the gender
     * @param birthdate          the birthdate
     * @param birthdateEstimated the birthdate estimated
     * @param addresses          the addresses
     * @param attributes         the attributes
     * @param photo              the photo
     * @param causeOfDeath       the cause of death
     * @param dead               the dead
     */
    /*Constructor to initialize values of current class as well as parent class*/
    public Patient(Long id, String encounters, List<PatientIdentifier> identifiers,
                   List<PersonName> names, String gender, String birthdate, boolean birthdateEstimated, List<PersonAddress> addresses, List<PersonAttribute> attributes,
                   Bitmap photo, Resource causeOfDeath, boolean dead, String contactPhoneNumber, List<PersonName> contactNames) {
        super(names, gender, birthdate, birthdateEstimated, addresses, attributes, photo, causeOfDeath, dead, false);
        this.id = id;
        this.encounters = encounters;
        this.identifiers = identifiers;
        this.contactPhoneNumber = contactPhoneNumber;
        this.contactNames = contactNames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets person.
     *
     * @return the person
     */
    @SerializedName("person")
    public Person getPerson() {
        return new Person(getNames(),
                getGender(),
                getBirthdate(),
                getBirthdateEstimated(),
                getAddresses(), getAttributes(),
                getPhoto(),
                getCauseOfDeath(),
                isDeceased(),
                isVoided());
    }

    /**
     * Gets updated person.
     *
     * @return the updated person
     */
    public PersonUpdate getUpdatedPerson() {
        String causeOfDeath = (getCauseOfDeath() != null) ? getCauseOfDeath().getUuid() : null;
        return new PersonUpdate(getNames(), getGender(), getBirthdate(), getBirthdateEstimated(), getAddresses(), getAttributes(), getPhoto(), causeOfDeath, isDeceased());
    }

    /**
     * Gets patient dto.
     *
     * @return the patient dto
     */
    public PatientDto getPatientDto() {
        PatientDto patientDto = new PatientDto();
        patientDto.setPerson(getPerson());
        patientDto.setIdentifiers(getIdentifiers());
        if (getUuid() != null && !getUuid().isEmpty()) {
            patientDto.setUuid(getUuid());
        }
        return patientDto;
    }

    /**
     * Gets updated patient dto.
     *
     * @return the updated patient dto
     */
    public PatientDtoUpdate getUpdatedPatientDto() {
        PatientDtoUpdate patientDtoUpdate = new PatientDtoUpdate();
        patientDtoUpdate.setIdentifiers(getIdentifiers());
        patientDtoUpdate.setPerson(getUpdatedPerson());
        return patientDtoUpdate;
    }

    /**
     * Gets identifiers.
     *
     * @return The identifiers
     */
    public List<PatientIdentifier> getIdentifiers() {
        return identifiers;
    }

    /**
     * Sets identifiers.
     *
     * @param identifiers The identifiers
     */
    public void setIdentifiers(List<PatientIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    /**
     * Gets identifier.
     *
     * @return the identifier
     */
    public PatientIdentifier getIdentifier() {
        if (!identifiers.isEmpty()) {
            return identifiers.get(0);
        } else {
            return null;
        }
    }

    /**
     * Is synced boolean.
     *
     * @return the boolean
     */
    public boolean isSynced() {
        return !StringUtils.isBlank(getUuid());
        //Keeping it this way until the synced flag can be made to work
    }

    /**
     * Gets encounters.
     *
     * @return the encounters
     */
    public String getEncounters() {
        return encounters;
    }

    /**
     * Sets encounters.
     *
     * @param encounters the encounters
     */
    public void setEncounters(String encounters) {
        this.encounters = encounters;
    }

    public PersonName getContact() {
        if (!contactNames.isEmpty()) {
            return contactNames.get(0);
        } else {
            return null;
        }
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

}
