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

package org.openmrs.mobile.activities.login;

import androidx.annotation.NonNull;

import com.openmrs.android_sdk.library.databases.entities.LocationEntity;
import com.openmrs.android_sdk.utilities.ToastUtil;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;

import java.util.List;

public interface LoginContract {
    interface View extends BaseView<Presenter> {
        void hideSoftKeys();

        void setPresenter(@NonNull Presenter presenter);

        void showWarningDialog();

        void showLoadingAnimation();

        void hideLoadingAnimation();

        void showLocationLoadingAnimation();

        void hideUrlLoadingAnimation();

        void finishLoginActivity();

        void showInvalidURLSnackbar(String message);

        void showInvalidURLSnackbar(int messageID);

        void showInvalidLoginOrPasswordSnackbar();

        void setLocationErrorOccurred(boolean errorOccurred);

        void showToast(String message, ToastUtil.ToastType toastType);

        void showToast(int textId, ToastUtil.ToastType toastType);

        void initLoginForm(List<LocationEntity> locationList, String url);

        void userAuthenticated();

        void startFormListService();
    }

    interface Presenter extends BasePresenterContract {
        void authenticateUser(final String username, final String password, final String url);

        void authenticateUser(final String username, final String password, final String url, boolean wipeDatabase);

        void login(String username, String password, String url, String oldUrl);

        void saveLocationsToDatabase(List<LocationEntity> locationList, String selectedLocation);

        void loadLocations(String url);
    }
}
