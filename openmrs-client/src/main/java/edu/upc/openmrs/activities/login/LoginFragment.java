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

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import edu.upc.BuildConfig;
import edu.upc.R;
import edu.upc.blopup.ui.MainActivity;
import edu.upc.databinding.FragmentLoginBinding;
import edu.upc.openmrs.activities.ACBaseFragment;
import edu.upc.openmrs.activities.dialog.CustomFragmentDialog;
import edu.upc.openmrs.application.OpenMRS;
import edu.upc.openmrs.bundle.CustomDialogBundle;
import edu.upc.openmrs.listeners.watcher.LoginValidatorWatcher;
import edu.upc.openmrs.utilities.URLValidator;
import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.utilities.ApplicationConstants;
import edu.upc.sdk.utilities.StringUtils;
import edu.upc.sdk.utilities.ToastUtil;


public class LoginFragment extends ACBaseFragment<LoginContract.Presenter> implements LoginContract.View {
    private static String mLastCorrectURL = "";
    private static List<LocationEntity> mLocationsList;
    final private String initialUrl = OpenmrsAndroid.getServerUrl();
    protected final OpenMRS mOpenMRS = OpenMRS.getInstance();
    private FragmentLoginBinding binding;
    private View mRootView;
    private LoginValidatorWatcher loginValidatorWatcher;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        mRootView = binding.getRoot();

        initViewFields();
        initListeners();
        if (mLastCorrectURL.isEmpty()) {
            binding.loginUrlField.setText(OpenmrsAndroid.getServerUrl());
            mLastCorrectURL = OpenmrsAndroid.getServerUrl();
        } else {
            binding.loginUrlField.setText(mLastCorrectURL);
        }
        hideURLDialog();
        if (BuildConfig.SHOW_SERVER_URL_TOGGLE) {
            binding.textInputLayoutLoginURL.setVisibility(View.VISIBLE);
        } else {
            binding.textInputLayoutLoginURL.setVisibility(View.INVISIBLE);
        }

