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

package edu.upc.openmrs.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;
import edu.upc.R;
import edu.upc.openmrs.activities.dialog.CustomFragmentDialog;
import edu.upc.openmrs.activities.introduction.SplashActivity;
import edu.upc.openmrs.activities.login.LoginActivity;
import edu.upc.openmrs.activities.settings.SettingsActivity;
import edu.upc.openmrs.application.OpenMRS;
import edu.upc.openmrs.bundle.CustomDialogBundle;
import edu.upc.openmrs.net.AuthorizationManager;
import edu.upc.sdk.DefaultUncaughtExceptionHandler;
import edu.upc.sdk.library.OpenMRSLogger;
import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.dao.LocationDAO;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.utilities.ApplicationConstants;
import edu.upc.sdk.utilities.ToastUtil;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@AndroidEntryPoint
public abstract class ACBaseActivity extends AppCompatActivity {
    protected final OpenMRS mOpenMRS = OpenMRS.getInstance();
    protected final OpenMRSLogger mOpenMRSLogger = OpenmrsAndroid.getOpenMRSLogger();
    protected FragmentManager mFragmentManager;
    protected AuthorizationManager mAuthorizationManager;
    protected CustomFragmentDialog mCustomFragmentDialog;
    private List<String> locationList;
    private IntentFilter mIntentFilter;

    private final BroadcastReceiver mPasswordChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showCredentialChangedDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();
        mAuthorizationManager = new AuthorizationManager();
        Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
        locationList = new ArrayList<>();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ApplicationConstants.BroadcastActions.AUTHENTICATION_CHECK_BROADCAST_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        if (!(this instanceof LoginActivity) && !mAuthorizationManager.isUserLoggedIn()
                && !(this instanceof SplashActivity)) {
            mAuthorizationManager.moveToLoginActivity();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mPasswordChangedReceiver, mIntentFilter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mPasswordChangedReceiver,mIntentFilter);
        }
        ToastUtil.setAppVisible(true);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mPasswordChangedReceiver);
        super.onPause();
        ToastUtil.setAppVisible(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.basic_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        MenuItem logoutMenuItem = menu.findItem(R.id.actionLogout);
        if (logoutMenuItem != null) {
            logoutMenuItem.setTitle(getString(R.string.action_logout) + " " + OpenmrsAndroid.getUsername());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.actionSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.actionLogout:
                this.showLogoutDialog();
                return true;
            case R.id.actionLocation:
                if (!locationList.isEmpty()) {
                    locationList.clear();
                }
                Observable<List<LocationEntity>> observableList = new LocationDAO().getLocations();
                observableList.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(getLocationList());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Observer<List<LocationEntity>> getLocationList() {
        return new Observer<List<LocationEntity>>() {
            @Override
            public void onCompleted() {
                showLocationDialog(locationList);
            }

            @Override
            public void onError(Throwable e) {
                mOpenMRSLogger.e(e.getMessage());
            }

            @Override
            public void onNext(List<LocationEntity> locations) {
                for (LocationEntity locationItem : locations) {
                    locationList.add(locationItem.getName());
                }
            }
        };
    }

    public void logout() {
        OpenmrsAndroid.clearUserPreferencesData();
        mAuthorizationManager.moveToLoginActivity();
        ToastUtil.showShortToast(getApplicationContext(), ToastUtil.ToastType.SUCCESS, R.string.logout_success);
    }

    private void showCredentialChangedDialog() {
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(R.string.credentials_changed_dialog_title));
        bundle.setTextViewMessage(getString(R.string.credentials_changed_dialog_message));
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.LOGOUT);
        bundle.setRightButtonText(getString(R.string.ok));
        mCustomFragmentDialog = CustomFragmentDialog.newInstance(bundle);
        mCustomFragmentDialog.setCancelable(false);
        mCustomFragmentDialog.setRetainInstance(true);
        mCustomFragmentDialog.show(mFragmentManager, ApplicationConstants.DialogTAG.CREDENTIAL_CHANGED_DIALOG_TAG);
    }

    private void showLogoutDialog() {
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(R.string.logout_dialog_title));
        bundle.setTextViewMessage(getString(R.string.logout_dialog_message));
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.LOGOUT);
        bundle.setRightButtonText(getString(R.string.logout_dialog_button));
        bundle.setLeftButtonAction(CustomFragmentDialog.OnClickAction.DISMISS);
        bundle.setLeftButtonText(getString(R.string.dialog_button_cancel));
        createAndShowDialog(bundle, ApplicationConstants.DialogTAG.LOGOUT_DIALOG_TAG);
    }

    public void showStartVisitImpossibleDialog(CharSequence title, UUID visitUuid) {
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(R.string.start_visit_unsuccessful_dialog_title));
        bundle.setTextViewMessage(getString(R.string.start_visit_unsuccessful_dialog_message, title));
        bundle.setLeftButtonAction(CustomFragmentDialog.OnClickAction.DISMISS);
        bundle.setLeftButtonText(getString(R.string.dialog_button_cancel));
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.END_VISIT_START_NEW_VISIT);
        bundle.setRightButtonText(getString(R.string.action_start_visit));
        bundle.setEndVisitUuid(visitUuid);
        createAndShowDialog(bundle, ApplicationConstants.DialogTAG.START_VISIT_IMPOSSIBLE_DIALOG_TAG);
    }

    private void showLocationDialog(List<String> locationList) {
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(R.string.location_dialog_title));
        bundle.setTextViewMessage(getString(R.string.location_dialog_current_location) + " " + OpenmrsAndroid.getLocation());
        bundle.setLocationList(locationList);
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.SELECT_LOCATION);
        bundle.setRightButtonText(getString(R.string.dialog_button_select_location));
        bundle.setLeftButtonAction(CustomFragmentDialog.OnClickAction.DISMISS);
        bundle.setLeftButtonText(getString(R.string.dialog_button_cancel));
        createAndShowDialog(bundle, ApplicationConstants.DialogTAG.LOCATION_DIALOG_TAG);
    }

    public void createAndShowDialog(CustomDialogBundle bundle, String tag) {
        CustomFragmentDialog instance = CustomFragmentDialog.newInstance(bundle);
        instance.show(mFragmentManager, tag);
    }

    public void moveUnauthorizedUserToLoginScreen() {
        OpenmrsAndroid.clearUserPreferencesData();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    public void dismissCustomFragmentDialog() {
        if (mCustomFragmentDialog != null) {
            mCustomFragmentDialog.dismiss();
            mCustomFragmentDialog = null;
        }
    }

    public void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                      @NonNull Fragment fragment, int frameId) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }
}
