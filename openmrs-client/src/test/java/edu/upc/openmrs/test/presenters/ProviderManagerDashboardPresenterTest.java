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

import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import edu.upc.sdk.library.OpenMRSLogger;
import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.api.repository.ProviderRepository;
import edu.upc.sdk.library.dao.ProviderRoomDAO;
import edu.upc.sdk.library.models.Provider;
import edu.upc.sdk.utilities.NetworkUtils;
import edu.upc.sdk.utilities.ToastUtil;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.upc.openmrs.activities.providermanagerdashboard.ProviderManagerDashboardContract;
import edu.upc.openmrs.activities.providermanagerdashboard.ProviderManagerDashboardPresenter;
import edu.upc.openmrs.application.OpenMRS;
import edu.upc.openmrs.test.ACUnitTestBase;

@PrepareForTest({NetworkUtils.class, ToastUtil.class, OpenMRS.class, OpenMRSLogger.class,OpenmrsAndroid.class})
public class ProviderManagerDashboardPresenterTest extends ACUnitTestBase {
    @Rule
    public InstantTaskExecutorRule taskExecutorRule = new InstantTaskExecutorRule();
    @Mock
    private RestApi restApi;
    @Mock
    private ProviderManagerDashboardContract.View providerManagerView;
    @Mock
    private Observer<List<Provider>> observer;
    @Mock
    private OpenMRSLogger openMRSLogger;
    @Mock
    private OpenMRS openMRS;
    MutableLiveData<List<Provider>> providerLiveData = Mockito.mock(MutableLiveData.class);
    private ProviderManagerDashboardPresenter providerManagerDashboardPresenter;
    private ProviderRepository providerRepository;
    private Fragment fragment = new Fragment();
    List<Provider> providerList;
    Provider providerOne = createProvider(1l, "doctor");
    Provider providerTwo = createProvider(2l, "nurse");

    @Before
    public void setUp() {
        providerList = Arrays.asList(providerOne, providerTwo);
        providerLiveData.postValue(providerList);

        this.providerRepository = new ProviderRepository(restApi, openMRSLogger);
        ProviderRoomDAO providerRoomDao = Mockito.mock(ProviderRoomDAO.class, RETURNS_MOCKS);
        ProviderRoomDAO spyProviderRoomDao = spy(providerRoomDao);

        Single listSingle = Mockito.mock(Single.class);
        doNothing().when(spyProviderRoomDao).updateProviderByUuid(Mockito.anyString(), Mockito.anyLong(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
        when(spyProviderRoomDao.getProviderList()).thenReturn(listSingle);
        when(listSingle.blockingGet()).thenReturn(providerList);

        this.providerRepository.setProviderRoomDao(spyProviderRoomDao);

        this.providerManagerDashboardPresenter = new ProviderManagerDashboardPresenter(providerManagerView, restApi, providerRepository);
        mockStaticMethods();
    }

    private void mockStaticMethods() {
        PowerMockito.mockStatic(NetworkUtils.class);
        PowerMockito.mockStatic(OpenMRS.class);
        PowerMockito.mockStatic(OpenMRSLogger.class);
        PowerMockito.mockStatic(OpenmrsAndroid.class);
        Mockito.lenient().when(OpenMRS.getInstance()).thenReturn(openMRS);
        PowerMockito.when(OpenmrsAndroid.getOpenMRSLogger()).thenReturn(openMRSLogger);
        PowerMockito.mockStatic(ToastUtil.class);
    }

    public abstract class Single extends io.reactivex.Single {
    }
}
