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

package edu.upc.openmrs.activities.providermanagerdashboard.addprovider;

import edu.upc.sdk.library.models.Person;
import edu.upc.sdk.library.models.Provider;

import java.util.ArrayList;
import java.util.List;

import edu.upc.openmrs.activities.BasePresenterContract;
import edu.upc.openmrs.activities.BaseView;

public class AddProviderContract {
    public interface View extends BaseView<Presenter> {
        boolean validateFields();
    }

    public interface Presenter extends BasePresenterContract {
        Person createPerson(String firstName, String lastName);

        Provider createNewProvider(Person person, String identifier);

        Provider editExistingProvider(Provider provider, Person person, String identifier);

        List<Provider> getMatchingProviders(ArrayList<Provider> providers, Provider currentProvider);
    }
}
