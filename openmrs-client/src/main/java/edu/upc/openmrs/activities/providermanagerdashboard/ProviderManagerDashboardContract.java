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

package edu.upc.openmrs.activities.providermanagerdashboard;

import androidx.fragment.app.Fragment;

import edu.upc.sdk.library.models.Provider;

import java.util.List;

import edu.upc.openmrs.activities.BasePresenterContract;
import edu.upc.openmrs.activities.BaseView;

public interface ProviderManagerDashboardContract {
    interface View extends BaseView<Presenter> {
        void refreshUI();

        void updateAdapter(List<Provider> providerList);

        void updateVisibility(boolean visibility, String text);
    }

    interface Presenter extends BasePresenterContract {
        void getProviders(Fragment fragment);

        void updateViews(List<Provider> providerList);

        void deleteProvider(String providerUuid);

        void addProvider(Provider provider);

        void updateProvider(Provider provider);
    }
}