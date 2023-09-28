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

import edu.upc.sdk.library.models.Encounter

enum class HypertensionType {
    NORMAL, STAGE_I, STAGE_II_A, STAGE_II_B, STAGE_II_C
}

fun hypertensionTypeFromEncounter(encounter: Encounter) : HypertensionType? {
    val systolic = encounter.observations.find { it.display?.contains("Systolic") == true }?.displayValue?.toDouble()
    val diastolic = encounter.observations.find { it.display?.contains("Diastolic") == true }?.displayValue?.toDouble()

    if (systolic == null || diastolic == null) return null;

    if (systolic >= 180 || diastolic >= 110) return HypertensionType.STAGE_II_C;
    if (systolic >= 160 || diastolic >= 100) return HypertensionType.STAGE_II_B;
    if (systolic >= 140 || diastolic >= 90) return HypertensionType.STAGE_II_A;
    if (systolic >= 130 || diastolic >= 80) return HypertensionType.STAGE_I;

    return HypertensionType.NORMAL;
}