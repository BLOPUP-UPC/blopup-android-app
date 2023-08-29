package edu.upc.openmrs.utilities

import edu.upc.R
import org.junit.Assert.assertEquals
import org.junit.Test


class EncounterTranslationUtilsTest{

    @Test
    fun `when a string contains the value, then I am able to return the translation`(){
        val observationString = "Systolic: 173"

        val expected = R.string.systolic_label
        val result = EncounterTranslationUtils.getTranslatedResourceId(observationString)

        assertEquals(expected, result)
    }

    @Test
    fun `when a string does not contain the value, then return 0`(){
        val observationString = "No value matches"

        val expected = 0
        val result = EncounterTranslationUtils.getTranslatedResourceId(observationString)

        assertEquals(expected, result)
    }
}