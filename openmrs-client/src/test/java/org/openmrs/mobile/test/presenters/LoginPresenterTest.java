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

package org.openmrs.mobile.test.presenters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;

import com.openmrs.android_sdk.library.OpenMRSLogger;
import com.openmrs.android_sdk.library.OpenmrsAndroid;
import com.openmrs.android_sdk.library.api.RestApi;
import com.openmrs.android_sdk.library.api.RestServiceBuilder;
import com.openmrs.android_sdk.library.api.repository.VisitRepository;
import com.openmrs.android_sdk.library.dao.EncounterDAO;
import com.openmrs.android_sdk.library.dao.LocationDAO;
import com.openmrs.android_sdk.library.dao.VisitDAO;
import com.openmrs.android_sdk.library.databases.entities.LocationEntity;
import com.openmrs.android_sdk.library.models.Session;
import com.openmrs.android_sdk.library.models.User;
import com.openmrs.android_sdk.library.models.VisitType;
import com.openmrs.android_sdk.utilities.ApplicationConstants;
import com.openmrs.android_sdk.utilities.NetworkUtils;
import com.openmrs.android_sdk.utilities.StringUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.login.LoginContract;
import org.openmrs.mobile.activities.login.LoginFragment;
import org.openmrs.mobile.activities.login.LoginPresenter;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.net.AuthorizationManager;
import org.openmrs.mobile.services.UserService;
import org.openmrs.mobile.test.ACUnitTestBaseRx;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.ArrayList;
import java.util.Collections;

import rx.Observable;

@PrepareForTest({OpenMRS.class, NetworkUtils.class, LocationDAO.class, RestServiceBuilder.class,
    StringUtils.class,OpenmrsAndroid.class})
@PowerMockIgnore("javax.net.ssl.*")
public class LoginPresenterTest extends ACUnitTestBaseRx {
    @Mock
    private OpenMRS openMRS;
    @Mock
    private OpenMRSLogger openMRSLogger;
    @Mock
    private AuthorizationManager authorizationManager;
    @Mock
    private RestApi restApi;
    @Mock
    private LoginContract.View view;
    @Mock
    private LocationDAO locationDAO;
    @Mock
    private EncounterDAO encounterDAO;
    @Mock
    private VisitDAO visitDAO;
    @Mock
    private UserService userService;
    private LoginPresenter presenter;

    MockedStatic<RestServiceBuilder> mockedStatic;

    @BeforeClass
    public static void setup() {
        Mockito.mockStatic(OpenMRS.class);
        Mockito.mockStatic(LocationDAO.class);
        Mockito.mockStatic(StringUtils.class);
        Mockito.mockStatic(NetworkUtils.class);
        Mockito.mockStatic(OpenmrsAndroid.class);
    }

    @Before
    public void setUp() {
        super.setUp();
        VisitRepository visitRepository = new VisitRepository(openMRSLogger, restApi, visitDAO, locationDAO, encounterDAO);
        presenter = new LoginPresenter(restApi, visitRepository, locationDAO, userService, view, openMRS,
            openMRSLogger, authorizationManager);

        Mockito.lenient().when(OpenmrsAndroid.getServerUrl()).thenReturn("http://www.some_server_url.com");
        Mockito.lenient().when(OpenmrsAndroid.getHashedPassword()).thenReturn(ApplicationConstants.EMPTY_STRING);
        Mockito.when(OpenMRS.getInstance()).thenReturn(openMRS);
        mockedStatic = Mockito.mockStatic(RestServiceBuilder.class);
        Mockito.when(RestServiceBuilder.createService(any(), any(), any())).thenReturn(restApi);
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        mockedStatic.close();
    }

    @Test
    public void shouldNotLoginUser_emptyCredentials() {
        mockNonEmptyCredentials(false);
        presenter.login("", "", "some_url", "some_old_url");
        verify(view, never()).showWarningDialog();
        verify(view, never()).showLoadingAnimation();
    }

    @Test
    public void shouldShowWipingDBWarningDialog_newUsernameAndUrl() {
        mockNonEmptyCredentials(true);
        mockLastUser("newUser", "pass", "newUrl");
        presenter.login("oldUsername", "pass", "some_url", "some_old_url");
        verify(view).hideSoftKeys();
        verify(view).showWarningDialog();
    }

