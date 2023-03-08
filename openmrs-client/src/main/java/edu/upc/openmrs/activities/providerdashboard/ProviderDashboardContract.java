package edu.upc.openmrs.activities.providerdashboard;

import android.content.Intent;

import edu.upc.sdk.library.models.Provider;

import edu.upc.openmrs.activities.BasePresenterContract;
import edu.upc.openmrs.activities.BaseView;

public interface ProviderDashboardContract {
    public interface View extends BaseView<Presenter> {
        void setupBackdrop(Provider provider);

        void showSnackbarForFailedEditRequest();
    }

    public interface Presenter extends BasePresenterContract {
        void updateProvider(Provider provider);

        Provider getProviderFromIntent(Intent intent);

        void deleteProvider();
    }
}
