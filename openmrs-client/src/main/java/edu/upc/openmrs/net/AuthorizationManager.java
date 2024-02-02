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

package edu.upc.openmrs.net;

import android.content.Intent;

import edu.upc.openmrs.activities.login.LoginActivity;
import edu.upc.openmrs.application.OpenMRS;
import edu.upc.sdk.library.OpenmrsAndroid;

public class AuthorizationManager {
    protected final OpenMRS mOpenMRS = OpenMRS.getInstance();

    public boolean isUserNameOrServerEmpty() {
        return OpenmrsAndroid.getUsername().isEmpty() || OpenmrsAndroid.getServerUrl().isEmpty();
    }

    public boolean isUserLoggedIn() {
        return !OpenmrsAndroid.getSessionToken().isEmpty();
    }

    public void moveToLoginActivity() {
        Intent intent = new Intent(mOpenMRS.getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mOpenMRS.getApplicationContext().startActivity(intent);
    }
}
