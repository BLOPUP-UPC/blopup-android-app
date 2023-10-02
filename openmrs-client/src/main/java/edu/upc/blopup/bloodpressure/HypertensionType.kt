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

// Convert to anonymous enum class

enum class HypertensionType {
    NORMAL {
        override fun relatedColor() = R.color.ht_normal
        override fun relatedText() = R.string.hp_normal
    },
    STAGE_I {
        override fun relatedColor() = R.color.bp_ht_stage_I
        override fun relatedText() = 2
    },
    STAGE_II_A
    {
        override fun relatedColor() = R.color.bp_ht_stage_II_A
        override fun relatedText() = 3

    },
    STAGE_II_B
    {
        override fun relatedColor() = R.color.bp_ht_stage_II_B
        override fun relatedText() = 4

    },
    STAGE_II_C
    {
        override fun relatedColor() = R.color.bp_ht_stage_II_C
        override fun relatedText() = 5
    };

    abstract fun relatedColor(): Int
    abstract fun relatedText(): Int
}

fun hypertensionTypeFromEncounter(encounter: Encounter): HypertensionType? {
    val systolic =
        encounter.observations.find { it.display?.contains("Systolic") == true }?.displayValue?.toDouble()
    val diastolic =
        encounter.observations.find { it.display?.contains("Diastolic") == true }?.displayValue?.toDouble()

    if (systolic == null || diastolic == null) return null;

    if (systolic >= 180 || diastolic >= 110) return HypertensionType.STAGE_II_C;
    if (systolic >= 160 || diastolic >= 100) return HypertensionType.STAGE_II_B;
    if (systolic >= 140 || diastolic >= 90) return HypertensionType.STAGE_II_A;
    if (systolic >= 130 || diastolic >= 80) return HypertensionType.STAGE_I;

    return HypertensionType.NORMAL;
}