package edu.upc.blopup.model

import edu.upc.R

data class BloodPressure(val systolic: Int, val diastolic: Int, val pulse: Int) {
    fun bloodPressureType(): BloodPressureType {
        return when {
            systolic >= 180 || diastolic >= 110 -> BloodPressureType.STAGE_II_C
            systolic >= 160 || diastolic >= 100 -> BloodPressureType.STAGE_II_B
            systolic >= 140 || diastolic >= 90  -> BloodPressureType.STAGE_II_A
            systolic >= 130 || diastolic >= 80  -> BloodPressureType.STAGE_I
            else -> BloodPressureType.NORMAL
        }
    }
}

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