package edu.upc.blopup.vitalsform

class Vital(val concept: String, val value: String) {

    companion object {
        val DEFAULT_VALUE = -1.0F
    }

    fun validate(): Boolean {
        return value.isNotEmpty() && value.toFloat() != DEFAULT_VALUE
    }
}