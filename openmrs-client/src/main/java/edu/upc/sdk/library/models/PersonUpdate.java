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

package edu.upc.sdk.library.models;

import android.graphics.Bitmap;

import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.upc.sdk.library.models.typeConverters.PersonAddressConverter;
import edu.upc.sdk.library.models.typeConverters.PersonAttributeConverter;
import edu.upc.sdk.library.models.typeConverters.PersonNameConverter;

/**
 * The type Person update.
 *
 * <p> More on Subresources of Person https://rest.openmrs.org/#person </p>
 */
public class PersonUpdate extends Resource implements Serializable {

    @TypeConverters(PersonNameConverter.class)
    @SerializedName("names")
    @Expose
    private List<PersonName> names = new ArrayList<>();
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("birthdate")
    @Expose
    private String birthdate;
    @SerializedName("birthdateEstimated")
    @Expose
    private boolean birthdateEstimated;

    @TypeConverters(PersonAddressConverter.class)
    @SerializedName("addresses")
    @Expose
    private List<PersonAddress> addresses = new ArrayList<>();

    @TypeConverters(PersonAttributeConverter.class)
    @SerializedName("attributes")
    @Expose
    private List<PersonAttribute> attributes = new ArrayList<>();

    @SerializedName("dead")
    @Expose
    private Boolean dead = false;

    @SerializedName("causeOfDeath")
    @Expose
    private String causeOfDeath = null;

    /**
     * Instantiates a new Person update.
     */
    public PersonUpdate() {
    }

    /**
     * Instantiates a new Person update.
     *
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
    public PersonUpdate(List<PersonName> names, String gender, String birthdate, boolean birthdateEstimated, List<PersonAddress> addresses, List<PersonAttribute> attributes,
                        Bitmap photo, String causeOfDeath, boolean dead) {
        this.names = names;
        this.gender = gender;
        this.birthdate = birthdate;
        this.birthdateEstimated = birthdateEstimated;
        this.addresses = addresses;
        this.attributes = attributes;
        this.causeOfDeath = causeOfDeath;
        this.dead = dead;
    }
}