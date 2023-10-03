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
package edu.upc.blopup.bloodpressure

import edu.upc.R
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.utilities.ApplicationConstants.vitalsConceptType.DIASTOLIC_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.vitalsConceptType.SYSTOLIC_FIELD_CONCEPT

enum class BloodPressureType {
    NORMAL {
        override fun relatedColor() = R.color.bp_normal
        override fun relatedText() = R.string.bp_normal
        override fun relatedRecommendation() = R.string.bp_normal_recommendation
    },
    STAGE_I {
        override fun relatedColor() = R.color.bp_ht_stage_I
        override fun relatedText() = R.string.bp_ht_stage_I
        override fun relatedRecommendation() = R.string.bp_ht_stage_I_recommendation

    },
    STAGE_II_A
    {
        override fun relatedColor() = R.color.bp_ht_stage_II_A
        override fun relatedText() = R.string.bp_ht_stage_II_A
        override fun relatedRecommendation() = R.string.bp_ht_stage_II_A_recommendation
    },
    STAGE_II_B
    {
        override fun relatedColor() = R.color.bp_ht_stage_II_B
        override fun relatedText() = R.string.bp_ht_stage_II_B
        override fun relatedRecommendation() = R.string.bp_ht_stage_II_B_recommendation
    },
    STAGE_II_C
    {
        override fun relatedColor() = R.color.bp_ht_stage_II_C
        override fun relatedText() = R.string.bp_ht_stage_II_C
        override fun relatedRecommendation() = R.string.bp_ht_stage_II_C_recommendation
    };

    abstract fun relatedColor(): Int
    abstract fun relatedText(): Int
    abstract fun relatedRecommendation(): Int
}

fun hypertensionTypeFromEncounter(encounter: Encounter): BloodPressureType? {
    val systolic =
        encounter.observations.find { it.concept?.uuid?.equals(SYSTOLIC_FIELD_CONCEPT) == true }?.displayValue?.toDouble()
    val diastolic =
        encounter.observations.find { it.concept?.uuid?.equals(DIASTOLIC_FIELD_CONCEPT) == true }?.displayValue?.toDouble()

    if (systolic == null || diastolic == null) return null;

    if (systolic >= 180 || diastolic >= 110) return BloodPressureType.STAGE_II_C;
    if (systolic >= 160 || diastolic >= 100) return BloodPressureType.STAGE_II_B;
    if (systolic >= 140 || diastolic >= 90) return BloodPressureType.STAGE_II_A;
    if (systolic >= 130 || diastolic >= 80) return BloodPressureType.STAGE_I;

    return BloodPressureType.NORMAL;
}
