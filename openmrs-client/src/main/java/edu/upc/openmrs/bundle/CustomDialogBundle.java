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

package edu.upc.openmrs.bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.upc.openmrs.activities.dialog.CustomFragmentDialog;
import edu.upc.sdk.library.models.Patient;

public class CustomDialogBundle implements Serializable {
    private CustomFragmentDialog.OnClickAction leftButtonAction;
    private CustomFragmentDialog.OnClickAction rightButtonAction;
    private String textViewMessage;
    private String titleViewMessage;
    private String editTextViewMessage;
    private String leftButtonText;
    private String rightButtonText;
    private String progressViewMessage;
    private List<Patient> patientsList;
    private List<String> locationList;
    private ArrayList<Patient> selectedItems = new ArrayList<>();
    private Patient newPatient;
    private boolean loadingBar;
    private boolean progressDialog;
    private UUID endVisitUuid;

    public boolean hasProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog(boolean progressDialog) {
        this.progressDialog = progressDialog;
    }

    public boolean hasLoadingBar() {
        return loadingBar;
    }

    public void setLoadingBar(boolean loadingBar) {
        this.loadingBar = loadingBar;
    }

    public CustomFragmentDialog.OnClickAction getLeftButtonAction() {
        return leftButtonAction;
    }

    public void setLeftButtonAction(CustomFragmentDialog.OnClickAction leftButtonAction) {
        this.leftButtonAction = leftButtonAction;
    }

    public CustomFragmentDialog.OnClickAction getRightButtonAction() {
        return rightButtonAction;
    }

    public void setRightButtonAction(CustomFragmentDialog.OnClickAction rightButtonAction) {
        this.rightButtonAction = rightButtonAction;
    }

    public void setSelectedItems(ArrayList<Patient> toDelete) {
        this.selectedItems = toDelete;
    }

    public ArrayList<Patient> getSelectedItems() {
        return selectedItems;
    }

    public String getTextViewMessage() {
        return textViewMessage;
    }

    public void setTextViewMessage(String textViewMessage) {
        this.textViewMessage = textViewMessage;
    }

    public String getLeftButtonText() {
        return leftButtonText;
    }

    public void setLeftButtonText(String leftButtonText) {
        this.leftButtonText = leftButtonText;
    }

    public String getRightButtonText() {
        return rightButtonText;
    }

    public void setRightButtonText(String rightButtonText) {
        this.rightButtonText = rightButtonText;
    }

    public String getTitleViewMessage() {
        return titleViewMessage;
    }

    public void setProgressViewMessage(String progressViewMessage) {
        this.progressViewMessage = progressViewMessage;
    }

    public String getProgressViewMessage() {
        return progressViewMessage;
    }

    public void setTitleViewMessage(String titleViewMessage) {
        this.titleViewMessage = titleViewMessage;
    }

    public String getEditTextViewMessage() {
        return editTextViewMessage;
    }

    public void setEditTextViewMessage(String editTextViewMessage) {
        this.editTextViewMessage = editTextViewMessage;
    }

    public List<Patient> getPatientsList() {
        return patientsList;
    }

    public boolean hasPatientList() {
        return patientsList != null;
    }

    public void setPatientsList(List<Patient> patientsList) {
        this.patientsList = patientsList;
    }

    public Patient getNewPatient() {
        return newPatient;
    }

    public void setNewPatient(Patient newPatient) {
        this.newPatient = newPatient;
    }

    public List<String> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<String> locationList) {
        this.locationList = locationList;
    }

    public void setEndVisitUuid(UUID visitUuid) { this.endVisitUuid = visitUuid; }

    public UUID getEndVisitId() { return endVisitUuid; }
}
