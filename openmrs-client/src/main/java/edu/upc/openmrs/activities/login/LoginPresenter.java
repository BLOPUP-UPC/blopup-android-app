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

package edu.upc.openmrs.activities.login;

import androidx.annotation.NonNull;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.upc.R;
import edu.upc.openmrs.activities.BasePresenter;
import edu.upc.openmrs.application.OpenMRS;
import edu.upc.openmrs.net.AuthorizationManager;
import edu.upc.openmrs.services.UserService;
import edu.upc.sdk.library.OpenMRSLogger;
import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.api.RestServiceBuilder;
import edu.upc.sdk.library.dao.LocationDAO;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.library.models.Session;
import edu.upc.sdk.utilities.ApplicationConstants;
import edu.upc.sdk.utilities.NetworkUtils;
import edu.upc.sdk.utilities.StringUtils;
import edu.upc.sdk.utilities.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginPresenter extends BasePresenter implements LoginContract.Presenter {
    private final RestApi restApi;
    private final UserService userService;
    private final LoginContract.View loginView;
    private final OpenMRS mOpenMRS;
    private final OpenMRSLogger mLogger;
    private final AuthorizationManager authorizationManager;
    private final LocationDAO locationDAO;
    private boolean mWipeRequired;

    public LoginPresenter(LoginContract.View loginView, OpenMRS openMRS) {
        this.loginView = loginView;
        this.mOpenMRS = openMRS;
        this.mLogger = OpenmrsAndroid.getOpenMRSLogger();
        this.loginView.setPresenter(this);
        this.authorizationManager = new AuthorizationManager();
        this.locationDAO = new LocationDAO();
        this.restApi = RestServiceBuilder.createService(RestApi.class);
        this.userService = new UserService();
    }

    public LoginPresenter(RestApi restApi, LocationDAO locationDAO,
                          UserService userService, LoginContract.View loginView, OpenMRS mOpenMRS,
                          OpenMRSLogger mLogger, AuthorizationManager authorizationManager) {
        this.restApi = restApi;
        this.locationDAO = locationDAO;
        this.userService = userService;
        this.loginView = loginView;
        this.mOpenMRS = mOpenMRS;
        this.mLogger = mLogger;
        this.authorizationManager = authorizationManager;
        this.loginView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        // This method is intentionally empty
    }

    @Override
    public void login(String username, String password, String url, String oldUrl) {
        if (validateLoginFields(username, password, url)) {
            loginView.hideSoftKeys();
            if ((!OpenmrsAndroid.getUsername().equals(ApplicationConstants.EMPTY_STRING) &&
                    !OpenmrsAndroid.getUsername().equals(username)) ||
                    ((!OpenmrsAndroid.getServerUrl().equals(ApplicationConstants.EMPTY_STRING) &&
                            !OpenmrsAndroid.getServerUrl().equals(oldUrl))) ||
                    (!OpenmrsAndroid.getHashedPassword().equals(ApplicationConstants.EMPTY_STRING) &&
                            !BCrypt.checkpw(password, OpenmrsAndroid.getHashedPassword())) ||
                    mWipeRequired) {
                loginView.showWarningDialog();
            } else {
                authenticateUser(username, password, url);
            }
        }
    }

    @Override
    public void authenticateUser(final String username, final String password, final String url) {
        authenticateUser(username, password, url, mWipeRequired);
    }

    @Override
    public void authenticateUser(final String username, final String password, final String url, final boolean wipeDatabase) {
        loginView.showLoadingAnimation();
        if (NetworkUtils.isOnline()) {
            mWipeRequired = wipeDatabase;

            RestApi restApi = RestServiceBuilder.createService(RestApi.class, username, password);
            Call<Session> call = restApi.getSession();
            call.enqueue(new Callback<Session>() {
                @Override
                public void onResponse(@NonNull Call<Session> call, @NonNull Response<Session> response) {
                    if (response.isSuccessful()) {
                        mLogger.d(response.body().toString());
                        Session session = response.body();
                        if (session.isAuthenticated()) {
                            OpenmrsAndroid.deleteSecretKey();
                            String sessionID = getSessionIdFromHeaders(response);

                            if (wipeDatabase) {
                                mOpenMRS.deleteDatabase(ApplicationConstants.DB_NAME);
                                setData(sessionID, url, username, password);
                                mWipeRequired = false;
                            }
                            if (authorizationManager.isUserNameOrServerEmpty()) {
                                setData(sessionID, url, username, password);
                            } else {
                                OpenmrsAndroid.setSessionToken(sessionID);
                                OpenmrsAndroid.setPasswordAndHashedPassword(password);
                            }

                            OpenmrsAndroid.setVisitTypeUUID(ApplicationConstants.DEFAULT_VISIT_TYPE_UUID);
                            setLogin(true, url);
                            userService.updateUserInformation(username);

                            loginView.userAuthenticated();
                            loginView.finishLoginActivity();
                        } else {
                            loginView.hideLoadingAnimation();
                            loginView.showInvalidLoginOrPasswordSnackbar();
                        }
                    } else {
                        loginView.hideLoadingAnimation();
                        loginView.showToast(response.message(), ToastUtil.ToastType.ERROR);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Session> call, @NonNull Throwable t) {
                    loginView.hideLoadingAnimation();
                    loginView.showToast(t.getMessage(), ToastUtil.ToastType.ERROR);
                }
            });
        } else {
            if (OpenmrsAndroid.isUserLoggedOnline() && url.equals(OpenmrsAndroid.getLastLoginServerUrl())) {
                if (OpenmrsAndroid.getUsername().equals(username) && BCrypt.checkpw(password, OpenmrsAndroid.getHashedPassword())) {
                    OpenmrsAndroid.deleteSecretKey();
                    OpenmrsAndroid.setPasswordAndHashedPassword(password);
                    OpenmrsAndroid.setSessionToken(OpenmrsAndroid.getLastSessionToken());
                    loginView.showToast(R.string.login_offline_toast_message,
                            ToastUtil.ToastType.NOTICE);
                    loginView.userAuthenticated();
                    loginView.finishLoginActivity();
                } else {
                    loginView.hideLoadingAnimation();
                    loginView.showToast(R.string.auth_failed_dialog_message,
                            ToastUtil.ToastType.ERROR);
                }
            } else if (NetworkUtils.hasNetwork()) {
                loginView.showToast(R.string.offline_mode_unsupported_in_first_login,
                        ToastUtil.ToastType.ERROR);
                loginView.hideLoadingAnimation();
            } else {
                loginView.showToast(R.string.no_internet_connection_message,
                        ToastUtil.ToastType.ERROR);
                loginView.hideLoadingAnimation();
            }
        }
    }

    private static String getSessionIdFromHeaders(@NonNull Response<Session> response) {
        // Just blame OpenMRS
        return Arrays.stream(response.headers().get("Set-Cookie").split(";")).filter(cookie -> cookie.contains("JSESSIONID")).findFirst().get().split("=")[1];
    }

    @Override
    public void saveLocationsToDatabase(List<LocationEntity> locationList, String selectedLocation) {
        OpenmrsAndroid.setLocation(selectedLocation);
        locationDAO.deleteAllLocations();
        for (int i = 0; i < locationList.size(); i++) {
            locationDAO.saveLocation(locationList.get(i))
                    .observeOn(Schedulers.io())
                    .subscribe();
        }
    }

    @Override
    public void loadLocations(final String url) {
        loginView.showLocationLoadingAnimation();

        if (NetworkUtils.hasNetwork()) {
            String locationEndPoint = url + ApplicationConstants.API.REST_ENDPOINT + "location";
            Call<Results<LocationEntity>> call =
                    restApi.getLocations(locationEndPoint, "Login Location", "full");
            call.enqueue(new Callback<Results<LocationEntity>>() {
                @Override
                public void onResponse(@NonNull Call<Results<LocationEntity>> call, @NonNull Response<Results<LocationEntity>> response) {
                    if (response.isSuccessful()) {
                        RestServiceBuilder.changeBaseUrl(url.trim());
                        OpenmrsAndroid.setServerUrl(url);
                        loginView.initLoginForm(response.body().getResults(), url);
                        loginView.startFormListService();
                        loginView.setLocationErrorOccurred(false);
                    } else {
                        loginView.showInvalidURLSnackbar(R.string.snackbar_server_error);
                        loginView.setLocationErrorOccurred(true);
                        loginView.initLoginForm(new ArrayList<>(), url);
                    }
                    loginView.hideUrlLoadingAnimation();
                }

                @Override
                public void onFailure(@NonNull Call<Results<LocationEntity>> call, @NonNull Throwable t) {
                    loginView.hideUrlLoadingAnimation();
                    loginView.showInvalidURLSnackbar(t.getMessage());
                    loginView.initLoginForm(new ArrayList<>(), url);
                    loginView.setLocationErrorOccurred(true);
                }
            });
        } else {
            addSubscription(locationDAO.getLocations()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(locationEntities -> {
                        if (locationEntities.size() > 0) {
                            loginView.initLoginForm(locationEntities, url);
                            loginView.setLocationErrorOccurred(false);
                        } else {
                            loginView.showToast(R.string.no_internet_connection_message, ToastUtil.ToastType.ERROR);
                            loginView.setLocationErrorOccurred(true);
                        }
                        loginView.hideLoadingAnimation();
                    }));
        }
    }

    private boolean validateLoginFields(String username, String password, String url) {
        return StringUtils.notEmpty(username) || StringUtils.notEmpty(password) || StringUtils.notEmpty(url);
    }

    // use this method to populate the Openmrs username password and everything else.

    private void setData(String sessionToken, String url, String username, String password) {
        OpenmrsAndroid.setSessionToken(sessionToken);
        OpenmrsAndroid.setServerUrl(url);
        OpenmrsAndroid.setUsername(username);
        OpenmrsAndroid.setPasswordAndHashedPassword(password);
    }

    private void setLogin(boolean isLogin, String serverUrl) {
        OpenmrsAndroid.setUserLoggedOnline(isLogin);
        OpenmrsAndroid.setLastLoginServerUrl(serverUrl);
    }
}
