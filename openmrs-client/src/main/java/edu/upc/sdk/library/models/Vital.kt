package edu.upc.sdk.library.models

class Vital(val concept: String, val value: String) {

    companion object {
        const val DEFAULT_VALUE = -1.0F
    }

    override fun toString(): String {
        return "Vital(concept=$concept, value=$value)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vital) return false
        return this.concept == other.concept && this.value == other.value
    }

    override fun hashCode(): Int {
        var result = concept.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}