    @Test
    public void shouldLoginUserInOnlineMode_noWipe_userAuthenticated() {
        mockNonEmptyCredentials(true);
        mockOnlineMode(true);
        Mockito.lenient().when(restApi.getSession())
            .thenReturn(mockSuccessCall(new Session("someId", true, new User())));
        Mockito.lenient().when(restApi.getVisitType())
            .thenReturn(mockSuccessCall(Collections.singletonList(new VisitType("visitType"))));
        Mockito.lenient().when(authorizationManager.isUserNameOrServerEmpty()).thenReturn(false);
        String user = "user";
        String url = "url";
        String password = "pass";
        mockLastUser(user, password, url);
        presenter.login(user, password, url, url);

        verify(view).showLoadingAnimation();
        verify(view).userAuthenticated();
        verify(view).finishLoginActivity();
    }

    @Test
    public void shouldLoginUserInOnlineMode_noWipe_userNotAuthenticated() {
        mockNonEmptyCredentials(true);
        mockOnlineMode(true);
        Mockito.lenient().when(restApi.getSession())
            .thenReturn(mockSuccessCall(new Session("someId", false, new User())));
        Mockito.lenient().when(restApi.getVisitType())
            .thenReturn(mockSuccessCall(Collections.singletonList(new VisitType("visitType"))));
        Mockito.lenient().when(authorizationManager.isUserNameOrServerEmpty()).thenReturn(false);
        String user = "user";
        String url = "url";
        String password = "pass";
        mockLastUser(user, password, url);
        presenter.login(user, password, url, url);

        verify(view).showLoadingAnimation();
        verify(view).showInvalidLoginOrPasswordSnackbar();
    }

    @Test
    public void shouldLoginUserInOnlineMode_errorResponse() {
        mockNonEmptyCredentials(true);
        mockOnlineMode(true);
        Mockito.lenient().when(restApi.getSession()).thenReturn(mockErrorCall(401));
        String user = "user";
        String url = "url";
        String password = "pass";
        mockLastUser(user, password, url);
        presenter.login(user, password, url, url);

        verify(view).hideLoadingAnimation();
        verify(view).showToast(anyString(), any());
    }

    @Test
    public void shouldLoginUserInOnlineMode_failure() {
        mockNonEmptyCredentials(true);
        mockOnlineMode(true);
        Mockito.lenient().when(restApi.getSession()).thenReturn(mockFailureCall());
        String user = "user";
        String url = "url";
        String password = "pass";
        mockLastUser(user, password, url);
        presenter.login(user, password, url, url);

        verify(view).hideLoadingAnimation();
        verify(view).showToast(Mockito.any(), any());
    }

    @Test
    public void shouldLoginUserInOfflineMode_userLoggedBefore_sameUrl() {
        String user = "user";
        String url = "url";
        String password = "pass";
        mockLastUser(user, password, url);
        mockNonEmptyCredentials(true);
        mockOnlineMode(false);
        Mockito.lenient().when(OpenmrsAndroid.isUserLoggedOnline()).thenReturn(true);
        Mockito.lenient().when(OpenmrsAndroid.getLastLoginServerUrl()).thenReturn(url);
        presenter.login(user, password, url, url);

        verify(view).showToast(anyInt(), any());
        verify(view).userAuthenticated();
        verify(view).finishLoginActivity();
    }

    @Test
    public void shouldShowWipingDBWarningDialogUserInOfflineMode_userLoggedBefore_wrongCredentials() {
        String user = "user";
        String url = "url";
        String password = "pass";
        mockLastUser(user, "newPass", url);
        mockNonEmptyCredentials(true);
        mockOnlineMode(false);
        Mockito.lenient().when(OpenmrsAndroid.isUserLoggedOnline()).thenReturn(true);
        Mockito.lenient().when(OpenmrsAndroid.getLastLoginServerUrl()).thenReturn(url);
        presenter.login(user, password, url, url);

        verify(view).hideSoftKeys();
        verify(view).showWarningDialog();
    }

