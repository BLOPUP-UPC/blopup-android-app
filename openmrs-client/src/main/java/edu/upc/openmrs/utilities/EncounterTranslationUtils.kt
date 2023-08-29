package edu.upc.openmrs.utilities

import edu.upc.R

object EncounterTranslationUtils {
    private val translationMap: Map<String, Int> = mapOf(
        "Systolic" to R.string.systolic_label,
        "Diastolic" to R.string.diastolic_label,
        "Pulse" to R.string.pulse_label,
        "Weight (kg)" to R.string.weight_value,
        "Height (cm)" to R.string.height_value,
        "Vitals" to R.string.vitals,
        "Visit Note" to R.string.visit_note
    )

    @JvmStatic
    fun getTranslatedResourceId(observation: String): Int {
        for (key in translationMap.keys) {
            if (observation.contains(key)) {
                return translationMap[key] ?: 0
            }
        }
        return 0
    }
}
