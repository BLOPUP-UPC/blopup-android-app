package edu.upc.openmrs.test

import edu.upc.blopup.model.VisitExample
import edu.upc.openmrs.activities.visitdashboard.BMICalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BMICalculatorTest {

    private val calculator = BMICalculator()

    @Test
    fun givenAWeightAndHeight_calculateBMI(){
        val visit = VisitExample.random(heightCm = 165, weightKg = 70.0F)

        assertEquals(25.711662F, calculator.execute(visit))
    }

    @Test
    fun givenJustWeightInput_returnANotApplicable(){
        val visit = VisitExample.random(heightCm = 165, weightKg = null)

        assertNull(calculator.execute(visit))
    }

    @Test
    fun givenJustHeightInput_returnANotApplicable(){
        val visit = VisitExample.random(heightCm = null, weightKg = 80.1F)

        assertNull(calculator.execute(visit))
    }

    @Test
    fun givenNonHeightOrWeightInput_returnANotApplicable(){
        val visit = VisitExample.random(heightCm = null, weightKg = null)

        assertNull(calculator.execute(visit))
    }
}