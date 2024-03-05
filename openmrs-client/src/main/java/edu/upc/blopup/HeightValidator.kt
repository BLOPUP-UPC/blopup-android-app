package edu.upc.blopup

class HeightValidator {
    companion object {
        fun isValid(height: String, weight: String, systolic: String, diastolic: String, heartRate: String): Boolean {
            val heightValue = height.trim().toIntOrNull()
            val isHeightValid = heightValue in (50..280)
            val isOtherDataPresent = weight.isNotEmpty() || systolic.isNotEmpty() || diastolic.isNotEmpty() || heartRate.isNotEmpty()

            return isHeightValid || (isOtherDataPresent && heightValue == null)
        }
    }
}