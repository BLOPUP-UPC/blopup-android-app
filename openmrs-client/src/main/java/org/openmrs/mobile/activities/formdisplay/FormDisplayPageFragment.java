/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.mobile.activities.formdisplay;

import static android.app.Activity.RESULT_OK;
import static edu.upc.blopup.scale.readScaleMeasurement.ReadScaleActivityKt.EXTRAS_WEIGHT;
import static edu.upc.blopup.tensiometer.readTensiometerMeasurement.ReadTensiometerActivityKt.EXTRAS_DIASTOLIC;
import static edu.upc.blopup.tensiometer.readTensiometerMeasurement.ReadTensiometerActivityKt.EXTRAS_HEART_RATE;
import static edu.upc.blopup.tensiometer.readTensiometerMeasurement.ReadTensiometerActivityKt.EXTRAS_SYSTOLIC;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.openmrs.android_sdk.library.models.Answer;
import com.openmrs.android_sdk.library.models.Question;
import com.openmrs.android_sdk.utilities.ApplicationConstants;
import com.openmrs.android_sdk.utilities.InputField;
import com.openmrs.android_sdk.utilities.RangeEditText;
import com.openmrs.android_sdk.utilities.SelectOneField;
import com.openmrs.android_sdk.utilities.ToastUtil;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.bundle.FormFieldsWrapper;
import org.openmrs.mobile.databinding.FragmentFormDisplayBinding;

import java.util.ArrayList;
import java.util.List;

import edu.upc.blopup.scale.readScaleMeasurement.ReadWeightActivity;
import edu.upc.blopup.tensiometer.readTensiometerMeasurement.ReadTensiometerActivity;

public class FormDisplayPageFragment extends ACBaseFragment<FormDisplayContract.Presenter.PagePresenter> implements FormDisplayContract.View.PageView {

