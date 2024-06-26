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

package edu.upc.openmrs.utilities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class URLValidatorTest {
    private static final String INVALID_URL_1;
    private static final String VALID_URL_1;
    private static final String VALID_URL_1_TRIMMED;
    private static final String VALID_URL_2;
    private static final String VALID_URL_3;
    private static final String VALID_URL_4;

    static {
        INVALID_URL_1 = "http://";
        VALID_URL_1 = "http://demo.openmrs.org/openmrs/";
        VALID_URL_1_TRIMMED = "http://demo.openmrs.org/openmrs";
        VALID_URL_2 = "https://demo.openmrs.org:8081/openmrs-standalone";
        VALID_URL_3 = "http://demo.openmrs.org/openmrs/ ";
        VALID_URL_4 = "http://10.0.2.2/openmrs";
    }

    @Test
    public void testURLValidator() {
        URLValidator.ValidationResult result;
        URLValidator.ValidationResult expected;

        result = URLValidator.validate(INVALID_URL_1);
        expected = new URLValidator.ValidationResult(false, INVALID_URL_1);
        assertEquals(expected.isURLValid(), result.isURLValid());
        assertEquals(expected.getUrl(), result.getUrl());

        result = URLValidator.validate(VALID_URL_1);
        expected = new URLValidator.ValidationResult(true, VALID_URL_1_TRIMMED);
        assertEquals(expected.isURLValid(), result.isURLValid());
        assertEquals(expected.getUrl(), result.getUrl());

        result = URLValidator.validate(VALID_URL_2);
        expected = new URLValidator.ValidationResult(true, VALID_URL_2);
        assertEquals(expected.isURLValid(), result.isURLValid());
        assertEquals(expected.getUrl(), result.getUrl());

        result = URLValidator.validate(VALID_URL_3);
        expected = new URLValidator.ValidationResult(true, VALID_URL_1_TRIMMED);
        assertEquals(expected.isURLValid(), result.isURLValid());
        assertEquals(expected.getUrl(), result.getUrl());

        result = URLValidator.validate(VALID_URL_4);
        expected = new URLValidator.ValidationResult(true, VALID_URL_4);
        assertEquals(expected.isURLValid(), result.isURLValid());
        assertEquals(expected.getUrl(), result.getUrl());
    }
}
