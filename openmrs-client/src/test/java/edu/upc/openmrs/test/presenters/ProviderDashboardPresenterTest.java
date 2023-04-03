package edu.upc.openmrs.test.presenters;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import edu.upc.openmrs.activities.providerdashboard.ProviderDashboardContract;
import edu.upc.openmrs.activities.providerdashboard.ProviderDashboardPresenter;
import edu.upc.openmrs.application.OpenMRS;
import edu.upc.openmrs.test.ACUnitTestBase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NetworkUtils.class, ToastUtil.class, OpenMRS.class, OpenMRSLogger.class, OpenmrsAndroid.class})
public class ProviderDashboardPresenterTest extends ACUnitTestBase {
    @Rule
    public InstantTaskExecutorRule taskExecutorRule = new InstantTaskExecutorRule();
    @Mock
    private RestApi restApi;
    @Mock
    private ProviderDashboardContract.View providerManagerView;
    @Mock
    private OpenMRSLogger openMRSLogger;
    @Mock
    private OpenMRS openMRS;
    private ProviderDashboardPresenter providerDashboardPresenter;

    @Spy
    private ProviderRepository providerRepository = new ProviderRepository(restApi, openMRSLogger);

    @Mock
    private Context context;

    @Before
    public void setup() {
        this.providerRepository = new ProviderRepository(restApi, openMRSLogger);
        when(context.getString(anyInt())).thenReturn("dummy");
        Whitebox.setInternalState(providerRepository, "context", context);

        ProviderRoomDAO providerRoomDao = Mockito.mock(ProviderRoomDAO.class);
        ProviderRoomDAO spyProviderRoomDao = spy(providerRoomDao);
        doNothing().when(spyProviderRoomDao).updateProviderByUuid(Mockito.anyString(), Mockito.anyLong(), Mockito.any(), Mockito.anyString(), Mockito.anyString());

        this.providerRepository.setProviderRoomDao(spyProviderRoomDao);
        this.providerDashboardPresenter = new ProviderDashboardPresenter(providerManagerView, restApi, providerRepository);
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
}
