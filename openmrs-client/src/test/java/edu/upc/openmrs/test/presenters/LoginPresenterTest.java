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

package edu.upc.openmrs.test.presenters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

import edu.upc.R;
import edu.upc.openmrs.activities.login.LoginContract;
import edu.upc.openmrs.activities.login.LoginFragment;
import edu.upc.openmrs.activities.login.LoginPresenter;
import edu.upc.openmrs.application.OpenMRS;
import edu.upc.openmrs.net.AuthorizationManager;
import edu.upc.openmrs.services.UserService;
import edu.upc.openmrs.test.ACUnitTestBaseRx;
import edu.upc.sdk.library.OpenMRSLogger;
import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.api.RestServiceBuilder;
import edu.upc.sdk.library.dao.LocationDAO;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.library.models.Session;
import edu.upc.sdk.library.models.User;
import edu.upc.sdk.library.models.VisitType;
import edu.upc.sdk.utilities.ApplicationConstants;
import edu.upc.sdk.utilities.NetworkUtils;
import edu.upc.sdk.utilities.StringUtils;
import okhttp3.Headers;

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
    private UserService userService;
    private LoginPresenter presenter;

    private static MockedStatic<OpenmrsAndroid> openmrsAndroid;

    MockedStatic<RestServiceBuilder> mockedStatic;

    @BeforeClass
    public static void setup() {
        Mockito.mockStatic(OpenMRS.class);
        Mockito.mockStatic(LocationDAO.class);
        Mockito.mockStatic(StringUtils.class);
        Mockito.mockStatic(NetworkUtils.class);
        openmrsAndroid = Mockito.mockStatic(OpenmrsAndroid.class);
    }

    @Before
    public void setUp() {
        super.setUp();
        presenter = new LoginPresenter(restApi, locationDAO, userService, view, openMRS,
            openMRSLogger, authorizationManager);

        Mockito.lenient().when(OpenmrsAndroid.getServerUrl()).thenReturn("http://www.some_server_url.com");
        Mockito.lenient().when(OpenmrsAndroid.getHashedPassword()).thenReturn("");
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
        Mockito.lenient().when(restApi.getSession())
            .thenReturn(mockSuccessCall(
                    new Session( true, new User()),
                    Headers.of("Set-Cookie", "JSESSIONID=1234567890")
            ));
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
        openmrsAndroid.verify(() -> OpenmrsAndroid.setSessionToken("1234567890"));
    }

    @Test
    public void shouldLoginUserInOnlineMode_noWipe_userNotAuthenticated() {
        mockNonEmptyCredentials(true);
        Mockito.lenient().when(restApi.getSession())
            .thenReturn(mockSuccessCall(new Session(false, new User())));
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
    public void shouldLoadLocationsInOnlineMode_allOK() {
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
        Mockito.lenient().when(restApi.getLocations(any(), anyString(), anyString()))
            .thenReturn(mockFailureCall());
        presenter.loadLocations("someUrl");
        verify(view).initLoginForm(any(), any());
        verify(view).setLocationErrorOccurred(true);
        verify(view).showInvalidURLSnackbar(Mockito.any());
        verify(view).hideUrlLoadingAnimation();
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

    private void mockNonEmptyCredentials(boolean isNonEmpty) {
        Mockito.when(StringUtils.notEmpty(anyString())).thenReturn(isNonEmpty);
    }

    private void mockLastUser(String user, String password, String url) {
        Mockito.lenient().when(OpenmrsAndroid.getUsername()).thenReturn(user);
        Mockito.lenient().when(OpenmrsAndroid.getServerUrl()).thenReturn(url);
        Mockito.lenient().when(OpenmrsAndroid.getPassword()).thenReturn(password);
        Mockito.lenient().when(OpenmrsAndroid.getHashedPassword()).thenReturn(BCrypt.hashpw(password, BCrypt.gensalt(ApplicationConstants.DEFAULT_BCRYPT_ROUND)));
    }
}