        return mRootView;
    }

    private void initListeners() {
        loginValidatorWatcher = new LoginValidatorWatcher(binding.loginUrlField, binding.loginUsernameField,
                binding.loginPasswordField, binding.locationSpinner, binding.loginButton);

        binding.loginUrlField.setOnFocusChangeListener((view, hasFocus) -> {
            if (StringUtils.notEmpty(binding.loginUrlField.getText().toString())
                    && !view.isFocused()
                    && loginValidatorWatcher.isUrlChanged()
                    || (loginValidatorWatcher.isUrlChanged() && !view.isFocused()
                    && loginValidatorWatcher.isLocationErrorOccurred())
                    || (!loginValidatorWatcher.isUrlChanged() && !view.isFocused())) {
                ((LoginFragment) getActivity()
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.loginContentFrame))
                        .setUrl(binding.loginUrlField.getText().toString());
                loginValidatorWatcher.setUrlChanged(false);
            }
        });

        binding.loginUsernameField.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.textInputLayoutUsername.setHint(HtmlCompat.fromHtml(getString(R.string.login_username_hint), HtmlCompat.FROM_HTML_MODE_LEGACY));
            } else if (binding.loginUsernameField.getText().toString().equals("")) {
                binding.textInputLayoutUsername.setHint(HtmlCompat.fromHtml(getString(R.string.login_username_hint) + getString(R.string.req_star),HtmlCompat.FROM_HTML_MODE_LEGACY));
                binding.textInputLayoutUsername.setHintAnimationEnabled(true);
            }
        });

        binding.loginPasswordField.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.textInputLayoutPassword.setHint(HtmlCompat.fromHtml(getString(R.string.login_password_hint), HtmlCompat.FROM_HTML_MODE_LEGACY));
            } else if (binding.loginPasswordField.getText().toString().equals("")) {
                binding.textInputLayoutPassword.setHint(HtmlCompat.fromHtml(getString(R.string.login_password_hint) + getString(R.string.req_star), HtmlCompat.FROM_HTML_MODE_LEGACY));
                binding.textInputLayoutPassword.setHintAnimationEnabled(true);
            }
        });

        binding.locationSpinner.setOnTouchListener((view, event) -> {
            mPresenter.loadLocations(binding.loginUrlField.getText().toString());
            return view.performClick();
        });

        binding.loginButton.setOnClickListener(view -> mPresenter.login(binding.loginUsernameField.getText().toString(),
                binding.loginPasswordField.getText().toString(),
                binding.loginUrlField.getText().toString(),
                initialUrl));

        binding.aboutUsTextView.setOnClickListener(view -> openAboutPage());
    }

    private void initViewFields() {
        binding.textInputLayoutPassword.setHint(Html.fromHtml(getString(R.string.login_password_hint) + getString(R.string.req_star)));
        binding.textInputLayoutUsername.setHint(Html.fromHtml(getString(R.string.login_username_hint) + getString(R.string.req_star)));
        binding.loginUrlField.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_grey_8x));
        binding.textInputLayoutUsername.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dark_grey_8x)));
        binding.textInputLayoutPassword.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.dark_grey_8x)));
    }

    @Override
    public void onResume() {
        super.onResume();
        hideUrlLoadingAnimation();
    }

    @Override
    public void hideSoftKeys() {
        View view = this.getActivity().getCurrentFocus();
        if (view == null) {
            view = new View(this.getActivity());
        }
        InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void openAboutPage() {
        String userGuideUrl = ApplicationConstants.USER_GUIDE;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(userGuideUrl));
        startActivity(intent);
    }

    @Override
    public void showWarningDialog() {
        CustomDialogBundle bundle = new CustomDialogBundle();
        bundle.setTitleViewMessage(getString(R.string.warning_dialog_title));
        bundle.setTextViewMessage(getString(R.string.warning_lost_data_dialog));
        bundle.setRightButtonText(getString(R.string.dialog_button_ok));
        bundle.setRightButtonAction(CustomFragmentDialog.OnClickAction.LOGIN);
        bundle.setLeftButtonText(getString(R.string.dialog_button_cancel));
        bundle.setLeftButtonAction(CustomFragmentDialog.OnClickAction.DISMISS);
        ((LoginActivity) this.getActivity()).createAndShowDialog(bundle, ApplicationConstants.DialogTAG.WARNING_LOST_DATA_DIALOG_TAG);
    }

    @Override
    public void showLoadingAnimation() {
        binding.loginFormView.setVisibility(View.GONE);
        binding.loginLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingAnimation() {
        binding.loginFormView.setVisibility(View.VISIBLE);
        binding.loginLoading.setVisibility(View.GONE);
    }

    @Override
    public void showLocationLoadingAnimation() {
        setLoginButtonStatus(false);
        binding.locationLoadingProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideUrlLoadingAnimation() {
        binding.locationLoadingProgressBar.setVisibility(View.GONE);
        binding.loginLoading.setVisibility(View.GONE);
    }

    @Override
    public void finishLoginActivity() {
        getActivity().finish();
    }

    public void initLoginForm(List<LocationEntity> locationsList, String serverURL) {
        setLocationErrorOccurred(false);
        mLastCorrectURL = serverURL;
        binding.loginUrlField.setText(serverURL);
        mLocationsList = locationsList;
        if (isActivityNotNull()) {
            List<String> items = getLocationStringList(locationsList);
            final LocationArrayAdapter adapter = new LocationArrayAdapter(this.getActivity(), items);
            binding.locationSpinner.setAdapter(adapter);
            setLoginButtonStatus(false);
            binding.loginLoading.setVisibility(View.GONE);
            binding.loginFormView.setVisibility(View.VISIBLE);
            if (locationsList.isEmpty()) {
                setLoginButtonStatus(true);
            } else {
                setLoginButtonStatus(false);
            }
        }
    }

    @Override
    public void userAuthenticated() {
        Intent intent = new Intent(mOpenMRS.getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mOpenMRS.getApplicationContext().startActivity(intent);
        mPresenter.saveLocationsToDatabase(mLocationsList, binding.locationSpinner.getSelectedItem().toString());
    }

    @Override
    public void showInvalidURLSnackbar(String message) {
        if (isActivityNotNull()) {
            createSnackbar(message)
                    .setAction(getResources().getString(R.string.snackbar_choose), view -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_server_list)));
                        startActivity(intent);
                    })
                    .show();
        }
    }

    @Override
    public void showInvalidURLSnackbar(int messageID) {
        if (isActivityNotNull()) {
            createSnackbar(getString(messageID))
                    .setAction(getResources().getString(R.string.snackbar_choose), view -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_server_list)));
                        startActivity(intent);
                    })
                    .show();
        }
    }

    @Override
    public void showInvalidLoginOrPasswordSnackbar() {
        String message = getResources().getString(R.string.invalid_login_or_password_message);
        if (isActivityNotNull()) {
            createSnackbar(message)
                    .setAction(getResources().getString(R.string.snackbar_edit), view -> {
                        binding.loginPasswordField.requestFocus();
                        binding.loginPasswordField.selectAll();
                    })
                    .show();
        }
    }

    private Snackbar createSnackbar(String message) {
        return Snackbar
                .make(mRootView, message, Snackbar.LENGTH_LONG);
    }

    @Override
    public void setLocationErrorOccurred(boolean errorOccurred) {
        this.loginValidatorWatcher.setLocationErrorOccurred(errorOccurred);
        setLoginButtonStatus(!errorOccurred);
    }


    @Override
    public void showToast(String message, ToastUtil.ToastType toastType) {
        if (getActivity() != null) {
            ToastUtil.showShortToast(getActivity(), toastType, message);
        }
    }

    @Override
    public void showToast(int textId, ToastUtil.ToastType toastType) {
        if (getActivity() != null) {
            ToastUtil.showShortToast(getActivity(), toastType, getResources().getString(textId));
        }
    }

    private List<String> getLocationStringList(List<LocationEntity> locationList) {
        List<String> list = new ArrayList<>();
        //If spinner is at start option, append a red * to signify requirement
        list.add(Html.fromHtml(getString(R.string.login_location_select) + getString(R.string.req_star)).toString());
        for (int i = 0; i < locationList.size(); i++) {
            list.add(locationList.get(i).getDisplay());
        }
        return list;
    }

    public void setUrl(String url) {
        URLValidator.ValidationResult result = URLValidator.validate(url);
        if (result.isURLValid()) {
            mPresenter.loadLocations(result.getUrl());
        } else {
            showInvalidURLSnackbar(getResources().getString(R.string.invalid_URL_message));
        }
    }

    public void hideURLDialog() {
        if (mLocationsList == null) {
            mPresenter.loadLocations(mLastCorrectURL);
        } else {
            initLoginForm(mLocationsList, mLastCorrectURL);
        }
    }

    public void login() {
        mPresenter.authenticateUser(binding.loginUsernameField.getText().toString(),
                binding.loginPasswordField.getText().toString(),
                binding.loginUrlField.getText().toString());
    }

    public void login(boolean wipeDatabase) {
        mPresenter.authenticateUser(binding.loginUsernameField.getText().toString(),
                binding.loginPasswordField.getText().toString(),
                binding.loginUrlField.getText().toString(), wipeDatabase);
    }

    private boolean isActivityNotNull() {
        return (isAdded() && getActivity() != null);
    }

    private void setLoginButtonStatus(Boolean shouldEnable) {
        if (shouldEnable) {
            binding.loginButton.setEnabled(true);
            binding.loginButton.setBackgroundColor(getResources().getColor(R.color.color_accent, null));
        } else {
            binding.loginButton.setEnabled(false);
            binding.loginButton.setBackgroundColor(getResources().getColor(R.color.dark_grey_for_stroke, null));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}