    private static final String SYSTOLIC_FIELD_CONCEPT = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String DIASTOLIC_FIELD_CONCEPT = "5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String HEART_RATE_FIELD_CONCEPT = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String WEIGHT_FIELD_CONCEPT = "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    private String mSectionLabel;
    private FragmentFormDisplayBinding binding = null;
    private List<InputField> inputFields = new ArrayList<>();
    private List<SelectOneField> selectOneFields = new ArrayList<>();
    private LinearLayout parent;
    private final ActivityResultLauncher<Intent> bluetoothTensiometerDataLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Intent intent = result.getData();
                        if (intent != null && result.getResultCode() == RESULT_OK) {
                            int systolic = intent.getExtras().getInt(EXTRAS_SYSTOLIC);
                            int diastolic = intent.getExtras().getInt(EXTRAS_DIASTOLIC);
                            int heartRate = intent.getExtras().getInt(EXTRAS_HEART_RATE);

                            fillVital(SYSTOLIC_FIELD_CONCEPT, systolic);
                            fillVital(DIASTOLIC_FIELD_CONCEPT, diastolic);
                            fillVital(HEART_RATE_FIELD_CONCEPT, heartRate);
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> bluetoothScaleDataLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Intent intent = result.getData();
                        if (intent != null && result.getResultCode() == RESULT_OK) {
                            float weight = intent.getExtras().getFloat(EXTRAS_WEIGHT);
                            fillVital(WEIGHT_FIELD_CONCEPT, weight);
                        }
                    }
            );

    // we know this is ugly as hell, best shot we had with this way of constructing forms :(
    private void fillVital(String concept, float value) {
        InputField inputField = getInputField(concept);
        View view = getActivity().findViewById(inputField.id);
        if (view instanceof DiscreteSeekBar) {
            DiscreteSeekBar seekbar = (DiscreteSeekBar) view;
            seekbar.setProgress(Double.valueOf(value).intValue());
            inputField.value = value;
        }
    }

    public static FormDisplayPageFragment newInstance() {
        return new FormDisplayPageFragment();
    }

    @Override
    public boolean isSectionAlreadyCreated(String sectionLabel) {
        return sectionLabel.equals(mSectionLabel);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFormDisplayBinding.inflate(inflater, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        parent = binding.sectionContainer;
        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        FormFieldsWrapper formFieldsWrapper = new FormFieldsWrapper(getInputFields(), getSelectOneFields());
        outState.putSerializable(ApplicationConstants.BundleKeys.FORM_FIELDS_BUNDLE, formFieldsWrapper);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            FormFieldsWrapper formFieldsWrapper = (FormFieldsWrapper) savedInstanceState.getSerializable(ApplicationConstants.BundleKeys.FORM_FIELDS_BUNDLE);
            inputFields = formFieldsWrapper.getInputFields();

            for (InputField field : inputFields) {
                View v = getActivity().findViewById(field.id);
                if (v != null && v instanceof DiscreteSeekBar) {
                    DiscreteSeekBar sb = (DiscreteSeekBar) v;
                    sb.setProgress(Double.valueOf(field.value).intValue());
                }
                if (field.isRed) {
                    RangeEditText ed = getActivity().findViewById(field.id);
                    ed.setTextColor(ContextCompat.getColor(OpenMRS.getInstance(), R.color.red));
                }
            }
            selectOneFields = formFieldsWrapper.getSelectOneFields();
        }
    }

    @Override
    public void attachSectionToView(LinearLayout linearLayout) {
        parent.addView(linearLayout);
    }

    @Override
    public void attachQuestionToSection(LinearLayout section, LinearLayout question) {
        section.addView(question);
    }

    @Override
    public void createAndAttachNumericQuestionEditText(Question question, LinearLayout sectionLinearLayout) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        RangeEditText ed = new RangeEditText(getActivity());
        DiscreteSeekBar dsb = new DiscreteSeekBar(getActivity());
        InputField field = new InputField(question.getQuestionOptions().getConcept());
        InputField inputField = getInputField(field.concept);
        if (inputField != null) {
            inputField.id = field.id;
        } else {
            field.concept = question.getQuestionOptions().getConcept();
            inputFields.add(field);
        }
        sectionLinearLayout.addView(generateTextView(question.getLabel()));

        if ((question.getQuestionOptions().getMax() != null) && (!(question.getQuestionOptions().isAllowDecimal()))) {
            dsb.setMax((int) Double.parseDouble(question.getQuestionOptions().getMax()));
            dsb.setMin((int) Double.parseDouble(question.getQuestionOptions().getMin()));
            dsb.setId(field.id);
            sectionLinearLayout.addView(dsb, layoutParams);
        } else {
            ed.setName(question.getLabel());
            ed.setSingleLine(true);
            ed.setHint(question.getLabel());
            ed.setLowerlimit(-1.0);
            ed.setUpperlimit(-1.0);
            ed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            if (question.getQuestionOptions().isAllowDecimal()) {
                ed.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            } else {
                ed.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            ed.setId(field.id);
            sectionLinearLayout.addView(ed, layoutParams);
        }
    }

    private View generateTextView(String text) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 0, 0, 0);
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    public InputField getInputField(String concept) {
        for (InputField inputField : inputFields) {
            if (concept.equals(inputField.concept)) {
                return inputField;
            }
        }
        return null;
    }

    public SelectOneField getSelectOneField(String concept) {
        for (SelectOneField selectOneField : selectOneFields) {
            if (concept.equals(selectOneField.getConcept())) {
                return selectOneField;
            }
        }
        return null;
    }

    @Override
    public void createAndAttachSelectQuestionDropdown(Question question, LinearLayout sectionLinearLayout) {
        TextView textView = new TextView(getActivity());
        textView.setPadding(20, 0, 0, 0);
        textView.setText(question.getLabel());
        Spinner spinner = (Spinner) getActivity().getLayoutInflater().inflate(R.layout.form_dropdown, null);

        LinearLayout questionLinearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams questionLinearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        questionLinearLayout.setOrientation(LinearLayout.VERTICAL);
        questionLinearLayoutParams.gravity = Gravity.START;
        questionLinearLayout.setLayoutParams(questionLinearLayoutParams);

        List<String> answerLabels = new ArrayList<>();
        for (Answer answer : question.getQuestionOptions().getAnswers()) {
            answerLabels.add(answer.getLabel());
        }

        SelectOneField spinnerField = new SelectOneField(question.getQuestionOptions().getAnswers(),
                question.getQuestionOptions().getConcept());

        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, answerLabels);
        spinner.setAdapter(arrayAdapter);

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        questionLinearLayout.addView(textView);
        questionLinearLayout.addView(spinner);

        sectionLinearLayout.setLayoutParams(linearLayoutParams);
        sectionLinearLayout.addView(questionLinearLayout);

        SelectOneField selectOneField = getSelectOneField(spinnerField.getConcept());
        if (selectOneField != null) {
            spinner.setSelection(selectOneField.getChosenAnswerPosition());
            setOnItemSelectedListener(spinner, selectOneField);
        } else {
            setOnItemSelectedListener(spinner, spinnerField);
            selectOneFields.add(spinnerField);
        }
    }

    private void setOnItemSelectedListener(Spinner spinner, final SelectOneField spinnerField) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinnerField.setAnswer(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spinnerField.setAnswer(-1);
            }
        });
    }

    @Override
    public void createAndAttachSelectQuestionRadioButton(Question question, LinearLayout sectionLinearLayout) {
        TextView textView = new TextView(getActivity());
        textView.setPadding(20, 0, 0, 0);
        textView.setText(question.getLabel());

        RadioGroup radioGroup = new RadioGroup(getActivity());

        for (Answer answer : question.getQuestionOptions().getAnswers()) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(answer.getLabel());
            radioGroup.addView(radioButton);
        }

        SelectOneField radioGroupField = new SelectOneField(question.getQuestionOptions().getAnswers(),
                question.getQuestionOptions().getConcept());

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        sectionLinearLayout.addView(textView);
        sectionLinearLayout.addView(radioGroup);

        sectionLinearLayout.setLayoutParams(linearLayoutParams);

        SelectOneField selectOneField = getSelectOneField(radioGroupField.getConcept());
        if (selectOneField != null) {
            if (selectOneField.getChosenAnswerPosition() != -1) {
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(selectOneField.getChosenAnswerPosition());
                radioButton.setChecked(true);
            }
            setOnCheckedChangeListener(radioGroup, selectOneField);
        } else {
            setOnCheckedChangeListener(radioGroup, radioGroupField);
            selectOneFields.add(radioGroupField);
        }
    }

    private void setOnCheckedChangeListener(RadioGroup radioGroup, final SelectOneField radioGroupField) {
        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
            View radioButton = radioGroup1.findViewById(i);
            int idx = radioGroup1.indexOfChild(radioButton);
            radioGroupField.setAnswer(idx);
        });
    }

    @Override
    public LinearLayout createQuestionGroupLayout(String questionLabel) {
        LinearLayout questionLinearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams layoutParams = getAndAdjustLinearLayoutParams(questionLinearLayout);

        TextView tv = new TextView(getActivity());
        tv.setText(questionLabel);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary));
        questionLinearLayout.addView(tv, layoutParams);

        return questionLinearLayout;
    }

    @Override
    public LinearLayout createQuestionGroupLayoutForVitals(String questionLabel) {
        LinearLayout questionLinearLayout = createQuestionGroupLayout(questionLabel);

        Button tensiometerButton = new Button(getActivity());
        tensiometerButton.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        tensiometerButton.setText(R.string.receive_vitals_from_bluetooth_button_text);
        questionLinearLayout.addView(tensiometerButton);

        tensiometerButton.setOnClickListener(view -> {
            try {
                Intent input = new Intent(getContext(), ReadTensiometerActivity.class);
                bluetoothTensiometerDataLauncher.launch(input);
            } catch (ActivityNotFoundException ex) {
                ToastUtil.error(
                        getString(R.string.receive_vitals_from_bluetooth_button_error_message),
                        Toast.LENGTH_LONG
                );
            }
        });

        Button scaleButton = new Button(getActivity());
        scaleButton.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        scaleButton.setText(R.string.receive_vitals_from_scale_button_text);
        questionLinearLayout.addView(scaleButton);

        scaleButton.setOnClickListener(view -> {
            try {
                Intent input = new Intent(getContext(), ReadWeightActivity.class);
                bluetoothScaleDataLauncher.launch(input);
            } catch (ActivityNotFoundException ex) {
                ToastUtil.error(
                        getString(R.string.receive_vitals_from_bluetooth_button_error_message),
                        Toast.LENGTH_LONG
                );
            }
        });
        return questionLinearLayout;
    }

    @Override
    public LinearLayout createSectionLayout(String sectionLabel) {
        mSectionLabel = sectionLabel;

        LinearLayout sectionLinearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams layoutParams = getAndAdjustLinearLayoutParams(sectionLinearLayout);

        TextView tv = new TextView(getActivity());
        tv.setText(sectionLabel);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        tv.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary));
        sectionLinearLayout.addView(tv, layoutParams);

        return sectionLinearLayout;
    }

    private LinearLayout.LayoutParams getAndAdjustLinearLayoutParams(LinearLayout linearLayout) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        linearLayout.setOrientation(LinearLayout.VERTICAL);

        Resources r = getActivity().getResources();
        float pxLeftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
        float pxTopMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
        float pxRightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
        float pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
        layoutParams.setMargins(Math.round(pxLeftMargin), Math.round(pxTopMargin), Math.round(pxRightMargin), Math.round(pxBottomMargin));

        return layoutParams;
    }

    @Override
    public List<SelectOneField> getSelectOneFields() {
        return selectOneFields;
    }

    @Override
    public List<InputField> getInputFields() {
        for (InputField field : inputFields) {
            try {
                RangeEditText ed = getActivity().findViewById(field.id);
                if (!isEmpty(ed)) {
                    field.value = Double.parseDouble(ed.getText().toString());
                    boolean isRed = (ed.getCurrentTextColor() == ContextCompat.getColor(OpenMRS.getInstance(), R.color.red));
                    field.isRed = isRed;
                } else {
                    field.value = -1.0;
                }
            } catch (ClassCastException e) {
                field.value = Math.round(field.value * 100.0) / 100.0;
            }
        }

        return inputFields;
    }

    @Override
    public void setInputFields(List<InputField> inputFields) {
        this.inputFields = inputFields;
    }

    @Override
    public void setSelectOneFields(List<SelectOneField> selectOneFields) {
        this.selectOneFields = selectOneFields;
    }

    public boolean checkInputFields() {
        boolean allEmpty = true;
        boolean valid = true;
        for (InputField field : inputFields) {
            try {
                RangeEditText ed = getActivity().findViewById(field.id);
                if (!isEmpty(ed)) {
                    allEmpty = false;
                    if (ed.getText().toString().charAt(0) != '.') {
                        Double inp = Double.parseDouble(ed.getText().toString());
                        if (ed.getUpperlimit() != -1.0 && ed.getUpperlimit() != -1.0 && (ed.getUpperlimit() < inp || ed.getLowerlimit() > inp)) {
                            ed.setTextColor(ContextCompat.getColor(OpenMRS.getInstance(), R.color.red));
                            valid = false;
                        }
                    } else {
                        ed.setTextColor(ContextCompat.getColor(OpenMRS.getInstance(), R.color.red));
                        valid = false;
                    }
                }
            } catch (ClassCastException e) {
                DiscreteSeekBar dsb = getActivity().findViewById(field.id);
                if (dsb.getProgress() > dsb.getMin()) {
                    allEmpty = false;
                }
            }
        }

        for (SelectOneField radioGroupField : selectOneFields) {
            if (radioGroupField.getChosenAnswer() != null) {
                allEmpty = false;
            }
        }

        if (allEmpty) {
            ToastUtil.error(getString(R.string.all_fields_empty_error_message));
            return false;
        }
        return valid;
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
