package edu.upc.openmrs.test

import edu.upc.openmrs.activities.visitdashboard.BMICalculator
import edu.upc.sdk.library.models.Observation
import org.junit.Assert.*
import org.junit.Test

class BMICalculatorTest(){

    private val calculator = BMICalculator();

    private val weightObservation = Observation().apply {
        display = "Weight (kg): 70.0"
        displayValue = "70.0"
    }
    private val heightObservation = Observation().apply {
        display= "Height (cm): 165.0"
        displayValue= "165.0"
    }
    @Test
    fun givenAWeightAndHeight_calculateBMI(){
        val list = listOf(weightObservation, heightObservation)

        val result = calculator.execute(list)

        assertEquals("25.7", result)
    }

    @Test
    fun givenJustWeightInput_returnANotApplicable(){
        val list = listOf(weightObservation)

        val result = calculator.execute(list)

        assertEquals("N/A", result)
    }

    @Test
    fun givenJustHeightInput_returnANotApplicable(){
        val list = listOf(heightObservation)

        val result = calculator.execute(list)

        assertEquals("N/A", result)
    }

    @Test
    fun givenNonHeightOrWeightInput_returnANotApplicable(){
        val list = emptyList<Observation>()

        val result = calculator.execute(list)

        assertEquals("N/A", result)
    }
}