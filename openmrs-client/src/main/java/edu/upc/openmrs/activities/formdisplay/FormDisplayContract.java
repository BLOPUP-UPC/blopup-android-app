/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package edu.upc.openmrs.activities.formdisplay;

import android.widget.LinearLayout;

import com.openmrs.android_sdk.library.models.Question;
import com.openmrs.android_sdk.utilities.InputField;
import com.openmrs.android_sdk.utilities.SelectOneField;

import java.util.List;

import edu.upc.openmrs.activities.BasePresenterContract;
import edu.upc.openmrs.activities.BaseView;

public interface FormDisplayContract {
    interface View {
        interface MainView extends BaseView<Presenter.MainPresenter> {
            void quitFormEntry();

            void enableSubmitButton(boolean enabled);

            void showToast(String errorMessage);

            void showToast();

            void showSuccessfulToast();
        }

        interface PageView extends BaseView<Presenter.PagePresenter> {
            boolean isSectionAlreadyCreated(String sectionLabel);

            void attachSectionToView(LinearLayout linearLayout);

            void attachQuestionToSection(LinearLayout section, LinearLayout question);

            void createAndAttachNumericQuestionEditText(Question question, LinearLayout sectionLinearLayout);

            void createAndAttachSelectQuestionDropdown(Question question, LinearLayout sectionLinearLayout);

            void createAndAttachSelectQuestionRadioButton(Question question, LinearLayout sectionLinearLayout);

            LinearLayout createQuestionGroupLayout(String questionLabel);

            LinearLayout createQuestionGroupLayoutForVitals(String questionLabel);

            LinearLayout createSectionLayout(String sectionLabel);

            List<SelectOneField> getSelectOneFields();

            List<InputField> getInputFields();

            void setInputFields(List<InputField> inputFields);

            void setSelectOneFields(List<SelectOneField> selectOneFields);
        }
    }

    interface Presenter {
        interface MainPresenter extends BasePresenterContract {
            void createEncounter();
        }

        interface PagePresenter extends BasePresenterContract {
        }
    }
}