    @Test
    public void shouldLoadLocationsInOnlineMode_allOK() {
        mockNetworkConnection(true);
        Mockito.lenient().when(restApi.getLocations(any(), anyString(), anyString()))
            .thenReturn(mockSuccessCall(Collections.singletonList(new LocationEntity(""))));
        presenter.loadLocations("someUrl");
        verify(view).initLoginForm(any(), any());
        verify(view).startFormListService();
        verify(view).setLocationErrorOccurred(false);
        verify(view).hideUrlLoadingAnimation();
    }

    @Test
    public void shouldLoadLocationsInOnlineMode_errorResponse() {
        mockNetworkConnection(true);
        Mockito.lenient().when(restApi.getLocations(any(), anyString(), anyString()))
            .thenReturn(mockErrorCall(401));

        presenter.loadLocations("someUrl");
        verify(view).initLoginForm(any(), any());
        verify(view).setLocationErrorOccurred(true);
        verify(view).showInvalidURLSnackbar(R.string.snackbar_server_error);
        verify(view).hideUrlLoadingAnimation();
    }

    @Test
    public void shouldLoadLocationsInOnlineMode_failure() {
        mockNetworkConnection(true);
        Mockito.lenient().when(restApi.getLocations(any(), anyString(), anyString()))
            .thenReturn(mockFailureCall());
        presenter.loadLocations("someUrl");
        verify(view).initLoginForm(any(), any());
        verify(view).setLocationErrorOccurred(true);
        verify(view).showInvalidURLSnackbar(Mockito.any());
        verify(view).hideUrlLoadingAnimation();
    }

    @Test
    public void shouldLoadLocationsInOfflineMode_emptyList() {
        mockNetworkConnection(false);
        Mockito.lenient().when(locationDAO.getLocations()).thenReturn(Observable.just(new ArrayList<>()));
        presenter.loadLocations("someUrl");
        verify(view).showToast(anyInt(), any());
        verify(view).setLocationErrorOccurred(true);
        verify(view).hideLoadingAnimation();
    }

    @Test
    public void shouldLoadLocationsInOfflineMode_nonEmptyList() {
        mockNetworkConnection(false);
        Mockito.lenient().when(locationDAO.getLocations()).thenReturn(Observable.just(Collections.singletonList(new LocationEntity(""))));
        presenter.loadLocations("someUrl");
        verify(view).initLoginForm(any(), any());
        verify(view).setLocationErrorOccurred(false);
        verify(view).hideLoadingAnimation();
    }

    @Test
    public void shouldStartFormListServiceWhenAuthenticated() {
        Context context = Mockito.mock(Context.class);
        Mockito.when(OpenMRS.getInstance()).thenReturn(openMRS);
        Mockito.when(openMRS.getApplicationContext()).thenReturn(context);
        LoginFragment loginFragment = LoginFragment.newInstance();
        try {
            loginFragment.userAuthenticated();
        } catch (NullPointerException ignored) {
        }

        verify(openMRS.getApplicationContext(), times(1)).startService(any());
    }

    private void mockNetworkConnection(boolean isNetwork) {
        Mockito.when(NetworkUtils.hasNetwork()).thenReturn(isNetwork);
    }

    private void mockNonEmptyCredentials(boolean isNonEmpty) {
        Mockito.when(StringUtils.notEmpty(anyString())).thenReturn(isNonEmpty);
    }

    private void mockOnlineMode(boolean isOnline) {
        Mockito.when(NetworkUtils.isOnline()).thenReturn(isOnline);
    }

    private void mockLastUser(String user, String password, String url) {
        Mockito.lenient().when(OpenmrsAndroid.getUsername()).thenReturn(user);
        Mockito.lenient().when(OpenmrsAndroid.getServerUrl()).thenReturn(url);
        Mockito.lenient().when(OpenmrsAndroid.getPassword()).thenReturn(password);
        Mockito.lenient().when(OpenmrsAndroid.getHashedPassword()).thenReturn(BCrypt.hashpw(password, BCrypt.gensalt(ApplicationConstants.DEFAULT_BCRYPT_ROUND)));
    }
}
