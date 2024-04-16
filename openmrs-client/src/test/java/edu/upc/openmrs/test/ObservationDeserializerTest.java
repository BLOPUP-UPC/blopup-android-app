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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import edu.upc.sdk.library.models.Observation;
import edu.upc.sdk.utilities.ObservationDeserializer;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ObservationDeserializerTest {

    @Mock
    private JsonDeserializationContext context;

    @Test
    public void shouldDeserializeMedicationTypeObservationGroupMembers() throws IOException {
        File mockResponse = new File("src/test/java/edu/upc/openmrs/test/retrofitMocks/", "obsWithMedicationTypeDataExampleResponse.json");
        String response = getMockResponseFromFile(mockResponse);

        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        Observation observation = new ObservationDeserializer().deserialize(jsonObject, Observation.class, context);
        assertThat(observation.getGroupMembers().get(0).getValueCodedName(), is(equalTo("467f7d87-8c2e-4519-9e81-048c2c7824fd")));
        assertThat(observation.getGroupMembers().get(1).getValueCodedName(), is(equalTo("2146fbb8-8a8a-44f5-81de-2bee8ec4edce")));
        assertThat(observation.getGroupMembers().get(2).getValueCodedName(), is(equalTo("f2c7ec86-6fe0-4e6a-bfe9-c73380228177")));
        assertThat(observation.getDisplay(), is(equalTo("Medication Type: ")));

    }

    private String getMockResponseFromFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }

}
